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

import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;

class MethodVerifier15 extends MethodVerifier {

MethodVerifier15(LookupEnvironment environment) {
	super(environment);
}
boolean areMethodsEqual(MethodBinding one, MethodBinding substituteTwo) {
	TypeBinding[] oneParams = one.parameters;
	TypeBinding[] twoParams = substituteTwo.parameters;
	boolean checkParameters = false;
	if (oneParams != twoParams) {
		int length = oneParams.length;
		if (length != twoParams.length) return false; // no match

		for (int i = 0; i < length; i++) {
			if (oneParams[i] != twoParams[i]) {
				checkParameters |= oneParams[i].leafComponentType().isParameterizedType();
				if (!areTypesEqual(oneParams[i], twoParams[i])) {
					while (!checkParameters && ++i < length)
						checkParameters |= oneParams[i].leafComponentType().isParameterizedType();
					if (one.areParameterErasuresEqual(substituteTwo)) // at least one parameter may cause a name clash
						detectNameClash(one, substituteTwo, checkParameters);
					return false; // no match but needed to check for a name clash
				}
			}
		}
	}
	return !detectNameClash(one, substituteTwo, checkParameters);
}
boolean areReturnTypesEqual(MethodBinding one, MethodBinding substituteTwo) {
	if (one.returnType == substituteTwo.returnType) return true;

	// methods from classes are always before methods from interfaces
	if (one.declaringClass.isClass() || one.declaringClass.implementsInterface(substituteTwo.declaringClass, true))
		return one.returnType.isCompatibleWith(substituteTwo.returnType);

	if (substituteTwo.declaringClass.implementsInterface(one.declaringClass, true))
		return substituteTwo.returnType.isCompatibleWith(one.returnType);

	// unrelated interfaces... one must be a subtype of the other
	return one.returnType.isCompatibleWith(substituteTwo.returnType)
		|| substituteTwo.returnType.isCompatibleWith(one.returnType);
}
boolean areTypesEqual(TypeBinding one, TypeBinding two) {
	if (one == two) return true;

	switch (one.kind()) {
		case Binding.PARAMETERIZED_TYPE :
		case Binding.RAW_TYPE :
			return one.isEquivalentTo(two);
//		case Binding.TYPE_PARAMETER : // won't work for variables from different classes - need substitution
	}

	// Can skip this since we resolved each method before comparing it, see computeSubstituteMethod()
	//	if (one instanceof UnresolvedReferenceBinding)
	//		return ((UnresolvedReferenceBinding) one).resolvedType == two;
	//	if (two instanceof UnresolvedReferenceBinding)
	//		return ((UnresolvedReferenceBinding) two).resolvedType == one;
	return false; // all other type bindings are identical
}
boolean canSkipInheritedMethods() {
	if (this.type.superclass() != null)
		if (this.type.superclass().isAbstract() || this.type.superclass().isParameterizedType())
			return false;
	return this.type.superInterfaces() == NoSuperInterfaces;
}
boolean canSkipInheritedMethods(MethodBinding one, MethodBinding two) {
	return two == null // already know one is not null
		|| (one.declaringClass == two.declaringClass && !one.declaringClass.isParameterizedType());
}
void checkForBridgeMethod(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	MethodBinding originalInherited = inheritedMethod.original();
	if (inheritedMethod != originalInherited) {
		MethodBinding[] toCheck = (MethodBinding[]) this.currentMethods.get(currentMethod.selector);
		if (toCheck.length > 1) {
			// must check to see if a bridge method will collide with another current method (see 77861)
			for (int i = 0, length = toCheck.length; i < length; i++) {
				if (currentMethod != toCheck[i] && toCheck[i].areParameterErasuresEqual(originalInherited)) {
					problemReporter(toCheck[i]).methodNameClash(toCheck[i], originalInherited); // bridge method will collide
					return;
				}
			}
		}
	}

	// so the parameters are equal and the return type is compatible b/w the currentMethod & the substituted inheritedMethod
	if (originalInherited.returnType != currentMethod.returnType) {
		TypeBinding originalReturnType = originalInherited.returnType.leafComponentType();
		switch (originalReturnType.kind()) {
			case Binding.GENERIC_TYPE :
				// TODO (philippe) - we need this hack until SourceTypeBindings stop acting as ParameterizedTypes
				if (originalReturnType != originalInherited.declaringClass || !inheritedMethod.returnType.leafComponentType().isParameterizedType())
					break;
			case Binding.PARAMETERIZED_TYPE :
				if (!currentMethod.returnType.leafComponentType().isParameterizedType()) {
					if (currentMethod.returnType.leafComponentType().isRawType() && inheritedMethod.returnType.leafComponentType().isRawType())
						break;
					problemReporter(currentMethod).unsafeReturnTypeOverride(currentMethod, originalInherited, ((MethodDeclaration) currentMethod.sourceMethod()).returnType);
				}
				break;
			case Binding.TYPE_PARAMETER : // see 81618
				if (((TypeVariableBinding) originalReturnType).declaringElement == originalInherited) {
					TypeBinding returnType = currentMethod.returnType.leafComponentType();
					if (!returnType.isTypeVariable() || ((TypeVariableBinding) returnType).declaringElement != currentMethod)
						problemReporter(currentMethod).unsafeReturnTypeOverride(currentMethod, originalInherited, ((MethodDeclaration) currentMethod.sourceMethod()).returnType);
				}
				break;
		}
	}
	this.type.addSyntheticBridgeMethod(originalInherited, currentMethod);
}
void checkInheritedMethods(MethodBinding[] methods, int length) {
	int count = length;
	nextMethod : for (int i = 0, l = length - 1; i < l;) {
		MethodBinding method = methods[i++];
		for (int j = i; j <= l; j++) {
			if (method.declaringClass == methods[j].declaringClass && doesMethodOverride(method, methods[j])) {
				// found an inherited ParameterizedType that defines duplicate methods
				problemReporter().duplicateInheritedMethods(this.type, method, methods[j]);
				count--;
				methods[i - 1] = null;
				continue nextMethod;
			}
		}
	}
	if (count < length) {
		if (count == 1) return; // no need to continue since only 1 inherited method is left
		MethodBinding[] newMethods = new MethodBinding[count];
		for (int i = length; --i >= 0;)
			if (methods[i] != null)
				newMethods[--count] = methods[i];
		methods = newMethods;
		length = newMethods.length;
	}

	super.checkInheritedMethods(methods, length);
}
MethodBinding computeSubstituteMethod(MethodBinding inheritedMethod, MethodBinding currentMethod) {
	if (inheritedMethod == null) return null;

	// due to hierarchy & compatibility checks, we need to ensure these 2 methods are resolved
	// should we push these tests to where they're needed? returnType.isCompatibleWith && parameter isEquivalentTo ?
	if (currentMethod.declaringClass instanceof BinaryTypeBinding)
		((BinaryTypeBinding) currentMethod.declaringClass).resolveTypesFor(currentMethod);
	if (inheritedMethod.declaringClass instanceof BinaryTypeBinding)
		((BinaryTypeBinding) inheritedMethod.declaringClass).resolveTypesFor(inheritedMethod);

	TypeVariableBinding[] inheritedTypeVariables = inheritedMethod.typeVariables();
	if (inheritedTypeVariables == NoTypeVariables) return inheritedMethod;
	TypeVariableBinding[] typeVariables = currentMethod == null ? NoTypeVariables : currentMethod.typeVariables;

	int inheritedLength = inheritedTypeVariables.length;
	int length = typeVariables.length;
	TypeBinding[] arguments = new TypeBinding[inheritedLength];
	if (inheritedLength <= length) {
		System.arraycopy(typeVariables, 0, arguments, 0, inheritedLength);
	} else {
		System.arraycopy(typeVariables, 0, arguments, 0, length);
		for (int i = length; i < inheritedLength; i++)
			arguments[i] = inheritedTypeVariables[i].erasure();
	}
	ParameterizedGenericMethodBinding substitute =
		new ParameterizedGenericMethodBinding(inheritedMethod, arguments, this.environment);
	for (int i = 0; i < inheritedLength; i++)
	    if (!inheritedTypeVariables[i].boundCheck(substitute, arguments[i]))
	    	return inheritedMethod; // incompatible due to bound check
   return substitute;
}
boolean detectNameClash(MethodBinding one, MethodBinding substituteTwo, boolean checkParameters) {
	if (doTypeVariablesClash(one, substituteTwo) || (checkParameters && doParametersClash(one, substituteTwo))) {
		if (this.type == one.declaringClass)
		 	problemReporter(one).methodNameClash(one, substituteTwo);
		else
			problemReporter().inheritedMethodsHaveNameClash(this.type, one, substituteTwo);
		return true;
	}
	return false;
}
public boolean doesMethodOverride(MethodBinding method, MethodBinding inheritedMethod) {
	return super.doesMethodOverride(method, computeSubstituteMethod(inheritedMethod, method));
}
boolean doParametersClash(MethodBinding one, MethodBinding substituteTwo) {
	// must check each parameter pair to see if parameterized types are compatible
	TypeBinding[] oneParams = one.parameters;
	TypeBinding[] twoParams = substituteTwo.parameters;
	for (int i = 0, l = oneParams.length; i < l; i++) {
		if (oneParams[i] == twoParams[i]) continue;
		if (!oneParams[i].leafComponentType().isParameterizedType()) continue;

		if (!twoParams[i].leafComponentType().isParameterizedType()
			|| !oneParams[i].isEquivalentTo(twoParams[i])
			|| !twoParams[i].isEquivalentTo(oneParams[i])) {
				return true;
		}
	}
	return false;
}
boolean doTypeVariablesClash(MethodBinding one, MethodBinding substituteTwo) {
	TypeBinding[] currentVars = one.typeVariables;
	TypeBinding[] inheritedVars = substituteTwo.original().typeVariables;
	return currentVars.length != inheritedVars.length && currentVars.length > 0;
}
boolean isInterfaceMethodImplemented(MethodBinding inheritedMethod, MethodBinding existingMethod, ReferenceBinding superType) {
	inheritedMethod = computeSubstituteMethod(inheritedMethod, existingMethod);
	return inheritedMethod.returnType == existingMethod.returnType
		&& super.isInterfaceMethodImplemented(inheritedMethod, existingMethod, superType);
}
}