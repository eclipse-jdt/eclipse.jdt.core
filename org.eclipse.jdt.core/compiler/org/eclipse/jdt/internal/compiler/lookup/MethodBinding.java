/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;

public class MethodBinding extends Binding implements TypeConstants {
	
	public int modifiers;
	public char[] selector;
	public TypeBinding returnType;
	public TypeBinding[] parameters;
	public ReferenceBinding[] thrownExceptions;
	public ReferenceBinding declaringClass;
	public TypeVariableBinding[] typeVariables = Binding.NO_TYPE_VARIABLES;
	char[] signature;
	public long tagBits;
	
protected MethodBinding() {
	// for creating problem or synthetic method
}
public MethodBinding(int modifiers, char[] selector, TypeBinding returnType, TypeBinding[] parameters, ReferenceBinding[] thrownExceptions, ReferenceBinding declaringClass) {
	this.modifiers = modifiers;
	this.selector = selector;
	this.returnType = returnType;
	this.parameters = (parameters == null || parameters.length == 0) ? Binding.NO_PARAMETERS : parameters;
	this.thrownExceptions = (thrownExceptions == null || thrownExceptions.length == 0) ? Binding.NO_EXCEPTIONS : thrownExceptions;
	this.declaringClass = declaringClass;
	
	// propagate the strictfp & deprecated modifiers
	if (this.declaringClass != null) {
		if (this.declaringClass.isStrictfp())
			if (!(isNative() || isAbstract()))
				this.modifiers |= ClassFileConstants.AccStrictfp;
	}
}
public MethodBinding(int modifiers, TypeBinding[] parameters, ReferenceBinding[] thrownExceptions, ReferenceBinding declaringClass) {
	this(modifiers, TypeConstants.INIT, TypeBinding.VOID, parameters, thrownExceptions, declaringClass);
}
// special API used to change method declaring class for runtime visibility check
public MethodBinding(MethodBinding initialMethodBinding, ReferenceBinding declaringClass) {
	this.modifiers = initialMethodBinding.modifiers;
	this.selector = initialMethodBinding.selector;
	this.returnType = initialMethodBinding.returnType;
	this.parameters = initialMethodBinding.parameters;
	this.thrownExceptions = initialMethodBinding.thrownExceptions;
	this.declaringClass = declaringClass;
	declaringClass.storeAnnotationHolder(this, initialMethodBinding.declaringClass.retrieveAnnotationHolder(initialMethodBinding, true));
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

	for (int i = 0; i < length; i++)
		if (parameters[i] != args[i] && parameters[i].erasure() != args[i].erasure())
			return false;
	return true;
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
public final boolean areParametersCompatibleWith(TypeBinding[] arguments) {
	int paramLength = this.parameters.length;
	int argLength = arguments.length;
	int lastIndex = argLength;
	if (isVarargs()) {
		lastIndex = paramLength - 1;
		if (paramLength == argLength) { // accept X[] but not X or X[][]
			TypeBinding varArgType = parameters[lastIndex]; // is an ArrayBinding by definition
			TypeBinding lastArgument = arguments[lastIndex];
			if (varArgType != lastArgument && !lastArgument.isCompatibleWith(varArgType))
				return false;
		} else if (paramLength < argLength) { // all remainig argument types must be compatible with the elementsType of varArgType
			TypeBinding varArgType = ((ArrayBinding) parameters[lastIndex]).elementsType();
			for (int i = lastIndex; i < argLength; i++)
				if (varArgType != arguments[i] && !arguments[i].isCompatibleWith(varArgType))
					return false;
		} else if (lastIndex != argLength) { // can call foo(int i, X ... x) with foo(1) but NOT foo();
			return false;
		}
		// now compare standard arguments from 0 to lastIndex
	}
	for (int i = 0; i < lastIndex; i++)
		if (parameters[i] != arguments[i] && !arguments[i].isCompatibleWith(parameters[i]))
			return false;
	return true;
}

/* API
* Answer the receiver's binding type from Binding.BindingID.
*/

public final int kind() {
	return Binding.METHOD;
}
/* Answer true if the receiver is visible to the invocationPackage.
*/

public final boolean canBeSeenBy(PackageBinding invocationPackage) {
	if (isPublic()) return true;
	if (isPrivate()) return false;

	// isProtected() or isDefault()
	return invocationPackage == declaringClass.getPackage();
}
/* Answer true if the type variables have the same erasure
*/
public final boolean areTypeVariableErasuresEqual(MethodBinding method) {
	TypeVariableBinding[] vars = method.typeVariables;
	if (this.typeVariables == vars)
		return true;

	int length = this.typeVariables.length;
	if (length != vars.length)
		return false;

	for (int i = 0; i < length; i++)
		if (this.typeVariables[i] != vars[i] && this.typeVariables[i].erasure() != vars[i].erasure())
			return false;
	return true;
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
		TypeBinding receiverErasure = receiverType.erasure();		
		ReferenceBinding declaringErasure = (ReferenceBinding) declaringClass.erasure();
		int depth = 0;
		do {
			if (currentType.findSuperTypeWithSameErasure(declaringErasure) != null) {
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
				if (currentType == receiverErasure || ((ReferenceBinding)receiverErasure).findSuperTypeWithSameErasure(currentType) != null){
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
MethodBinding computeSubstitutedMethod(MethodBinding method, LookupEnvironment env) {
	TypeVariableBinding[] vars = this.typeVariables;
	TypeVariableBinding[] vars2 = method.typeVariables;
	if (vars.length != vars2.length)
		return null;
	for (int v = vars.length; --v >= 0;)
		if (!vars[v].isInterchangeableWith(env, vars2[v]))
			return null;

	// must substitute to detect cases like:
	//   <T1 extends X<T1>> void dup() {}
	//   <T2 extends X<T2>> Object dup() {return null;}
	return new ParameterizedGenericMethodBinding(method, vars, env);
}
/*
 * declaringUniqueKey dot selector genericSignature
 * p.X { <T> void bar(X<T> t) } --> Lp/X;.bar<T:Ljava/lang/Object;>(LX<TT;>;)V
 */
public char[] computeUniqueKey(boolean isLeaf) {
	// declaring class 
	char[] declaringKey = this.declaringClass.computeUniqueKey(false/*not a leaf*/);
	int declaringLength = declaringKey.length;
	
	// selector
	int selectorLength = this.selector == TypeConstants.INIT ? 0 : this.selector.length;
	
	// generic signature
	char[] sig = genericSignature();
	if (sig == null) sig = signature();
	int signatureLength = sig.length;
	
	char[] uniqueKey = new char[declaringLength + 1 + selectorLength + signatureLength];
	int index = 0;
	System.arraycopy(declaringKey, 0, uniqueKey, index, declaringLength);
	index = declaringLength;
	uniqueKey[index++] = '.';
	System.arraycopy(this.selector, 0, uniqueKey, index, selectorLength);
	index += selectorLength;
	System.arraycopy(sig, 0, uniqueKey, index, signatureLength);
	//index += signatureLength;
	return uniqueKey;
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
	if ((this.modifiers & ExtraCompilerModifiers.AccGenericSignature) == 0) return null;
	StringBuffer sig = new StringBuffer(10);
	if (this.typeVariables != Binding.NO_TYPE_VARIABLES) {
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
	sig.append(')');
	if (this.returnType != null)
		sig.append(this.returnType.genericTypeSignature());
	
	// only append thrown exceptions if any is generic/parameterized
	boolean needExceptionSignatures = false;
	int length = this.thrownExceptions.length;
	for (int i = 0; i < length; i++) {
		if((this.thrownExceptions[i].modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0) {
			needExceptionSignatures = true;
			break;
		}
	}
	if (needExceptionSignatures) {
		for (int i = 0; i < length; i++) {
			sig.append('^');
			sig.append(this.thrownExceptions[i].genericTypeSignature());
		}
	}
	int sigLength = sig.length();
	char[] genericSignature = new char[sigLength];
	sig.getChars(0, sigLength, genericSignature, 0);	
	return genericSignature;
}
public AnnotationBinding[] getAnnotations() {
	MethodBinding originalMethod = this.original();
	return originalMethod.declaringClass.retrieveAnnotations(originalMethod);
}
/**
 * @param index the index of the parameter of interest
 * @return the annotations on the <code>index</code>th parameter
 * @throws ArrayIndexOutOfBoundsException when <code>index</code> is not valid 
 */
public AnnotationBinding[] getParameterAnnotations(int index) {
	MethodBinding originalMethod = this.original();
	AnnotationHolder holder = originalMethod.declaringClass.retrieveAnnotationHolder(originalMethod, true);
	return holder == null ? Binding.NO_ANNOTATIONS : holder.getParameterAnnotations(index);
}
public final int getAccessFlags() {
	return modifiers & ExtraCompilerModifiers.AccJustFlag;
}

/**
 * Compute the tagbits for standard annotations. For source types, these could require
 * lazily resolving corresponding annotation nodes, in case of forward references.
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#getAnnotationTagBits()
 */
public long getAnnotationTagBits() {
	MethodBinding originalMethod = this.original();
	if ((originalMethod.tagBits & TagBits.AnnotationResolved) == 0 && originalMethod.declaringClass instanceof SourceTypeBinding) {
		ClassScope scope = ((SourceTypeBinding) originalMethod.declaringClass).scope;
		if (scope != null) {
			TypeDeclaration typeDecl = scope.referenceContext;
			AbstractMethodDeclaration methodDecl = typeDecl.declarationOf(originalMethod);
			if (methodDecl != null)
				ASTNode.resolveAnnotations(methodDecl.scope, methodDecl.annotations, originalMethod);
		}
	}
	return originalMethod.tagBits;
}
/**
 * @return the default value for this annotation method or <code>null</code> if there is no default value
 */
public Object getDefaultValue() {
	MethodBinding originalMethod = this.original();
	AnnotationHolder holder = originalMethod.declaringClass.retrieveAnnotationHolder(originalMethod, true);
	return holder == null ? null : holder.getDefaultValue();
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
	return (modifiers & ClassFileConstants.AccAbstract) != 0;
}

/* Answer true if the receiver is a bridge method
*/
public final boolean isBridge() {
	return (modifiers & ClassFileConstants.AccBridge) != 0;
}

/* Answer true if the receiver is a constructor
*/
public final boolean isConstructor() {
	return selector == TypeConstants.INIT;
}

/* Answer true if the receiver has default visibility
*/
public final boolean isDefault() {
	return !isPublic() && !isProtected() && !isPrivate();
}

/* Answer true if the receiver is a system generated default abstract method
*/
public final boolean isDefaultAbstract() {
	return (modifiers & ExtraCompilerModifiers.AccDefaultAbstract) != 0;
}

/* Answer true if the receiver is a deprecated method
*/
public final boolean isDeprecated() {
	return (modifiers & ClassFileConstants.AccDeprecated) != 0;
}

/* Answer true if the receiver is final and cannot be overridden
*/
public final boolean isFinal() {
	return (modifiers & ClassFileConstants.AccFinal) != 0;
}

/* Answer true if the receiver is implementing another method
 * in other words, it is overriding and concrete, and overriden method is abstract
 * Only set for source methods
*/
public final boolean isImplementing() {
	return (modifiers & ExtraCompilerModifiers.AccImplementing) != 0;
}

/* Answer true if the receiver is a native method
*/
public final boolean isNative() {
	return (modifiers & ClassFileConstants.AccNative) != 0;
}

/* Answer true if the receiver is overriding another method
 * Only set for source methods
*/
public final boolean isOverriding() {
	return (modifiers & ExtraCompilerModifiers.AccOverriding) != 0;
}
/*
 * Answer true if the receiver is a "public static void main(String[])" method
 */
public final boolean isMain() {
	if (this.selector.length == 4 && CharOperation.equals(this.selector, MAIN)
			&& ((this.modifiers & (ClassFileConstants.AccPublic | ClassFileConstants.AccStatic)) != 0)
			&& TypeBinding.VOID == this.returnType  
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
	return (modifiers & ClassFileConstants.AccPrivate) != 0;
}

/* Answer true if the receiver has private visibility and is used locally
*/
public final boolean isUsed() {
	return (modifiers & ExtraCompilerModifiers.AccLocallyUsed) != 0;
}

/* Answer true if the receiver has protected visibility
*/
public final boolean isProtected() {
	return (modifiers & ClassFileConstants.AccProtected) != 0;
}

/* Answer true if the receiver has public visibility
*/
public final boolean isPublic() {
	return (modifiers & ClassFileConstants.AccPublic) != 0;
}

/* Answer true if the receiver got requested to clear the private modifier
 * during private access emulation.
 */
public final boolean isRequiredToClearPrivateModifier() {
	return (modifiers & ExtraCompilerModifiers.AccClearPrivateModifier) != 0;
}

/* Answer true if the receiver is a static method
*/
public final boolean isStatic() {
	return (modifiers & ClassFileConstants.AccStatic) != 0;
}

/* Answer true if all float operations must adher to IEEE 754 float/double rules
*/
public final boolean isStrictfp() {
	return (modifiers & ClassFileConstants.AccStrictfp) != 0;
}

/* Answer true if the receiver is a synchronized method
*/
public final boolean isSynchronized() {
	return (modifiers & ClassFileConstants.AccSynchronized) != 0;
}

/* Answer true if the receiver has public visibility
*/
public final boolean isSynthetic() {
	return (modifiers & ClassFileConstants.AccSynthetic) != 0;
}

/* Answer true if the receiver method has varargs
*/
public final boolean isVarargs() {
	return (modifiers & ClassFileConstants.AccVarargs) != 0;
}

/* Answer true if the receiver's declaring type is deprecated (or any of its enclosing types)
*/
public final boolean isViewedAsDeprecated() {
	return (modifiers & (ClassFileConstants.AccDeprecated | ExtraCompilerModifiers.AccDeprecatedImplicitly)) != 0;
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
	if (parameters != Binding.NO_PARAMETERS) {
		for (int i = 0, length = parameters.length; i < length; i++) {
			if (i > 0)
				buffer.append(", "); //$NON-NLS-1$
			buffer.append(parameters[i].sourceName());
		}
	}
	buffer.append(')');
	return buffer.toString().toCharArray();
}
public void setAnnotations(AnnotationBinding[] annotations) {
	this.declaringClass.storeAnnotations(this, annotations);
}
public void setAnnotations(AnnotationBinding[] annotations, AnnotationBinding[][] parameterAnnotations, Object defaultValue) {
	this.declaringClass.storeAnnotationHolder(this,  AnnotationHolder.storeAnnotations(annotations, parameterAnnotations, defaultValue));
}
public void setDefaultValue(Object defaultValue) {
	AnnotationHolder holder = this.declaringClass.retrieveAnnotationHolder(this, false);
	if (holder == null)
		setAnnotations(null, null, defaultValue);
	else
		setAnnotations(holder.getAnnotations(), holder.getParameterAnnotations(), defaultValue);
}
public void setParameterAnnotations(AnnotationBinding[][] parameterAnnotations) {
	AnnotationHolder holder = this.declaringClass.retrieveAnnotationHolder(this, false);
	if (holder == null)
		setAnnotations(null, parameterAnnotations, null);
	else
		setAnnotations(holder.getAnnotations(), parameterAnnotations, holder.getDefaultValue());
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
	if (parameters != Binding.NO_PARAMETERS) {
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
	boolean isConstructor = isConstructor();
	if (isConstructor && declaringClass.isEnum()) { // insert String name,int ordinal 
		buffer.append(ConstantPool.JavaLangStringSignature);
		buffer.append(TypeBinding.INT.signature());
	}
	boolean needSynthetics = isConstructor && declaringClass.isNestedType();
	if (needSynthetics) {
		// take into account the synthetic argument type signatures as well
		ReferenceBinding[] syntheticArgumentTypes = declaringClass.syntheticEnclosingInstanceTypes();
		if (syntheticArgumentTypes != null) {
			for (int i = 0, count = syntheticArgumentTypes.length; i < count; i++) {
				buffer.append(syntheticArgumentTypes[i].signature());
			}
		}
		
		if (this instanceof SyntheticMethodBinding) {
			targetParameters = ((SyntheticMethodBinding)this).targetMethod.parameters;
		}
	}

	if (targetParameters != Binding.NO_PARAMETERS) {
		for (int i = 0; i < targetParameters.length; i++) {
			buffer.append(targetParameters[i].signature());
		}
	}
	if (needSynthetics) {
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
	if (this.returnType != null)
		buffer.append(this.returnType.signature());
	int nameLength = buffer.length();
	signature = new char[nameLength];
	buffer.getChars(0, nameLength, signature, 0);	    
	
	return signature;
}
public final int sourceEnd() {
	AbstractMethodDeclaration method = sourceMethod();
	if (method == null) {
		if (this.declaringClass instanceof SourceTypeBinding)
			return ((SourceTypeBinding) this.declaringClass).sourceEnd();
		return 0;
	}
	return method.sourceEnd;
}
public AbstractMethodDeclaration sourceMethod() {
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
	if (method == null) {
		if (this.declaringClass instanceof SourceTypeBinding)
			return ((SourceTypeBinding) this.declaringClass).sourceStart();
		return 0;
	}
	return method.sourceStart;
}

/* During private access emulation, the binding can be requested to loose its
 * private visibility when the class file is dumped.
 */

public final void tagForClearingPrivateModifier() {
	modifiers |= ExtraCompilerModifiers.AccClearPrivateModifier;
}
public String toString() {
	String s = (returnType != null) ? returnType.debugName() : "NULL TYPE"; //$NON-NLS-1$
	s += " "; //$NON-NLS-1$
	s += (selector != null) ? new String(selector) : "UNNAMED METHOD"; //$NON-NLS-1$

	s += "("; //$NON-NLS-1$
	if (parameters != null) {
		if (parameters != Binding.NO_PARAMETERS) {
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
		if (thrownExceptions != Binding.NO_EXCEPTIONS) {
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
/**
 * Returns the method to use during tiebreak (usually the method itself).
 * For generic method invocations, tiebreak needs to use generic method with erasure substitutes.
 */
public MethodBinding tiebreakMethod() {
	return this;
}
public TypeVariableBinding[] typeVariables() {
	return this.typeVariables;
}
}
