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

import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

class MethodVerifier15 extends MethodVerifier {

MethodVerifier15(LookupEnvironment environment) {
	super(environment);
}
boolean areMethodsEqual(MethodBinding one, MethodBinding substituteTwo) {
	return areParametersEqual(one, substituteTwo) && !doTypeVariablesClash(one, substituteTwo);
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
			if (oneArgs[i].isRawType() && !one.declaringClass.isInterface() && oneArgs[i].isEquivalentTo(twoArgs[i]))
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
	return this.type.superInterfaces() == NoSuperInterfaces;
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
void checkForBridgeMethod(MethodBinding currentMethod, MethodBinding inheritedMethod, MethodBinding[] otherInheritedMethods) {
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
		for (int i = 0, l = otherInheritedMethods.length; i < l; i++) {
			if (otherInheritedMethods[i] != null) {
				MethodBinding otherOriginal = otherInheritedMethods[i].original();
				if (otherOriginal != otherInheritedMethods[i] && detectInheritedMethodClash(originalInherited, otherOriginal))
					return;
			}
		}

		// there is an ordering issue with the comparison in checkMethods
		// its possible that compareTo(X) is walked first & removes Comparable.compareTo(T) from the inherited list before we can compare it to compareTo(Object)
		// its only a problem when the matching inherited method creates a bridge method which collides with an unwalked current method
		//		class X implements Comparable<X> {
		//			public int compareTo(Object o) { return 0; }
		//			public int compareTo(X o) { return 1; }
		//		}
		MethodBinding[] toCheck = (MethodBinding[]) this.currentMethods.get(currentMethod.selector);
		for (int i = 0, l = toCheck.length; i < l; i++)
			if (currentMethod != toCheck[i] && detectNameClash(toCheck[i], inheritedMethod))
				return;
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
		detectInheritedMethodClash(inheritedMethod, otherInheritedMethod);
}

void checkForNameClash(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	// sent from checkMethods() to compare a current method and an inherited method that are not 'equal'

	// error cases:
	//		abstract class AA<E extends Comparable> { abstract void test(E element); }
	//		class A extends AA<Integer> { public void test(Integer i) {} }
	//		public class B extends A { public void test(Comparable i) {} }
	// AND
	//		interface I<E extends Comparable> { void test(E element); }
	//		class A implements I<Integer> { public void test(Integer i) {} }
	//		public class B extends A { public void test(Comparable i) {} }
	// AND
	//		abstract class Y implements EqualityComparable<Integer>, Equivalent<String> {
	//			public boolean equalTo(Integer other) { return true; }
	//		}
	//		interface Equivalent<T> { boolean equalTo(T other); }
	//		interface EqualityComparable<T> { boolean equalTo(T other); }
	// AND
	//		class Y implements EqualityComparable, Equivalent<String>{
	//			public boolean equalTo(String other) { return true; }
	//			public boolean equalTo(Object other) { return true; }
	//		}
	//		interface Equivalent<T> { boolean equalTo(T other); }
	//		interface EqualityComparable { boolean equalTo(Object other); }

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
		ReferenceBinding superType = this.type.superclass;
		while (superType != null && superType.isValidBinding()) {
			MethodBinding[] methods = superType.getMethods(currentMethod.selector);
			for (int m = 0, n = methods.length; m < n; m++)
				if (!areMethodsEqual(currentMethod, methods[m]) && detectNameClash(currentMethod, methods[m]))
					return;
			if ((itsInterfaces = superType.superInterfaces()) != NoSuperInterfaces) {
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
					for (int m = 0, n = methods.length; m < n; m++)
						if (!areMethodsEqual(currentMethod, methods[m]) && detectNameClash(currentMethod, methods[m]))
							return;
					if ((itsInterfaces = superType.superInterfaces()) != NoSuperInterfaces) {
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
	    if (inheritedTypeVariables[i].boundCheck(substitute, arguments[i]) != TypeConstants.OK)
	    	return inheritedMethod; // incompatible due to bound check
   return substitute;
}
boolean detectInheritedMethodClash(MethodBinding inherited, MethodBinding otherInherited) {
	if (!inherited.areParameterErasuresEqual(otherInherited) || inherited.returnType.erasure() != otherInherited.returnType.erasure()) return false;
	if (doTypeVariablesClash(inherited, otherInherited) || doParametersClash(inherited, otherInherited)) {
		problemReporter().inheritedMethodsHaveNameClash(this.type, inherited, otherInherited);
		return true;
	}
	return false;
}
boolean detectNameClash(MethodBinding current, MethodBinding inherited) {
	MethodBinding original = inherited.original(); // can be the same as inherited
	if (!current.areParameterErasuresEqual(original) || current.returnType.erasure() != original.returnType.erasure()) return false;
	if (doTypeVariablesClash(current, inherited) || doParametersClash(current, original)) {
		problemReporter(current).methodNameClash(current, original);
		return true;
	}
	return false;
}
public boolean doesMethodOverride(MethodBinding one, MethodBinding two) {
	return super.doesMethodOverride(one, computeSubstituteMethod(two, one));
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
	return one.typeVariables != NoTypeVariables && !one.areTypeVariableErasuresEqual(substituteTwo.original());
}
boolean isInterfaceMethodImplemented(MethodBinding inheritedMethod, MethodBinding existingMethod, ReferenceBinding superType) {
	inheritedMethod = computeSubstituteMethod(inheritedMethod, existingMethod);
	return inheritedMethod.returnType == existingMethod.returnType
		&& super.isInterfaceMethodImplemented(inheritedMethod, existingMethod, superType);
}
boolean mustImplementAbstractMethod(ReferenceBinding declaringClass) {
	if (!this.type.isEnum())
		return super.mustImplementAbstractMethod(declaringClass);
	if (this.type.isAnonymousType())
		return true; // body of enum constant must implement any inherited abstract methods
	if (this.type.isAbstract())
		return false; // is an enum that has since been tagged as abstract by the code below

	// enum type needs to implement abstract methods if one of its constants does not supply a body
	TypeDeclaration typeDeclaration = this.type.scope.referenceContext;
	FieldDeclaration[] fields = typeDeclaration.fields;
	int length = typeDeclaration.fields == null ? 0 : typeDeclaration.fields.length;
	if (length == 0) return true; // has no constants so must implement the method itself
	for (int i = 0; i < length; i++) {
		FieldDeclaration fieldDecl = fields[i];
		if (fieldDecl.getKind() == AbstractVariableDeclaration.ENUM_CONSTANT)
			if (!(fieldDecl.initialization instanceof QualifiedAllocationExpression))
				return true;
	}

	// tag this enum as abstract since an abstract method must be implemented AND all enum constants define an anonymous body
	// as a result, each of its anonymous constants will see it as abstract and must implement each inherited abstract method
	this.type.modifiers |= IConstants.AccAbstract;
	return false;
}
void verify(SourceTypeBinding someType) {
	if (someType.isAnnotationType())
		someType.detectAnnotationCycle();

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
