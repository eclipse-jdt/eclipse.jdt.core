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
boolean areTypesEqual(TypeBinding one, TypeBinding two) {
	return one == two || super.areTypesEqual(one.erasure(), two.erasure());
}
void checkAgainstInheritedMethods(MethodBinding currentMethod, MethodBinding[] methods, int length) {
	// methods includes the inherited methods that the currentMethod must comply with
	// likely only 1 but could be more if mutiple declared supertypes define the method (1 superclass & 1 to many declared interfaces)
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

		boolean addBridgeMethod = inheritedMethod.hasSubstitutedReturnType();
		if (!super.areTypesEqual(currentMethod.returnType, inheritedMethod.returnType)) {
			// can be [] of Class#RAW vs. Class<T>
			if (!isReturnTypeSubstituable(currentMethod, inheritedMethod)) {
				this.problemReporter(currentMethod).incompatibleReturnType(currentMethod, inheritedMethod);
				continue nextMethod;
			} else if (inheritedMethod.typeVariables.length != currentMethod.typeVariables.length) {
				this.problemReporter(currentMethod).incompatibleReturnType(currentMethod, inheritedMethod);
//				this.problemReporter(currentMethod).nameClash(currentMethod, inheritedMethod);
				continue nextMethod;
			}
			addBridgeMethod = true;
		}

		if (addBridgeMethod || inheritedMethod.hasSubstitutedParameters()) {
		    MethodBinding original = inheritedMethod.original();
		    if (!areReturnTypesEqual(original, currentMethod) || !areParametersEqual(original, currentMethod))
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
boolean doesMethodOverride(MethodBinding method, MethodBinding inheritedMethod) {
	return areParametersEqual(method, inheritedMethod) && isReturnTypeSubstituable(method, inheritedMethod);
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

	superType = BinaryTypeBinding.resolveType(superType, this.environment, true);
	subType = BinaryTypeBinding.resolveType(subType, this.environment, true);
	if (superType.isInterface())
		return subType.implementsInterface(superType, true);
	return subType.isClass() && isSameClassOrSubclassOf(subType, superType);
}
}
