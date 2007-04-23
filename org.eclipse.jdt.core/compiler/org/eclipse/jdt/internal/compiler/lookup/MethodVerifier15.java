/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;

class MethodVerifier15 extends MethodVerifier {

MethodVerifier15(LookupEnvironment environment) {
	super(environment);
}
boolean areMethodsCompatible(MethodBinding one, MethodBinding two) {
	MethodBinding sub = computeSubstituteMethod(two, one);
	return sub != null && doesSubstituteMethodOverride(one, sub) && areReturnTypesCompatible(one, sub);
}
boolean areParametersEqual(MethodBinding one, MethodBinding two) {
	TypeBinding[] oneArgs = one.parameters;
	TypeBinding[] twoArgs = two.parameters;
	if (oneArgs == twoArgs) return true;

	int length = oneArgs.length;
	if (length != twoArgs.length) return false;

	if (one.declaringClass.isInterface()) {
		for (int i = 0; i < length; i++)
			if (!areTypesEqual(oneArgs[i], twoArgs[i]))
				return false;
	} else {
		// methods with raw parameters are considered equal to inherited methods
		// with parameterized parameters for backwards compatibility, need a more complex check
		int i;
		foundRAW: for (i = 0; i < length; i++) {
			if (!areTypesEqual(oneArgs[i], twoArgs[i])) {
				if (oneArgs[i].leafComponentType().isRawType()) {
					if (oneArgs[i].dimensions() == twoArgs[i].dimensions() && oneArgs[i].leafComponentType().isEquivalentTo(twoArgs[i].leafComponentType())) {
						// one parameter type is raw, hence all parameters types must be raw or non generic
						// otherwise we have a mismatch check backwards
						for (int j = 0; j < i; j++)
							if (oneArgs[j].leafComponentType().isParameterizedType())
								return false;
						// switch to all raw mode
						break foundRAW;
					}
				}
				return false;
			}
		}
		// all raw mode for remaining parameters (if any)
		for (i++; i < length; i++) {
			if (!areTypesEqual(oneArgs[i], twoArgs[i])) {
				if (oneArgs[i].leafComponentType().isRawType())
					if (oneArgs[i].dimensions() == twoArgs[i].dimensions() && oneArgs[i].leafComponentType().isEquivalentTo(twoArgs[i].leafComponentType()))
						continue;
				return false;
			} else if (oneArgs[i].leafComponentType().isParameterizedType()) {
				return false; // no remaining parameter can be a Parameterized type (if one has been converted then all RAW types must be converted)
			}
		}
	}
	return true;
}
boolean areReturnTypesCompatible(MethodBinding one, MethodBinding two) {
	if (one.returnType == two.returnType) return true;
	return areReturnTypesCompatible0(one, two);
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
		// check whether bridge method is already defined above for interface methods
		if (originalInherited.declaringClass.isInterface()
				&& this.type.superclass.erasure().findSuperTypeWithSameErasure(originalInherited.declaringClass) == null) {
			this.type.addSyntheticBridgeMethod(originalInherited, concreteMethod.original());
		}
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
		if (inheritedMethod.returnType.leafComponentType().isParameterizedType() && currentMethod.returnType.leafComponentType().isRawType()) {
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

	if (inheritedMethod.declaringClass.isInterface() || inheritedMethod.isStatic()) return;

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

	if (currentMethod.declaringClass.isInterface() || currentMethod.isStatic()) return;

	if (!detectNameClash(currentMethod, inheritedMethod)) { // check up the hierarchy for skipped inherited methods
		TypeBinding[] currentParams = currentMethod.parameters;
		TypeBinding[] inheritedParams = inheritedMethod.parameters;
		int length = currentParams.length;
		if (length != inheritedParams.length) return; // no match

		for (int i = 0; i < length; i++)
			if (currentParams[i] != inheritedParams[i])
				if (currentParams[i].isBaseType() != inheritedParams[i].isBaseType() || !inheritedParams[i].isCompatibleWith(currentParams[i]))
					return; // no chance that another inherited method's bridge method can collide

		ReferenceBinding[] interfacesToVisit = null;
		int nextPosition = 0;
		ReferenceBinding superType = inheritedMethod.declaringClass;
		ReferenceBinding[] itsInterfaces = superType.superInterfaces();
		if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
			nextPosition = itsInterfaces.length;
			interfacesToVisit = itsInterfaces;
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
				if (interfacesToVisit == null) {
					interfacesToVisit = itsInterfaces;
					nextPosition = interfacesToVisit.length;
				} else {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (next == interfacesToVisit[b]) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}
			superType = superType.superclass();
		}

		for (int i = 0; i < nextPosition; i++) {
			superType = interfacesToVisit[i];
			if (superType.isValidBinding()) {
				MethodBinding[] methods = superType.getMethods(currentMethod.selector);
				for (int m = 0, n = methods.length; m < n; m++){
					MethodBinding substitute = computeSubstituteMethod(methods[m], currentMethod);
					if (substitute != null && !doesSubstituteMethodOverride(currentMethod, substitute) && detectNameClash(currentMethod, substitute))
						return;
				}
				if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
					int itsLength = itsInterfaces.length;
					if (nextPosition + itsLength >= interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
					nextInterface : for (int a = 0; a < itsLength; a++) {
						ReferenceBinding next = itsInterfaces[a];
						for (int b = 0; b < nextPosition; b++)
							if (next == interfacesToVisit[b]) continue nextInterface;
						interfacesToVisit[nextPosition++] = next;
					}
				}
			}
		}
	}
}
void checkInheritedMethods(MethodBinding[] methods, int length) {
	int count = length;
	int[] skip = new int[count];
	nextMethod : for (int i = 0, l = length - 1; i < l; i++) {
		if (skip[i] == -1) continue nextMethod;
		MethodBinding method = methods[i];
		MethodBinding[] duplicates = null;
		for (int j = i + 1; j <= l; j++) {
			MethodBinding method2 = methods[j];
			if (method.declaringClass == method2.declaringClass && areMethodsCompatible(method, method2)) {
				skip[j] = -1;
				if (duplicates == null)
					duplicates = new MethodBinding[length];
				duplicates[j] = method2;
			}
		}
		if (duplicates != null) {
			// found an inherited ParameterizedType that defines duplicate methods
			// if all methods are abstract or more than 1 concrete method exists, then consider them to be duplicates
			// if a single concrete method 'implements' the abstract methods, then do not report a duplicate error
			int concreteCount = method.isAbstract() ? 0 : 1;
			MethodBinding methodToKeep = method; // if a concrete method exists, keep it, otherwise keep the first method
			for (int m = 0, s = duplicates.length; m < s; m++) {
				if (duplicates[m] != null) {
					if (!duplicates[m].isAbstract()) {
						methodToKeep = duplicates[m];
						concreteCount++;
					}
				}
			}
			if (concreteCount != 1) {
				for (int m = 0, s = duplicates.length; m < s; m++) {
					if (duplicates[m] != null) {
						problemReporter().duplicateInheritedMethods(this.type, method, duplicates[m]);
						count--;
						if (methodToKeep == duplicates[m])
							methods[i] = null;
						else
							methods[m] = null;
					}
				}
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
	if (length <= 1) {
		return true; // no need to continue since only 1 inherited method is left
	}
	// get rid of overriden methods coming from interfaces - if any
	MethodBinding methodsToCheck[] = new MethodBinding[length];	// must not nullify methods slots in place
	int count = length;
	for (int i = 0; i < length; i++) {
		methodsToCheck[i] = methods[i];
	}
	for (int i = 0; i < length; i++) {
		MethodBinding existingMethod;
		if ((existingMethod = methodsToCheck[i]) != null) {
			for (int j = 0; j < length; j++) {
				MethodBinding inheritedMethod;
				if (i != j && (inheritedMethod = methodsToCheck[j]) != null &&
						existingMethod.declaringClass.implementsInterface(inheritedMethod.declaringClass, true)) {
					MethodBinding substitute = computeSubstituteMethod(inheritedMethod, existingMethod);
					if (substitute != null && 
							doesSubstituteMethodOverride(existingMethod, substitute) &&
							(existingMethod.returnType.isCompatibleWith(substitute.returnType) ||
									isReturnTypeSubstituable(substitute, existingMethod))) {
						count--;
						methodsToCheck[j] = null;
					}
				}
			}
		}
	}
	if (count < length) {
		if (count == 1) { 
			return true; // no need to continue since only 1 inherited method is left
		}
		for (int i = 0, j = 0; j < count; i++) {
			if (methodsToCheck[i] != null) {
				methodsToCheck[j++] = methodsToCheck[i];
			}
		}
		methods = methodsToCheck;
		length = count;
	} // else keep methods unchanged for further checks

	// its possible in 1.5 that A is compatible with B & C, but B is not compatible with C
	for (int i = 0, l = length - 1; i < l;) {
		MethodBinding method = methods[i++];
		for (int j = i; j <= l; j++) {
			if (!areReturnTypesCompatible(method, methods[j])) {
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
void checkTypeVariableMethods(TypeParameter typeParameter) {
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
				while (--count > 0 && areReturnTypesCompatible(first, matchingInherited[count])){/*empty*/}
				if (count > 0) {  // All inherited methods do NOT have the same vmSignature
					problemReporter().inheritedMethodsHaveIncompatibleReturnTypes(typeParameter, matchingInherited, index + 1);
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
	if (length > 0 && inheritedLength != length) return inheritedMethod; // no match JLS 8.4.2
	TypeBinding[] arguments = new TypeBinding[inheritedLength];
	if (inheritedLength <= length) {
		System.arraycopy(typeVariables, 0, arguments, 0, inheritedLength);
	} else {
		System.arraycopy(typeVariables, 0, arguments, 0, length);
		for (int i = length; i < inheritedLength; i++)
			arguments[i] = inheritedTypeVariables[i].upperBound();
	}
	ParameterizedGenericMethodBinding substitute =
		this.environment.createParameterizedGenericMethod(inheritedMethod, arguments);

	// interface I { <T> void foo(T t); }
	// class X implements I { public <T extends I> void foo(T t) {} }
	// for the above case, we do not want to answer the substitute method since its not a match
	for (int i = 0; i < inheritedLength; i++) {
		TypeVariableBinding inheritedTypeVariable = inheritedTypeVariables[i];
		TypeBinding argument = arguments[i];
		if (argument instanceof TypeVariableBinding) {
			TypeVariableBinding typeVariable = (TypeVariableBinding) argument;
			if (typeVariable.firstBound == inheritedTypeVariable.firstBound) {
				if (typeVariable.firstBound == null)
					continue; // both are null
			} else if (typeVariable.firstBound != null && inheritedTypeVariable.firstBound != null) {
				if (typeVariable.firstBound.isClass() != inheritedTypeVariable.firstBound.isClass())
					return inheritedMethod; // not a match
			}
			if (Scope.substitute(substitute, inheritedTypeVariable.superclass) != typeVariable.superclass)
				return inheritedMethod; // not a match
			int interfaceLength = inheritedTypeVariable.superInterfaces.length;
			ReferenceBinding[] interfaces = typeVariable.superInterfaces;
			if (interfaceLength != interfaces.length)
				return inheritedMethod; // not a match
			// TODO (kent) another place where we expect the superinterfaces to be in the exact same order
			next : for (int j = 0; j < interfaceLength; j++) {
				TypeBinding superType = Scope.substitute(substitute, inheritedTypeVariable.superInterfaces[j]);
				for (int k = 0; k < interfaceLength; k++)
					if (superType == interfaces[k])
						continue next;
				return inheritedMethod; // not a match
			}
		} else if (inheritedTypeVariable.boundCheck(substitute, argument) != TypeConstants.OK) {
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

	problemReporter(current).methodNameClash(current, 
			inherited.declaringClass.isRawType() ? inherited : original);
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

	for (int i = 0; i < length; i++) {
		if (inheritedParams[i].kind() == Binding.TYPE_PARAMETER) {
			if (params[i] != ((TypeVariableBinding) inheritedParams[i]).upperBound())
				return false;
		} else if (params[i] != inheritedParams[i]) {
			return false;			
		}
	}
	return true;
}
boolean doTypeVariablesClash(MethodBinding one, MethodBinding substituteTwo) {
	// one has type variables and substituteTwo did not pass bounds check in computeSubstituteMethod()
	return one.typeVariables != Binding.NO_TYPE_VARIABLES && !(substituteTwo instanceof ParameterizedGenericMethodBinding);
}
// caveat: returns false if a method is implemented but needs that a bridge 
//         method be generated
boolean isInterfaceMethodImplemented(MethodBinding inheritedMethod, MethodBinding existingMethod, ReferenceBinding superType) {
	if (inheritedMethod.original() != inheritedMethod && existingMethod.declaringClass.isInterface())
		return false; // must hold onto ParameterizedMethod to see if a bridge method is necessary

	inheritedMethod = computeSubstituteMethod(inheritedMethod, existingMethod);
	return inheritedMethod != null
		&& inheritedMethod.returnType == existingMethod.returnType
		&& super.isInterfaceMethodImplemented(inheritedMethod, existingMethod, superType);
}
/**
 * Return true iff the return type of existingMethod is a valid replacement for
 * the one of substituteMethod in a method declaration, in the context specified 
 * thereafter. It is expected that substituteMethod is the result of the 
 * substitution of the type parameters of an inheritedMethod method according to 
 * the type parameters of existingMethod and the inheritance relationship 
 * between existingMethod's declaring type and inheritedMethod's declaring type,
 * where inheritedMethod is a method inherited by existingMethod's declaring 
 * type which is override compatible with existingMethod, except maybe for
 * their respective return types. If those conditions are not met, the result is
 * unspecified.
 * @param substituteMethod a proper substitute of a method inherited by existingMethod 
 * @param existingMethod the existing method under examination
 * @return true if the return type of existingMethod is a valid substitute for
 *         the one of substituteMethod
 */
boolean isReturnTypeSubstituable(MethodBinding substituteMethod, MethodBinding existingMethod) {
	class ReturnTypeSubstitution implements Substitution {
		TypeBinding replaced, replacer;
		ReturnTypeSubstitution(TypeBinding replaced, TypeBinding replacer) {
			this.replaced = replaced;
			this.replacer = replacer;
		}
		public LookupEnvironment environment() { 
			return environment; 
		}
		public boolean isRawSubstitution() { 
			return false; 
		}
		public TypeBinding substitute(TypeVariableBinding typeVariable) {
			return typeVariable == replaced ? replacer : typeVariable;
		}
	}
	if (substituteMethod.returnType instanceof TypeVariableBinding) {
		return ((TypeVariableBinding) substituteMethod.returnType).
			boundCheck(
				new ReturnTypeSubstitution(substituteMethod.returnType, existingMethod.returnType),
				existingMethod.returnType)  == TypeConstants.OK;
	} else if (substituteMethod.returnType instanceof ParameterizedTypeBinding) {
		if (! (existingMethod.returnType instanceof ParameterizedTypeBinding)) {
			return false;
		}
		ParameterizedTypeBinding substituteReturnType = (ParameterizedTypeBinding) substituteMethod.returnType,
			existingReturnType = (ParameterizedTypeBinding) existingMethod.returnType;
		if (substituteReturnType.actualType() != existingReturnType.actualType())
			return false;
		for (int i = 0; i < substituteReturnType.arguments.length; i++) {
			TypeBinding substituteArgumentType, existingArgumentType;
			if (! (existingArgumentType = existingReturnType.arguments[i]).isCompatibleWith(
					substituteArgumentType = substituteReturnType.arguments[i])) {
				if (substituteArgumentType instanceof TypeVariableBinding) {
					if (((TypeVariableBinding) substituteArgumentType).
							boundCheck(
								new ReturnTypeSubstitution(substituteArgumentType, existingArgumentType),
								// we do not address the most general pattern of multiple type variables, nor the recursive case either
								existingArgumentType) != TypeConstants.OK) {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		return true;
	}
	return false;
}
SimpleSet findSuperinterfaceCollisions(ReferenceBinding superclass, ReferenceBinding[] superInterfaces) {
	ReferenceBinding[] interfacesToVisit = null;
	int nextPosition = 0;
	ReferenceBinding[] itsInterfaces = superInterfaces;
	if (itsInterfaces != Binding.NO_SUPERINTERFACES) {
		nextPosition = itsInterfaces.length;
		interfacesToVisit = itsInterfaces;
	}

	boolean isInconsistent = this.type.isHierarchyInconsistent();
	ReferenceBinding superType = superclass;
	while (superType != null && superType.isValidBinding()) {
		isInconsistent |= superType.isHierarchyInconsistent();
		if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
			if (interfacesToVisit == null) {
				interfacesToVisit = itsInterfaces;
				nextPosition = interfacesToVisit.length;
			} else {
				int itsLength = itsInterfaces.length;
				if (nextPosition + itsLength >= interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
				nextInterface : for (int a = 0; a < itsLength; a++) {
					ReferenceBinding next = itsInterfaces[a];
					for (int b = 0; b < nextPosition; b++)
						if (next == interfacesToVisit[b]) continue nextInterface;
					interfacesToVisit[nextPosition++] = next;
				}
			}
		}
		superType = superType.superclass();
	}

	for (int i = 0; i < nextPosition; i++) {
		superType = interfacesToVisit[i];
		if (superType.isValidBinding()) {
			isInconsistent |= superType.isHierarchyInconsistent();
			if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
				int itsLength = itsInterfaces.length;
				if (nextPosition + itsLength >= interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
				nextInterface : for (int a = 0; a < itsLength; a++) {
					ReferenceBinding next = itsInterfaces[a];
					for (int b = 0; b < nextPosition; b++)
						if (next == interfacesToVisit[b]) continue nextInterface;
					interfacesToVisit[nextPosition++] = next;
				}
			}
		}
	}

	if (!isInconsistent) return null; // hierarchy is consistent so no collisions are possible
	SimpleSet copy = null;
	for (int i = 0; i < nextPosition; i++) {
		ReferenceBinding current = interfacesToVisit[i];
		if (current.isValidBinding()) {
			TypeBinding erasure = current.erasure();
			for (int j = i + 1; j < nextPosition; j++) {
				ReferenceBinding next = interfacesToVisit[j];
				if (next.isValidBinding() && next.erasure() == erasure) {
					if (copy == null)
						copy = new SimpleSet(nextPosition);
					copy.add(interfacesToVisit[i]);
					copy.add(interfacesToVisit[j]);
				}
			}
		}
	}
	return copy;
}
boolean reportIncompatibleReturnTypeError(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	// JLS 3 §8.4.5: more are accepted, with an unchecked conversion
	if (currentMethod.returnType == inheritedMethod.returnType.erasure()) {
		TypeBinding[] currentParams = currentMethod.parameters;
		TypeBinding[] inheritedParams = inheritedMethod.parameters;
		for (int i = 0, l = currentParams.length; i < l; i++) {
			if (!areTypesEqual(currentParams[i], inheritedParams[i])) {
				problemReporter(currentMethod).unsafeReturnTypeOverride(currentMethod, inheritedMethod, this.type);
				return false;
			}
		}
	}
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
		checkTypeVariableMethods(someType.scope.referenceContext.typeParameters[i]);
	}
}
}
