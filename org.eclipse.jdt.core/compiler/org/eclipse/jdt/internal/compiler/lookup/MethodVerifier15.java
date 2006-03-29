/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

class MethodVerifier15 extends MethodVerifier {

MethodVerifier15(LookupEnvironment environment) {
	super(environment);
}
boolean areMethodsEqual(MethodBinding one, MethodBinding two) {
	MethodBinding sub = computeSubstituteMethod(two, one);
	return sub != null && doesSubstituteMethodOverride(one, sub) && areReturnTypesEqual(one, sub);
}
boolean areParametersEqual(MethodBinding one, MethodBinding two) {
	TypeBinding[] oneArgs = one.parameters;
	TypeBinding[] twoArgs = two.parameters;
	if (oneArgs == twoArgs) return true;

	int length = oneArgs.length;
	if (length != twoArgs.length) return false;

	for (int i = 0; i < length; i++) {
		if (!areTypesEqual(oneArgs[i], twoArgs[i])) {
			// methods with raw parameters are considered equal to inherited methods with parameterized parameters for backwards compatibility
			if (!one.declaringClass.isInterface() && oneArgs[i].leafComponentType().isRawType())
				if (oneArgs[i].dimensions() == twoArgs[i].dimensions() && oneArgs[i].leafComponentType().isEquivalentTo(twoArgs[i].leafComponentType()))
					continue;
			return false;
		}
	}
	return true;
}
boolean areReturnTypesEqual(MethodBinding one, MethodBinding substituteTwo) {
	if (one.returnType == substituteTwo.returnType) return true;

	// short is compatible with int, but as far as covariance is concerned, its not
	if (one.returnType.isBaseType()) return false;

	if (!one.declaringClass.isInterface()) {
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

	// need to consider X<?> and X<? extends Object> as the same 'type'
	if (one.isParameterizedType() && two.isParameterizedType())
		return one.isEquivalentTo(two) && two.isEquivalentTo(one);

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
	return this.type.superInterfaces() == Binding.NO_SUPERINTERFACES;
}
boolean canSkipInheritedMethods(MethodBinding one, MethodBinding two) {
	return two == null // already know one is not null
		|| (one.declaringClass == two.declaringClass && !one.declaringClass.isParameterizedType());
}
void checkConcreteInheritedMethod(MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
	super.checkConcreteInheritedMethod(concreteMethod, abstractMethods);

	for (int i = 0, l = abstractMethods.length; i < l; i++) {
		MethodBinding abstractMethod = abstractMethods[i];
		if (concreteMethod.isVarargs() != abstractMethod.isVarargs())
			problemReporter().varargsConflict(concreteMethod, abstractMethod, this.type);

		// so the parameters are equal and the return type is compatible b/w the currentMethod & the substituted inheritedMethod
		MethodBinding originalInherited = abstractMethod.original();
		if (originalInherited.returnType != concreteMethod.returnType) {
			if (abstractMethod.returnType.leafComponentType().isParameterizedType()) {
				if (concreteMethod.returnType.leafComponentType().isRawType())
					problemReporter().unsafeReturnTypeOverride(concreteMethod, originalInherited, this.type);
			} else if (abstractMethod.hasSubstitutedReturnType() && originalInherited.returnType.leafComponentType().isTypeVariable()) {
				if (((TypeVariableBinding) originalInherited.returnType.leafComponentType()).declaringElement == originalInherited) { // see 81618 - type variable from inherited method
					TypeBinding currentReturnType = concreteMethod.returnType.leafComponentType();
					if (!currentReturnType.isTypeVariable() || ((TypeVariableBinding) currentReturnType).declaringElement != concreteMethod)
						problemReporter().unsafeReturnTypeOverride(concreteMethod, originalInherited, this.type);
				}
			}
		}

		this.type.addSyntheticBridgeMethod(originalInherited, concreteMethod.original());
	}
}
void checkForBridgeMethod(MethodBinding currentMethod, MethodBinding inheritedMethod, MethodBinding[] allInheritedMethods) {
	if (currentMethod.isVarargs() != inheritedMethod.isVarargs())
		problemReporter(currentMethod).varargsConflict(currentMethod, inheritedMethod, this.type);

	// so the parameters are equal and the return type is compatible b/w the currentMethod & the substituted inheritedMethod
	MethodBinding originalInherited = inheritedMethod.original();
	if (originalInherited.returnType != currentMethod.returnType) {
//		if (currentMethod.returnType.needsUncheckedConversion(inheritedMethod.returnType)) {
//			problemReporter(currentMethod).unsafeReturnTypeOverride(currentMethod, originalInherited, this.type);
		if (inheritedMethod.returnType.leafComponentType().isParameterizedType()) {
			if (currentMethod.returnType.leafComponentType().isRawType())
				problemReporter(currentMethod).unsafeReturnTypeOverride(currentMethod, originalInherited, this.type);
		} else if (inheritedMethod.hasSubstitutedReturnType() && originalInherited.returnType.leafComponentType().isTypeVariable()) {
			if (((TypeVariableBinding) originalInherited.returnType.leafComponentType()).declaringElement == originalInherited) { // see 81618 - type variable from inherited method
				TypeBinding currentReturnType = currentMethod.returnType.leafComponentType();
				if (!currentReturnType.isTypeVariable() || ((TypeVariableBinding) currentReturnType).declaringElement != currentMethod)
					problemReporter(currentMethod).unsafeReturnTypeOverride(currentMethod, originalInherited, this.type);
			}
		}
	}

	if (this.type.addSyntheticBridgeMethod(originalInherited, currentMethod.original()) != null) {
		for (int i = 0, l = allInheritedMethods.length; i < l; i++) {
			MethodBinding otherInheritedMethod = allInheritedMethods[i];
			MethodBinding otherOriginal = otherInheritedMethod.original();
			if (otherOriginal == originalInherited || otherOriginal == otherInheritedMethod) continue;

			MethodBinding compareMethod = inheritedMethod instanceof ParameterizedGenericMethodBinding
				? ((ParameterizedGenericMethodBinding) inheritedMethod).originalMethod
				: inheritedMethod;
			MethodBinding substitute = computeSubstituteMethod(otherInheritedMethod, compareMethod);
			if (substitute == null || doesSubstituteMethodOverride(compareMethod, substitute))
				continue;
			if (detectInheritedNameClash(originalInherited, otherOriginal))
				return;
		}
	}
}
void checkForInheritedNameClash(MethodBinding inheritedMethod, MethodBinding otherInheritedMethod) {
	// sent from checkMethods() to compare 2 inherited methods that are not 'equal'

	// the 2 inherited methods clash because of a parameterized type overrides a raw type
	//		interface I { void foo(A a); }
	//		class Y { void foo(A<String> a) {} }
	//		abstract class X extends Y implements I { }
	//		class A<T> {}
	// in this case the 2 inherited methods clash because of type variables
	//		interface I { <T, S> void foo(T t); }
	//		class Y { <T> void foo(T t) {} }
	//		abstract class X extends Y implements I {}

	if (!inheritedMethod.declaringClass.isInterface())
		detectInheritedNameClash(inheritedMethod, otherInheritedMethod);
}
void checkForNameClash(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	// sent from checkMethods() to compare a current method and an inherited method that are not 'equal'

	// error cases:
	//		abstract class AA<E extends Comparable> { abstract void test(E element); }
	//		class A extends AA<Integer> { public void test(Integer i) {} }
	//		public class B extends A { public void test(Comparable i) {} }
	//		interface I<E extends Comparable> { void test(E element); }
	//		class A implements I<Integer> { public void test(Integer i) {} }
	//		public class B extends A { public void test(Comparable i) {} }

	//		abstract class Y implements EqualityComparable<Integer>, Equivalent<String> {
	//			public boolean equalTo(Integer other) { return true; }
	//		}
	//		interface Equivalent<T> { boolean equalTo(T other); }
	//		interface EqualityComparable<T> { boolean equalTo(T other); }

	//		class Y implements EqualityComparable, Equivalent<String>{
	//			public boolean equalTo(String other) { return true; }
	//			public boolean equalTo(Object other) { return true; }
	//		}
	//		interface Equivalent<T> { boolean equalTo(T other); }
	//		interface EqualityComparable { boolean equalTo(Object other); }

	//		class A<T extends Number> { void m(T t) {} }
	//		class B<S extends Integer> extends A<S> { void m(S t) {}}
	//		class D extends B<Integer> { void m(Number t) {}    void m(Integer t) {} }

	//		inheritedMethods does not include I.test since A has a valid implementation
	//		interface I<E extends Comparable<E>> { void test(E element); }
	//		class A implements I<Integer> { public void test(Integer i) {} }
	//		class B extends A { public void test(Comparable i) {} }

	if (currentMethod.declaringClass.isInterface()) return;

	if (!detectNameClash(currentMethod, inheritedMethod)) { // check up the hierarchy for skipped inherited methods
		TypeBinding[] currentParams = currentMethod.parameters;
		TypeBinding[] inheritedParams = inheritedMethod.parameters;
		int length = currentParams.length;
		if (length != inheritedParams.length) return; // no match

		for (int i = 0; i < length; i++)
			if (currentParams[i] != inheritedParams[i])
				if (currentParams[i].isBaseType() != inheritedParams[i].isBaseType() || !inheritedParams[i].isCompatibleWith(currentParams[i]))
					return; // no chance that another inherited method's bridge method can collide

		ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[3][];
		int lastPosition = -1;
		ReferenceBinding[] itsInterfaces = null;
		ReferenceBinding superType = inheritedMethod.declaringClass;
		if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
			if (++lastPosition == interfacesToVisit.length)
				System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
			interfacesToVisit[lastPosition] = itsInterfaces;
		}
		superType = superType.superclass(); // now start with its superclass
		while (superType != null && superType.isValidBinding()) {
			MethodBinding[] methods = superType.getMethods(currentMethod.selector);
			for (int m = 0, n = methods.length; m < n; m++) {
				MethodBinding substitute = computeSubstituteMethod(methods[m], currentMethod);
				if (substitute != null && !doesSubstituteMethodOverride(currentMethod, substitute) && detectNameClash(currentMethod, substitute))
					return;
			}
			if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}
			superType = superType.superclass();
		}

		for (int i = 0; i <= lastPosition; i++) {
			ReferenceBinding[] interfaces = interfacesToVisit[i];
			for (int j = 0, l = interfaces.length; j < l; j++) {
				superType = interfaces[j];
				if (superType.isValidBinding()) {
					MethodBinding[] methods = superType.getMethods(currentMethod.selector);
					for (int m = 0, n = methods.length; m < n; m++){
						MethodBinding substitute = computeSubstituteMethod(methods[m], currentMethod);
						if (substitute != null && !doesSubstituteMethodOverride(currentMethod, substitute) && detectNameClash(currentMethod, substitute))
							return;
					}
					if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
						if (++lastPosition == interfacesToVisit.length)
							System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
						interfacesToVisit[lastPosition] = itsInterfaces;
					}
				}
			}
		}
	}
}
void checkInheritedMethods(MethodBinding[] methods, int length) {
	int count = length;
	nextMethod : for (int i = 0, l = length - 1; i < l;) {
		MethodBinding method = methods[i++];
		for (int j = i; j <= l; j++) {
			if (method.declaringClass == methods[j].declaringClass && areMethodsEqual(method, methods[j])) {
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
boolean checkInheritedReturnTypes(MethodBinding[] methods, int length) {
	if (methods[0].declaringClass.isClass())
		return super.checkInheritedReturnTypes(methods, length);

	// its possible in 1.5 that A is compatible with B & C, but B is not compatible with C
	for (int i = 0, l = length - 1; i < l;) {
		MethodBinding method = methods[i++];
		for (int j = i; j <= l; j++) {
			if (!areReturnTypesEqual(method, methods[j])) {
				if (this.type.isInterface())
					for (int m = length; --m >= 0;)
						if (methods[m].declaringClass.id == TypeIds.T_JavaLangObject)
							return false; // do not complain since the super interface already got blamed
				problemReporter().inheritedMethodsHaveIncompatibleReturnTypes(this.type, methods, length);
				return false;
			}
		}
	}
	return true;
}
void checkMethods() {
	boolean mustImplementAbstractMethods = mustImplementAbstractMethods();
	boolean skipInheritedMethods = mustImplementAbstractMethods && canSkipInheritedMethods(); // have a single concrete superclass so only check overridden methods
	char[][] methodSelectors = this.inheritedMethods.keyTable;
	nextSelector : for (int s = methodSelectors.length; --s >= 0;) {
		if (methodSelectors[s] == null) continue nextSelector;

		MethodBinding[] current = (MethodBinding[]) this.currentMethods.get(methodSelectors[s]);
		if (current == null && skipInheritedMethods)
			continue nextSelector;

		MethodBinding[] inherited = (MethodBinding[]) this.inheritedMethods.valueTable[s];
		if (inherited.length == 1 && current == null) { // handle the common case
			if (mustImplementAbstractMethods && inherited[0].isAbstract())
				checkAbstractMethod(inherited[0]);
			continue nextSelector;
		}

		int index = -1;
		MethodBinding[] matchingInherited = new MethodBinding[inherited.length];
		byte[] foundMatch = new byte[inherited.length];
		if (current != null) {
			for (int i = 0, length1 = current.length; i < length1; i++) {
				MethodBinding currentMethod = current[i];
				for (int j = 0, length2 = inherited.length; j < length2; j++) {
					MethodBinding inheritedMethod = computeSubstituteMethod(inherited[j], currentMethod);
					if (inheritedMethod != null) {
						if (foundMatch[j] == 0 && doesSubstituteMethodOverride(currentMethod, inheritedMethod)) {
							matchingInherited[++index] = inheritedMethod;
							foundMatch[j] = 1; // cannot null out inherited methods
						} else {
							checkForNameClash(currentMethod, inheritedMethod);
						}
					}
				}
				if (index >= 0) {
					// see addtional comments in https://bugs.eclipse.org/bugs/show_bug.cgi?id=122881
					// if (index > 0 && currentMethod.declaringClass.isInterface()) // only check when inherited methods are from interfaces
					//	checkInheritedReturnTypes(matchingInherited, index + 1);
					checkAgainstInheritedMethods(currentMethod, matchingInherited, index + 1, inherited); // pass in the length of matching
					while (index >= 0) matchingInherited[index--] = null; // clear the contents of the matching methods
				}
			}
		}

		for (int i = 0, length = inherited.length; i < length; i++) {
			if (foundMatch[i] == 1) continue;

			MethodBinding inheritedMethod = inherited[i];
			matchingInherited[++index] = inheritedMethod;
			for (int j = i + 1; j < length; j++) {
				MethodBinding otherInheritedMethod = inherited[j];
				if (foundMatch[j] == 1 || canSkipInheritedMethods(inheritedMethod, otherInheritedMethod))
					continue;
				otherInheritedMethod = computeSubstituteMethod(otherInheritedMethod, inheritedMethod);
				if (otherInheritedMethod != null) {
					if (doesSubstituteMethodOverride(inheritedMethod, otherInheritedMethod)) {
						matchingInherited[++index] = otherInheritedMethod;
						foundMatch[j] = 1; // cannot null out inherited methods
					} else {
						checkForInheritedNameClash(inheritedMethod, otherInheritedMethod);
					}
				}
			}
			if (index == -1) continue;

			if (index > 0)
				checkInheritedMethods(matchingInherited, index + 1); // pass in the length of matching
			else if (mustImplementAbstractMethods && index == 0 && matchingInherited[0].isAbstract())
				checkAbstractMethod(matchingInherited[0]);
			while (index >= 0) matchingInherited[index--] = null; // clear the previous contents of the matching methods
		}
	}
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
					if (otherInheritedMethod != null && doesSubstituteMethodOverride(inheritedMethod, otherInheritedMethod)) {
						matchingInherited[++index] = otherInheritedMethod;
						inherited[j] = null; // do not want to find it again
					}
				}
			}
			if (index > 0) {
				MethodBinding first = matchingInherited[0];
				int count = index + 1;
				while (--count > 0 && areReturnTypesEqual(first, matchingInherited[count])){/*empty*/}
				if (count > 0) {  // All inherited methods do NOT have the same vmSignature
					problemReporter().inheritedMethodsHaveIncompatibleReturnTypes(this.type, matchingInherited, index + 1);
					continue nextSelector;
				}
			}
		}
	}
}
MethodBinding computeSubstituteMethod(MethodBinding inheritedMethod, MethodBinding currentMethod) {
	if (inheritedMethod == null) return null;
	if (currentMethod.parameters.length != inheritedMethod.parameters.length) return null; // no match

	// due to hierarchy & compatibility checks, we need to ensure these 2 methods are resolved
	if (currentMethod.declaringClass instanceof BinaryTypeBinding)
		((BinaryTypeBinding) currentMethod.declaringClass).resolveTypesFor(currentMethod);
	if (inheritedMethod.declaringClass instanceof BinaryTypeBinding)
		((BinaryTypeBinding) inheritedMethod.declaringClass).resolveTypesFor(inheritedMethod);

	TypeVariableBinding[] inheritedTypeVariables = inheritedMethod.typeVariables;
	if (inheritedTypeVariables == Binding.NO_TYPE_VARIABLES) return inheritedMethod;
	int inheritedLength = inheritedTypeVariables.length;
	TypeVariableBinding[] typeVariables = currentMethod.typeVariables;
	int length = typeVariables.length;
	if (length > 0 && inheritedLength != length) return inheritedMethod;
	TypeBinding[] arguments = new TypeBinding[inheritedLength];
	if (inheritedLength <= length) {
		System.arraycopy(typeVariables, 0, arguments, 0, inheritedLength);
	} else {
		System.arraycopy(typeVariables, 0, arguments, 0, length);
		for (int i = length; i < inheritedLength; i++)
			arguments[i] = inheritedTypeVariables[i].upperBound();
	}
	ParameterizedGenericMethodBinding substitute =
		new ParameterizedGenericMethodBinding(inheritedMethod, arguments, this.environment);

	// interface I { <T> void foo(T t); }
	// class X implements I { public <T extends I> void foo(T t) {} }
	// for the above case, we do not want to answer the substitute method since its not a match
	for (int i = 0; i < inheritedLength; i++) {
		TypeVariableBinding inheritedTypeVariable = inheritedTypeVariables[i];
		TypeBinding argument = arguments[i];
		if (argument instanceof TypeVariableBinding) {
			TypeVariableBinding typeVariable = (TypeVariableBinding) argument;
			if (Scope.substitute(substitute, inheritedTypeVariable.superclass) != typeVariable.superclass)  
				return inheritedMethod; // not a match
			int interfaceLength = inheritedTypeVariable.superInterfaces.length;
			ReferenceBinding[] interfaces = typeVariable.superInterfaces;
			if (interfaceLength != interfaces.length)
				return inheritedMethod; // not a match
			next : for (int j = 0; j < interfaceLength; j++) {
				TypeBinding superType = Scope.substitute(substitute, inheritedTypeVariable.superInterfaces[j]);
				for (int k = 0; k < interfaceLength; k++)
					if (superType == interfaces[k])
						continue next;
				return inheritedMethod; // not a match
			}
		} else if (inheritedTypeVariables[i].boundCheck(substitute, argument) != TypeConstants.OK) {
	    	return inheritedMethod;
		}
	}
   return substitute;
}
boolean detectInheritedNameClash(MethodBinding inherited, MethodBinding otherInherited) {
	if (!inherited.areParameterErasuresEqual(otherInherited) || inherited.returnType.erasure() != otherInherited.returnType.erasure()) return false;

	problemReporter().inheritedMethodsHaveNameClash(this.type, inherited, otherInherited);
	return true;
}
boolean detectNameClash(MethodBinding current, MethodBinding inherited) {
	MethodBinding original = inherited.original(); // can be the same as inherited
	if (!current.areParameterErasuresEqual(original) || current.returnType.erasure() != original.returnType.erasure()) return false;

	problemReporter(current).methodNameClash(current, original);
	return true;
}
public boolean doesMethodOverride(MethodBinding method, MethodBinding inheritedMethod) {
	MethodBinding substitute = computeSubstituteMethod(inheritedMethod, method);
	return substitute != null && doesSubstituteMethodOverride(method, substitute);
}
boolean doesSubstituteMethodOverride(MethodBinding method, MethodBinding substituteMethod) {
	if (doTypeVariablesClash(method, substituteMethod)) return false;
	if (areParametersEqual(method, substituteMethod)) return true;
	if (method.declaringClass == substituteMethod.declaringClass) return false;

	TypeBinding[] params = method.parameters;
	TypeBinding[] inheritedParams = substituteMethod.parameters;
	int length = params.length;
	if (length != inheritedParams.length)
		return false;

	// also allow a method such as Number foo(Number) to override <U> T foo(T) where T extends Number
	if (method.typeVariables != Binding.NO_TYPE_VARIABLES || !substituteMethod.hasSubstitutedParameters())
		return false;
	if (method.declaringClass.findSuperTypeWithSameErasure(substituteMethod.declaringClass) == null)
		return false;

	for (int i = 0; i < length; i++)
		if (params[i] != inheritedParams[i].erasure())
			return false;
	return true;
}
boolean doTypeVariablesClash(MethodBinding one, MethodBinding substituteTwo) {
	// one has type variables and substituteTwo did not pass bounds check in computeSubstituteMethod()
	return one.typeVariables != Binding.NO_TYPE_VARIABLES && !(substituteTwo instanceof ParameterizedGenericMethodBinding);
}
boolean isInterfaceMethodImplemented(MethodBinding inheritedMethod, MethodBinding existingMethod, ReferenceBinding superType) {
	if (inheritedMethod.original() != inheritedMethod && existingMethod.declaringClass.isInterface())
		return false; // must hold onto ParameterizedMethod to see if a bridge method is necessary

	inheritedMethod = computeSubstituteMethod(inheritedMethod, existingMethod);
	return inheritedMethod != null
		&& inheritedMethod.returnType == existingMethod.returnType
		&& super.isInterfaceMethodImplemented(inheritedMethod, existingMethod, superType);
}
boolean reportIncompatibleReturnTypeError(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	if (currentMethod.typeVariables == Binding.NO_TYPE_VARIABLES
		&& inheritedMethod.original().typeVariables != Binding.NO_TYPE_VARIABLES
		&& currentMethod.returnType.erasure().findSuperTypeWithSameErasure(inheritedMethod.returnType.erasure()) != null) {
			problemReporter(currentMethod).unsafeReturnTypeOverride(currentMethod, inheritedMethod, this.type);
			return false;
	}
	return super.reportIncompatibleReturnTypeError(currentMethod, inheritedMethod);
}
void verify(SourceTypeBinding someType) {
	if (someType.isAnnotationType())
		someType.detectAnnotationCycle();

	super.verify(someType);

	for (int i = someType.typeVariables.length; --i >= 0;) {
		TypeVariableBinding var = someType.typeVariables[i];
		// must verify bounds if the variable has more than 1
		if (var.superInterfaces == Binding.NO_SUPERINTERFACES) continue;
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
