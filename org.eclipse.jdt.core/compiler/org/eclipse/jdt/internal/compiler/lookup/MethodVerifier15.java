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

import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

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
				if (!areTypesEqual(oneParams[i], twoParams[i])) {
					if (one.areParameterErasuresEqual(substituteTwo)) // at least one parameter may cause a name clash
						detectNameClash(one, substituteTwo, true);
					return false; // no match but needed to check for a name clash
				}
				checkParameters = true;
			}
		}
	}
	return !detectNameClash(one, substituteTwo, checkParameters);
}
boolean areReturnTypesEqual(MethodBinding one, MethodBinding substituteTwo) {
	if (one.returnType == substituteTwo.returnType) return true;

	// short is compatible with int, but as far as covariance is concerned, its not
	if (one.returnType.isBaseType()) return false;

	if (one.declaringClass.isClass()) {
		if (one.declaringClass.id == TypeIds.T_JavaLangObject)
			return substituteTwo.returnType.isCompatibleWith(one.returnType); // interface methods inherit from Object
		return one.returnType.isCompatibleWith(substituteTwo.returnType);
	}

	// check for methods from Object, every interface inherits from Object
	if (substituteTwo.declaringClass.id == TypeIds.T_JavaLangObject)
		return one.returnType.isCompatibleWith(substituteTwo.returnType);

	// both are interfaces, see if they're related
	if (one.declaringClass.implementsInterface(substituteTwo.declaringClass, true))
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
void checkTypeVariableMethods() {
	char[][] methodSelectors = this.inheritedMethods.keyTable;
	nextSelector : for (int s = methodSelectors.length; --s >= 0;) {
		if (methodSelectors[s] == null) continue nextSelector;
		MethodBinding[] inherited = (MethodBinding[]) this.inheritedMethods.valueTable[s];
		if (inherited.length == 1) continue nextSelector;

		int index = -1;
		MethodBinding[] matchingInherited = new MethodBinding[inherited.length];
		for (int i = 0, length = inherited.length; i < length; i++) {
			while (index >= 0) matchingInherited[index--] = null; // clear the previous contents of the matching methods
			MethodBinding inheritedMethod = inherited[i];
			if (inheritedMethod != null) {
				matchingInherited[++index] = inheritedMethod;
				for (int j = i + 1; j < length; j++) {
					MethodBinding otherInheritedMethod = inherited[j];
					if (canSkipInheritedMethods(inheritedMethod, otherInheritedMethod))
						continue;
					otherInheritedMethod = computeSubstituteMethod(otherInheritedMethod, inheritedMethod);
					if (areMethodsEqual(inheritedMethod, otherInheritedMethod)) {
						matchingInherited[++index] = otherInheritedMethod;
						inherited[j] = null; // do not want to find it again
					}
				}
			}
			if (index > 0)
				checkInheritedMethods(matchingInherited, index + 1); // pass in the length of matching
		}
	}
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
		switch (oneParams[i].leafComponentType().kind()) {
			case Binding.PARAMETERIZED_TYPE :
				if (!twoParams[i].leafComponentType().isParameterizedType()
					|| !oneParams[i].isEquivalentTo(twoParams[i])
					|| !twoParams[i].isEquivalentTo(oneParams[i])) {
						return true;
				}
				break;
			case Binding.TYPE_PARAMETER :
				return true; // type variables must be identical (due to substitution) given their erasures are equal
		}
		if (twoParams[i].leafComponentType().isTypeVariable())
			return true; // type variables must be identical (due to substitution) given their erasures are equal
	}
	return false;
}
public boolean doReturnTypesCollide(MethodBinding method, MethodBinding inheritedMethod) {
	MethodBinding sub = computeSubstituteMethod(inheritedMethod, method);
	return org.eclipse.jdt.core.compiler.CharOperation.equals(method.selector, sub.selector)
		&& method.areParameterErasuresEqual(sub)
		&& !areReturnTypesEqual(method, sub);
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
boolean mustImplementAbstractMethods() {
	if (!super.mustImplementAbstractMethods()) return false;
	if (!this.type.isEnum() || this.type.isAnonymousType()) return true;

	// enum type only needs to implement abstract methods if any of its constants does not supply a body
	TypeDeclaration typeDeclaration = this.type.scope.referenceContext;
	for (int i = 0, length = typeDeclaration.fields == null ? 0 : typeDeclaration.fields.length; i < length; i++) {
		FieldDeclaration fieldDecl = typeDeclaration.fields[i];
		if (fieldDecl.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT)
			if (!(fieldDecl.initialization instanceof QualifiedAllocationExpression))
				return true; // leave mustImplementAbstractMethods flag on 
	}
	return false; // since all enum constants define an anonymous body
}
void verify(SourceTypeBinding someType) {
	super.verify(someType);

	for (int i = someType.typeVariables.length; --i >= 0;) {
		TypeVariableBinding var = someType.typeVariables[i];
		// must verify bounds if the variable has more than 1
		if (var.superInterfaces == NoSuperInterfaces) continue;
		if (var.superInterfaces.length == 1 && var.superclass.id == TypeIds.T_JavaLangObject) continue;

		this.currentMethods = new HashtableOfObject(0);
		ReferenceBinding superclass = var.superclass();
		if (superclass.kind() == Binding.TYPE_PARAMETER)
			superclass = (ReferenceBinding) superclass.erasure();
		ReferenceBinding[] itsInterfaces = var.superInterfaces();
		ReferenceBinding[] superInterfaces = new ReferenceBinding[itsInterfaces.length];
		for (int j = itsInterfaces.length; --j >= 0;) {
			superInterfaces[j] = itsInterfaces[j].kind() == Binding.TYPE_PARAMETER
				? (ReferenceBinding) itsInterfaces[j].erasure()
				: itsInterfaces[j];
		}
		computeInheritedMethods(superclass, superInterfaces);
		checkTypeVariableMethods();
	}
}
}