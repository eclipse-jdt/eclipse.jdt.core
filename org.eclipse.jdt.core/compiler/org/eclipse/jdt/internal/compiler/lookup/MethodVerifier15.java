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

class MethodVerifier15 extends MethodVerifier {

MethodVerifier15(LookupEnvironment environment) {
	super(environment);
}
boolean areParametersEqual(MethodBinding one, MethodBinding two) {
	TypeBinding[] oneArgs = one.parameters;
	TypeBinding[] twoArgs = two.parameters;
	if (oneArgs == twoArgs) return true;

	int length = oneArgs.length;
	if (length != twoArgs.length) return false;

	for (int i = 0; i < length; i++)
		if (!areTypesEqual(oneArgs[i].erasure(), twoArgs[i].erasure())) return false;
	return true;
}
boolean areReturnTypesEqual(MethodBinding one, MethodBinding two) {
	return areTypesEqual(one.returnType.erasure(), two.returnType.erasure());
}
void checkAgainstInheritedMethods(MethodBinding currentMethod, MethodBinding[] methods, int length) {
	nextMethod : for (int i = length; --i >= 0;) {
		MethodBinding inheritedMethod = methods[i];
		if (currentMethod.isStatic() != inheritedMethod.isStatic()) {  // Cannot override a static method or hide an instance method
			this.problemReporter(currentMethod).staticAndInstanceConflict(currentMethod, inheritedMethod);
			continue nextMethod;
		}

		if (!currentMethod.isAbstract() && inheritedMethod.isAbstract()) {
			if ((currentMethod.modifiers & CompilerModifiers.AccOverriding) == 0)
				currentMethod.modifiers |= CompilerModifiers.AccImplementing;
		} else {
			currentMethod.modifiers |= CompilerModifiers.AccOverriding;
		}

		boolean addBridgeMethod = inheritedMethod.hasSubstitutedReturnType()
			&& isTypeSubstituable(currentMethod.returnType, inheritedMethod.returnType);
		if (!addBridgeMethod && !areTypesEqual(currentMethod.returnType, inheritedMethod.returnType)) {
			// can be [] of Class#RAW vs. Class<T>
			if (!isReturnTypeSubstituable(currentMethod, inheritedMethod)) {
				this.problemReporter(currentMethod).incompatibleReturnType(currentMethod, inheritedMethod);
				continue nextMethod;
			} else if (inheritedMethod.typeVariables.length != currentMethod.typeVariables.length) {
				// TODO (kent) work to do on this case
				if (currentMethod.typeVariables.length == 0 && inheritedMethod.declaringClass.isRawType()) {
					// bug 69626
					// no error since the inheritedMethod's type variables are ignored in raw types... why does a raw type binding not remove the type variables?
				} else {
					this.problemReporter(currentMethod).incompatibleReturnType(currentMethod, inheritedMethod);
//					this.problemReporter(currentMethod).nameClash(currentMethod, inheritedMethod);
					continue nextMethod;
				}
			}
		}

		if (addBridgeMethod || inheritedMethod.hasSubstitutedParameters()) {
		    MethodBinding original = inheritedMethod.original();
		    if (!isReturnTypeSubstituable(original, currentMethod) || !areParametersEqual(original, currentMethod))
				this.type.addSyntheticBridgeMethod(original, currentMethod);
		}

		if (currentMethod.thrownExceptions != NoExceptions)
			this.checkExceptions(currentMethod, inheritedMethod);
		if (inheritedMethod.isFinal())
			this.problemReporter(currentMethod).finalMethodCannotBeOverridden(currentMethod, inheritedMethod);
		if (!this.isAsVisible(currentMethod, inheritedMethod))
			this.problemReporter(currentMethod).visibilityConflict(currentMethod, inheritedMethod);
		if (environment.options.reportDeprecationWhenOverridingDeprecatedMethod && inheritedMethod.isViewedAsDeprecated()) {
			if (!currentMethod.isViewedAsDeprecated() || environment.options.reportDeprecationInsideDeprecatedCode) {
				// check against the other inherited methods to see if they hide this inheritedMethod
				ReferenceBinding declaringClass = inheritedMethod.declaringClass;
				if (declaringClass.isInterface())
					for (int j = length; --j >= 0;)
						if (i != j && methods[j].declaringClass.implementsInterface(declaringClass, false))
							continue nextMethod;

				this.problemReporter(currentMethod).overridesDeprecatedMethod(currentMethod, inheritedMethod);
			}
		}
	}
}
boolean isReturnTypeSubstituable(MethodBinding one, MethodBinding two) {
	if (one.returnType == two.returnType) return true;

	return isTypeSubstituable(one.returnType.erasure(), two.returnType.erasure());
}
boolean isTypeSubstituable(TypeBinding one, TypeBinding two) {
	if (one == two) return true;
	if (one.isArrayType() || two.isArrayType()) {
		if (one.isArrayType() != two.isArrayType()) return false;
		ArrayBinding arrayOne = (ArrayBinding) one;
		ArrayBinding arrayTwo = (ArrayBinding) two;
		if (arrayOne.dimensions != arrayTwo.dimensions) return false;
		one = arrayOne.leafComponentType;
		two = arrayTwo.leafComponentType;
	}
	if (one.isBaseType() || two.isBaseType()) return false;

	ReferenceBinding subType = (ReferenceBinding) one;
	ReferenceBinding superType = (ReferenceBinding) two;
	if (CharOperation.equals(subType.compoundName, superType.compoundName)) return true;

	// TODO what about unresolved types?
	if (superType.isInterface())
		return subType.implementsInterface(superType, true);
	return subType.isClass() && isSameClassOrSubclassOf(subType, superType);
}
}
