/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;

public class MethodBinding extends Binding implements BaseTypes, TypeConstants {
	public int modifiers;
	public char[] selector;
	public TypeBinding returnType;
	public TypeBinding[] parameters;
	public ReferenceBinding[] thrownExceptions;
	public ReferenceBinding declaringClass;
	public TypeVariableBinding[] typeVariables = NoTypeVariables;

	char[] signature;

protected MethodBinding() {
	// for creating problem or synthetic method
}
public MethodBinding(int modifiers, char[] selector, TypeBinding returnType, TypeBinding[] parameters, ReferenceBinding[] thrownExceptions, ReferenceBinding declaringClass) {
	this.modifiers = modifiers;
	this.selector = selector;
	this.returnType = returnType;
	this.parameters = (parameters == null || parameters.length == 0) ? NoParameters : parameters;
	this.thrownExceptions = (thrownExceptions == null || thrownExceptions.length == 0) ? NoExceptions : thrownExceptions;
	this.declaringClass = declaringClass;
	
	// propagate the strictfp & deprecated modifiers
	if (this.declaringClass != null) {
		if (this.declaringClass.isStrictfp())
			if (!(isNative() || isAbstract()))
				this.modifiers |= AccStrictfp;
		if (this.declaringClass.isViewedAsDeprecated() && !isDeprecated())
			this.modifiers |= AccDeprecatedImplicitly;
	}
}
public MethodBinding(int modifiers, TypeBinding[] parameters, ReferenceBinding[] thrownExceptions, ReferenceBinding declaringClass) {
	this(modifiers, ConstructorDeclaration.ConstantPoolName, VoidBinding, parameters, thrownExceptions, declaringClass);
}
// special API used to change method declaring class for runtime visibility check
public MethodBinding(MethodBinding initialMethodBinding, ReferenceBinding declaringClass) {
	this.modifiers = initialMethodBinding.modifiers;
	this.selector = initialMethodBinding.selector;
	this.returnType = initialMethodBinding.returnType;
	this.parameters = initialMethodBinding.parameters;
	this.thrownExceptions = initialMethodBinding.thrownExceptions;
	this.declaringClass = declaringClass;
}
/* Answer true if the argument types & the receiver's parameters are equal
*/
public final boolean areParametersEqual(MethodBinding method) {
	TypeBinding[] args = method.parameters;
	if (parameters == args)
		return true;

	int length = parameters.length;
	if (length != args.length)
		return false;
	
	for (int i = 0; i < length; i++)
		if (parameters[i] != args[i])
			return false;
	return true;
}
/* Answer true if the argument types & the receiver's parameters have the same erasure
*/
public final boolean areParameterErasuresEqual(MethodBinding method) {
	TypeBinding[] args = method.parameters;
	if (parameters == args)
		return true;

	int length = parameters.length;
	if (length != args.length)
		return false;

	for (int i = 0; i < length; i++) {
		if (parameters[i].erasure() != args[i].erasure()) return false;
	}
	return true;
}
/* API
* Answer the receiver's binding type from Binding.BindingID.
*/

public final int bindingType() {
	return METHOD;
}
/* Answer true if the receiver is visible to the type provided by the scope.
* InvocationSite implements isSuperAccess() to provide additional information
* if the receiver is protected.
*
* NOTE: This method should ONLY be sent if the receiver is a constructor.
*
* NOTE: Cannot invoke this method with a compilation unit scope.
*/

public final boolean canBeSeenBy(InvocationSite invocationSite, Scope scope) {
	if (isPublic()) return true;

	SourceTypeBinding invocationType = scope.enclosingSourceType();
	if (invocationType == declaringClass) return true;

	if (isProtected()) {
		// answer true if the receiver is in the same package as the invocationType
		if (invocationType.fPackage == declaringClass.fPackage) return true;
		return invocationSite.isSuperAccess();
	}

	if (isPrivate()) {
		// answer true if the invocationType and the declaringClass have a common enclosingType
		// already know they are not the identical type
		ReferenceBinding outerInvocationType = invocationType;
		ReferenceBinding temp = outerInvocationType.enclosingType();
		while (temp != null) {
			outerInvocationType = temp;
			temp = temp.enclosingType();
		}

		ReferenceBinding outerDeclaringClass = (ReferenceBinding)declaringClass.erasure();
		temp = outerDeclaringClass.enclosingType();
		while (temp != null) {
			outerDeclaringClass = temp;
			temp = temp.enclosingType();
		}
		return outerInvocationType == outerDeclaringClass;
	}

	// isDefault()
	return invocationType.fPackage == declaringClass.fPackage;
}
/* Answer true if the receiver is visible to the type provided by the scope.
* InvocationSite implements isSuperAccess() to provide additional information
* if the receiver is protected.
*
* NOTE: Cannot invoke this method with a compilation unit scope.
*/
public final boolean canBeSeenBy(TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
	if (isPublic()) return true;

	SourceTypeBinding invocationType = scope.enclosingSourceType();
	if (invocationType == declaringClass && invocationType == receiverType) return true;

	if (isProtected()) {
		// answer true if the invocationType is the declaringClass or they are in the same package
		// OR the invocationType is a subclass of the declaringClass
		//    AND the receiverType is the invocationType or its subclass
		//    OR the method is a static method accessed directly through a type
		//    OR previous assertions are true for one of the enclosing type
		if (invocationType == declaringClass) return true;
		if (invocationType.fPackage == declaringClass.fPackage) return true;
		
		ReferenceBinding currentType = invocationType;
		int depth = 0;
		do {
			if (declaringClass.isSuperclassOf(currentType)) {
				if (invocationSite.isSuperAccess()){
					return true;
				}
				// receiverType can be an array binding in one case... see if you can change it
				if (receiverType instanceof ArrayBinding){
					return false;
				}
				if (isStatic()){
					if (depth > 0) invocationSite.setDepth(depth);
					return true; // see 1FMEPDL - return invocationSite.isTypeAccess();
				}
				if (currentType == receiverType || currentType.isSuperclassOf((ReferenceBinding) receiverType)){
					if (depth > 0) invocationSite.setDepth(depth);
					return true;
				}
			}
			depth++;
			currentType = currentType.enclosingType();
		} while (currentType != null);
		return false;
	}

	if (isPrivate()) {
		// answer true if the receiverType is the declaringClass
		// AND the invocationType and the declaringClass have a common enclosingType
		receiverCheck: {
			if (receiverType != declaringClass) {
				// special tolerance for type variable direct bounds
				if (receiverType.isTypeVariable() && ((TypeVariableBinding) receiverType).isErasureBoundTo(declaringClass.erasure())) {
					break receiverCheck;
				}
				return false;
			}
		}

		if (invocationType != declaringClass) {
			ReferenceBinding outerInvocationType = invocationType;
			ReferenceBinding temp = outerInvocationType.enclosingType();
			while (temp != null) {
				outerInvocationType = temp;
				temp = temp.enclosingType();
			}

			ReferenceBinding outerDeclaringClass = (ReferenceBinding)declaringClass.erasure();
			temp = outerDeclaringClass.enclosingType();
			while (temp != null) {
				outerDeclaringClass = temp;
				temp = temp.enclosingType();
			}
			if (outerInvocationType != outerDeclaringClass) return false;
		}
		return true;
	}

	// isDefault()
	if (invocationType.fPackage != declaringClass.fPackage) return false;

	// receiverType can be an array binding in one case... see if you can change it
	if (receiverType instanceof ArrayBinding)
		return false;
	ReferenceBinding type = (ReferenceBinding) receiverType;
	PackageBinding declaringPackage = declaringClass.fPackage;
	do {
		if (declaringClass == type) return true;
		if (declaringPackage != type.fPackage) return false;
	} while ((type = type.superclass()) != null);
	return false;
}

/* 
 * Answer the declaring class to use in the constant pool
 * may not be a reference binding (see subtypes)
 */
public TypeBinding constantPoolDeclaringClass() {
	return this.declaringClass;
}
/* Answer the receiver's constant pool name.
*
* <init> for constructors
* <clinit> for clinit methods
* or the source name of the method
*/
public final char[] constantPoolName() {
	return selector;
}
/**
 *<typeParam1 ... typeParamM>(param1 ... paramN)returnType thrownException1 ... thrownExceptionP
 * T foo(T t) throws X<T>   --->   (TT;)TT;LX<TT;>;
 * void bar(X<T> t)   -->   (LX<TT;>;)V
 * <T> void bar(X<T> t)   -->  <T:Ljava.lang.Object;>(LX<TT;>;)V
 */
public char[] genericSignature() {
	if ((this.modifiers & AccGenericSignature) == 0) return null;
	StringBuffer sig = new StringBuffer(10);
	if (this.typeVariables != NoTypeVariables) {
		sig.append('<');
		for (int i = 0, length = this.typeVariables.length; i < length; i++) {
			sig.append(this.typeVariables[i].genericSignature());
		}
		sig.append('>');
	}
	sig.append('(');
	for (int i = 0, length = this.parameters.length; i < length; i++) {
		sig.append(this.parameters[i].genericTypeSignature());
	}
	sig.append(')').append(this.returnType.genericTypeSignature());
	// only append thrown exception if any is generic/parameterized
	for (int i = 0, length = this.thrownExceptions.length; i < length; i++) {
		if((this.thrownExceptions[i].modifiers & AccGenericSignature) != 0) {
			for (int j = 0; j < length; j++) {
				sig.append(this.thrownExceptions[j].genericTypeSignature());
			}
			break;
		}
	}
	int sigLength = sig.length();
	char[] genericSignature = new char[sigLength];
	sig.getChars(0, sigLength, genericSignature, 0);	
	return genericSignature;
}
public final int getAccessFlags() {
	return modifiers & AccJustFlag;
}
public TypeVariableBinding getTypeVariable(char[] variableName) {
	for (int i = this.typeVariables.length; --i >= 0;)
		if (CharOperation.equals(this.typeVariables[i].sourceName, variableName))
			return this.typeVariables[i];
	return null;
}
/**
 * Returns true if method got substituted parameter types
 * (see ParameterizedMethodBinding)
 */
public boolean hasSubstitutedParameters() {
	return false;
}

/* Answer true if the return type got substituted.
 */
public boolean hasSubstitutedReturnType() {
	return false;
}

/* Answer true if the receiver is an abstract method
*/
public final boolean isAbstract() {
	return (modifiers & AccAbstract) != 0;
}

/* Answer true if the receiver is a bridge method
*/
public final boolean isBridge() {
	return (modifiers & AccBridge) != 0;
}

/* Answer true if the receiver is a constructor
*/
public final boolean isConstructor() {
	return selector == ConstructorDeclaration.ConstantPoolName;
}
protected boolean isConstructorRelated() {
	return isConstructor();
}

/* Answer true if the receiver has default visibility
*/
public final boolean isDefault() {
	return !isPublic() && !isProtected() && !isPrivate();
}

/* Answer true if the receiver is a system generated default abstract method
*/
public final boolean isDefaultAbstract() {
	return (modifiers & AccDefaultAbstract) != 0;
}

/* Answer true if the receiver is a deprecated method
*/
public final boolean isDeprecated() {
	return (modifiers & AccDeprecated) != 0;
}

/* Answer true if the receiver is final and cannot be overridden
*/
public final boolean isFinal() {
	return (modifiers & AccFinal) != 0;
}

/* Answer true if the receiver is implementing another method
 * in other words, it is overriding and concrete, and overriden method is abstract
 * Only set for source methods
*/
public final boolean isImplementing() {
	return (modifiers & AccImplementing) != 0;
}

/* Answer true if the receiver is a native method
*/
public final boolean isNative() {
	return (modifiers & AccNative) != 0;
}

/* Answer true if the receiver is overriding another method
 * Only set for source methods
*/
public final boolean isOverriding() {
	return (modifiers & AccOverriding) != 0;
}
/*
 * Answer true if the receiver is a "public static void main(String[])" method
 */
public final boolean isMain() {
	if (this.selector.length == 4 && CharOperation.equals(this.selector, MAIN)
			&& ((this.modifiers & (AccPublic | AccStatic)) != 0)
			&& VoidBinding == this.returnType  
			&& this.parameters.length == 1) {
		TypeBinding paramType = this.parameters[0];
		if (paramType.dimensions() == 1 && paramType.leafComponentType().id == TypeIds.T_JavaLangString) {
			return true;
		}
	}
	return false;
}
/* Answer true if the receiver has private visibility
*/
public final boolean isPrivate() {
	return (modifiers & AccPrivate) != 0;
}

/* Answer true if the receiver has private visibility and is used locally
*/
public final boolean isPrivateUsed() {
	return (modifiers & AccPrivateUsed) != 0;
}

/* Answer true if the receiver has protected visibility
*/
public final boolean isProtected() {
	return (modifiers & AccProtected) != 0;
}

/* Answer true if the receiver has public visibility
*/
public final boolean isPublic() {
	return (modifiers & AccPublic) != 0;
}

/* Answer true if the receiver got requested to clear the private modifier
 * during private access emulation.
 */
public final boolean isRequiredToClearPrivateModifier() {
	return (modifiers & AccClearPrivateModifier) != 0;
}

/* Answer true if the receiver is a static method
*/
public final boolean isStatic() {
	return (modifiers & AccStatic) != 0;
}

/* Answer true if all float operations must adher to IEEE 754 float/double rules
*/
public final boolean isStrictfp() {
	return (modifiers & AccStrictfp) != 0;
}

/* Answer true if the receiver is a synchronized method
*/
public final boolean isSynchronized() {
	return (modifiers & AccSynchronized) != 0;
}

/* Answer true if the receiver has public visibility
*/
public final boolean isSynthetic() {
	return (modifiers & AccSynthetic) != 0;
}

/* Answer true if the receiver is a vararg method
*/
public final boolean isVararg() {
	return (modifiers & AccVarargs) != 0;
}

/* Answer true if the receiver's declaring type is deprecated (or any of its enclosing types)
*/
public final boolean isViewedAsDeprecated() {
	return (modifiers & AccDeprecated) != 0 ||
		(modifiers & AccDeprecatedImplicitly) != 0;
}

/**
 * Returns the original method (as opposed to parameterized instances)
 */
public MethodBinding original() {
	return this;
}

public char[] readableName() /* foo(int, Thread) */ {
	StringBuffer buffer = new StringBuffer(parameters.length + 1 * 20);
	if (isConstructor())
		buffer.append(declaringClass.sourceName());
	else
		buffer.append(selector);
	buffer.append('(');
	if (parameters != NoParameters) {
		for (int i = 0, length = parameters.length; i < length; i++) {
			if (i > 0)
				buffer.append(", "); //$NON-NLS-1$
			buffer.append(parameters[i].sourceName());
		}
	}
	buffer.append(')');
	return buffer.toString().toCharArray();
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#shortReadableName()
 */
public char[] shortReadableName() {
	StringBuffer buffer = new StringBuffer(parameters.length + 1 * 20);
	if (isConstructor())
		buffer.append(declaringClass.shortReadableName());
	else
		buffer.append(selector);
	buffer.append('(');
	if (parameters != NoParameters) {
		for (int i = 0, length = parameters.length; i < length; i++) {
			if (i > 0)
				buffer.append(", "); //$NON-NLS-1$
			buffer.append(parameters[i].shortReadableName());
		}
	}
	buffer.append(')');
	int nameLength = buffer.length();
	char[] shortReadableName = new char[nameLength];
	buffer.getChars(0, nameLength, shortReadableName, 0);	    
	return shortReadableName;
}

protected final void setSelector(char[] selector) {
	this.selector = selector;
	this.signature = null;
}

/* Answer the receiver's signature.
*
* NOTE: This method should only be used during/after code gen.
* The signature is cached so if the signature of the return type or any parameter
* type changes, the cached state is invalid.
*/
public final char[] signature() /* (ILjava/lang/Thread;)Ljava/lang/Object; */ {
	if (signature != null)
		return signature;

	StringBuffer buffer = new StringBuffer(parameters.length + 1 * 20);
	buffer.append('(');
	
	TypeBinding[] targetParameters = this.parameters;
	boolean considerSynthetics = isConstructorRelated() && declaringClass.isNestedType();
	if (considerSynthetics) {
		
		// take into account the synthetic argument type signatures as well
		ReferenceBinding[] syntheticArgumentTypes = declaringClass.syntheticEnclosingInstanceTypes();
		int count = syntheticArgumentTypes == null ? 0 : syntheticArgumentTypes.length;
		for (int i = 0; i < count; i++) {
			buffer.append(syntheticArgumentTypes[i].signature());
		}
		
		if (this instanceof SyntheticAccessMethodBinding) {
			targetParameters = ((SyntheticAccessMethodBinding)this).targetMethod.parameters;
		}
	}

	if (targetParameters != NoParameters) {
		for (int i = 0; i < targetParameters.length; i++) {
			buffer.append(targetParameters[i].signature());
		}
	}
	if (considerSynthetics) {
		SyntheticArgumentBinding[] syntheticOuterArguments = declaringClass.syntheticOuterLocalVariables();
		int count = syntheticOuterArguments == null ? 0 : syntheticOuterArguments.length;
		for (int i = 0; i < count; i++) {
			buffer.append(syntheticOuterArguments[i].type.signature());
		}
		// move the extra padding arguments of the synthetic constructor invocation to the end		
		for (int i = targetParameters.length, extraLength = parameters.length; i < extraLength; i++) {
			buffer.append(parameters[i].signature());
		}
	}
	buffer.append(')');
	buffer.append(returnType.signature());
	int nameLength = buffer.length();
	signature = new char[nameLength];
	buffer.getChars(0, nameLength, signature, 0);	    
	
	return signature;
}
public final int sourceEnd() {
	AbstractMethodDeclaration method = sourceMethod();
	if (method == null)
		return 0;
	return method.sourceEnd;
}
AbstractMethodDeclaration sourceMethod() {
	SourceTypeBinding sourceType;
	try {
		sourceType = (SourceTypeBinding) declaringClass;
	} catch (ClassCastException e) {
		return null;		
	}

	AbstractMethodDeclaration[] methods = sourceType.scope.referenceContext.methods;
	for (int i = methods.length; --i >= 0;)
		if (this == methods[i].binding)
			return methods[i];
	return null;		
}
public final int sourceStart() {
	AbstractMethodDeclaration method = sourceMethod();
	if (method == null)
		return 0;
	return method.sourceStart;
}

/* During private access emulation, the binding can be requested to loose its
 * private visibility when the class file is dumped.
 */

public final void tagForClearingPrivateModifier() {
	modifiers |= AccClearPrivateModifier;
}
public String toString() {
	String s = (returnType != null) ? returnType.debugName() : "NULL TYPE"; //$NON-NLS-1$
	s += " "; //$NON-NLS-1$
	s += (selector != null) ? new String(selector) : "UNNAMED METHOD"; //$NON-NLS-1$

	s += "("; //$NON-NLS-1$
	if (parameters != null) {
		if (parameters != NoParameters) {
			for (int i = 0, length = parameters.length; i < length; i++) {
				if (i  > 0)
					s += ", "; //$NON-NLS-1$
				s += (parameters[i] != null) ? parameters[i].debugName() : "NULL TYPE"; //$NON-NLS-1$
			}
		}
	} else {
		s += "NULL PARAMETERS"; //$NON-NLS-1$
	}
	s += ") "; //$NON-NLS-1$

	if (thrownExceptions != null) {
		if (thrownExceptions != NoExceptions) {
			s += "throws "; //$NON-NLS-1$
			for (int i = 0, length = thrownExceptions.length; i < length; i++) {
				if (i  > 0)
					s += ", "; //$NON-NLS-1$
				s += (thrownExceptions[i] != null) ? thrownExceptions[i].debugName() : "NULL TYPE"; //$NON-NLS-1$
			}
		}
	} else {
		s += "NULL THROWN EXCEPTIONS"; //$NON-NLS-1$
	}
	return s;
}
public TypeVariableBinding[] typeVariables() {
	return this.typeVariables;
}
}
