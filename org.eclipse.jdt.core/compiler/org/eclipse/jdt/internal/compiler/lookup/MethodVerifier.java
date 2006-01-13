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

import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

public class MethodVerifier implements TagBits, TypeConstants {
	SourceTypeBinding type;
	HashtableOfObject inheritedMethods;
	HashtableOfObject currentMethods;
	ReferenceBinding runtimeException;
	ReferenceBinding errorException;
	LookupEnvironment environment;
/*
Binding creation is responsible for reporting all problems with types:
	- all modifier problems (duplicates & multiple visibility modifiers + incompatible combinations - abstract/final)
		- plus invalid modifiers given the context (the verifier did not do this before)
	- qualified name collisions between a type and a package (types in default packages are excluded)
	- all type hierarchy problems:
		- cycles in the superclass or superinterface hierarchy
		- an ambiguous, invisible or missing superclass or superinterface
		- extending a final class
		- extending an interface instead of a class
		- implementing a class instead of an interface
		- implementing the same interface more than once (ie. duplicate interfaces)
	- with nested types:
		- shadowing an enclosing type's source name
		- defining a static class or interface inside a non-static nested class
		- defining an interface as a local type (local types can only be classes)
*/
MethodVerifier(LookupEnvironment environment) {
	this.type = null;  // Initialized with the public method verify(SourceTypeBinding)
	this.inheritedMethods = null;
	this.currentMethods = null;
	this.runtimeException = null;
	this.errorException = null;
	this.environment = environment;
}
boolean areMethodsEqual(MethodBinding one, MethodBinding two) {
	return doesMethodOverride(one, two) && areReturnTypesEqual(one, two);
}
boolean areParametersEqual(MethodBinding one, MethodBinding two) {
	TypeBinding[] oneArgs = one.parameters;
	TypeBinding[] twoArgs = two.parameters;
	if (oneArgs == twoArgs) return true;

	int length = oneArgs.length;
	if (length != twoArgs.length) return false;

	for (int i = 0; i < length; i++)
		if (!areTypesEqual(oneArgs[i], twoArgs[i])) return false;
	return true;
}
boolean areReturnTypesEqual(MethodBinding one, MethodBinding two) {
	return areTypesEqual(one.returnType, two.returnType);
}
boolean areTypesEqual(TypeBinding one, TypeBinding two) {
	if (one == two) return true;

	// its possible that an UnresolvedReferenceBinding can be compared to its resolved type
	// when they're both UnresolvedReferenceBindings then they must be identical like all other types
	// all wrappers of UnresolvedReferenceBindings are converted as soon as the type is resolved
	// so its not possible to have 2 arrays where one is UnresolvedX[] and the other is X[]
	if (one instanceof UnresolvedReferenceBinding)
		return ((UnresolvedReferenceBinding) one).resolvedType == two;
	if (two instanceof UnresolvedReferenceBinding)
		return ((UnresolvedReferenceBinding) two).resolvedType == one;
	return false; // all other type bindings are identical
}
boolean canSkipInheritedMethods() {
	if (this.type.superclass() != null && this.type.superclass().isAbstract())
		return false;
	return this.type.superInterfaces() == Binding.NO_SUPERINTERFACES;
}
boolean canSkipInheritedMethods(MethodBinding one, MethodBinding two) {
	return two == null // already know one is not null
		|| one.declaringClass == two.declaringClass;
}
void checkAbstractMethod(MethodBinding abstractMethod) {
	if (mustImplementAbstractMethod(abstractMethod.declaringClass)) {
		TypeDeclaration typeDeclaration = this.type.scope.referenceContext;
		if (typeDeclaration != null) {
			MethodDeclaration missingAbstractMethod = typeDeclaration.addMissingAbstractMethodFor(abstractMethod);
			missingAbstractMethod.scope.problemReporter().abstractMethodMustBeImplemented(this.type, abstractMethod);
		} else {
			problemReporter().abstractMethodMustBeImplemented(this.type, abstractMethod);
		}
	}
}
void checkAgainstInheritedMethods(MethodBinding currentMethod, MethodBinding[] methods, int length, MethodBinding[] allInheritedMethods) {
	if (this.type.isAnnotationType()) { // annotation cannot override any method
		problemReporter().annotationCannotOverrideMethod(currentMethod, methods[length - 1]);
		return; // do not repoort against subsequent inherited methods
	}
	CompilerOptions options = type.scope.compilerOptions();
	nextMethod : for (int i = length; --i >= 0;) {
		MethodBinding inheritedMethod = methods[i];
		if (currentMethod.isStatic() != inheritedMethod.isStatic()) {  // Cannot override a static method or hide an instance method
			problemReporter(currentMethod).staticAndInstanceConflict(currentMethod, inheritedMethod);
			continue nextMethod;
		}

		// want to tag currentMethod even if return types are not equal
		if (inheritedMethod.isAbstract()) {
			if (inheritedMethod.declaringClass.isInterface()) {
				currentMethod.modifiers |= ExtraCompilerModifiers.AccImplementing;
			} else {
				currentMethod.modifiers |= ExtraCompilerModifiers.AccImplementing | ExtraCompilerModifiers.AccOverriding;
			}
// with the above change an abstract method is tagged as implementing the inherited abstract method
//		if (!currentMethod.isAbstract() && inheritedMethod.isAbstract()) {
//			if ((currentMethod.modifiers & CompilerModifiers.AccOverriding) == 0)
//				currentMethod.modifiers |= CompilerModifiers.AccImplementing;
		} else {
			currentMethod.modifiers |= ExtraCompilerModifiers.AccOverriding;
		}

		if (!areReturnTypesEqual(currentMethod, inheritedMethod)) {
			problemReporter(currentMethod).incompatibleReturnType(currentMethod, inheritedMethod);
			continue nextMethod;
		}

		if (currentMethod.thrownExceptions != Binding.NO_EXCEPTIONS)
			checkExceptions(currentMethod, inheritedMethod);
		if (inheritedMethod.isFinal())
			problemReporter(currentMethod).finalMethodCannotBeOverridden(currentMethod, inheritedMethod);
		if (!isAsVisible(currentMethod, inheritedMethod))
			problemReporter(currentMethod).visibilityConflict(currentMethod, inheritedMethod);
		if (options.reportDeprecationWhenOverridingDeprecatedMethod && inheritedMethod.isViewedAsDeprecated()) {
			if (!currentMethod.isViewedAsDeprecated() || options.reportDeprecationInsideDeprecatedCode) {
				// check against the other inherited methods to see if they hide this inheritedMethod
				ReferenceBinding declaringClass = inheritedMethod.declaringClass;
				if (declaringClass.isInterface())
					for (int j = length; --j >= 0;)
						if (i != j && methods[j].declaringClass.implementsInterface(declaringClass, false))
							continue nextMethod;

				problemReporter(currentMethod).overridesDeprecatedMethod(currentMethod, inheritedMethod);
			}
		}
		checkForBridgeMethod(currentMethod, inheritedMethod, allInheritedMethods);
	}
}
void checkConcreteInheritedMethod(MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
	// Remember that interfaces can only define public instance methods
	if (concreteMethod.isStatic())
		// Cannot inherit a static method which is specified as an instance method by an interface
		problemReporter().staticInheritedMethodConflicts(type, concreteMethod, abstractMethods);	
	if (!concreteMethod.isPublic())
		// Cannot reduce visibility of a public method specified by an interface
		problemReporter().inheritedMethodReducesVisibility(type, concreteMethod, abstractMethods);
	if (concreteMethod.thrownExceptions != Binding.NO_EXCEPTIONS)
		for (int i = abstractMethods.length; --i >= 0;)
			checkExceptions(concreteMethod, abstractMethods[i]);
}
/*
"8.4.4"
Verify that newExceptions are all included in inheritedExceptions.
Assumes all exceptions are valid and throwable.
Unchecked exceptions (compatible with runtime & error) are ignored (see the spec on pg. 203).
*/
void checkExceptions(MethodBinding newMethod, MethodBinding inheritedMethod) {
	ReferenceBinding[] newExceptions = resolvedExceptionTypesFor(newMethod);
	ReferenceBinding[] inheritedExceptions = resolvedExceptionTypesFor(inheritedMethod);
	for (int i = newExceptions.length; --i >= 0;) {
		ReferenceBinding newException = newExceptions[i];
		int j = inheritedExceptions.length;
		while (--j > -1 && !isSameClassOrSubclassOf(newException, inheritedExceptions[j])){/*empty*/}
		if (j == -1)
			if (!newException.isUncheckedException(false))
				problemReporter(newMethod).incompatibleExceptionInThrowsClause(this.type, newMethod, inheritedMethod, newException);
	}
}
void checkForBridgeMethod(MethodBinding currentMethod, MethodBinding inheritedMethod, MethodBinding[] allInheritedMethods) {
	// no op before 1.5
}
void checkInheritedMethods(MethodBinding[] methods, int length) {
	if (!checkInheritedReturnTypes(methods, length))
		return;

	MethodBinding concreteMethod = null;
	if (!type.isInterface()) {  // ignore concrete methods for interfaces
		for (int i = length; --i >= 0;) {  // Remember that only one of the methods can be non-abstract
			if (!methods[i].isAbstract()) {
				concreteMethod = methods[i];
				break;
			}
		}
	}
	if (concreteMethod == null) {
		if (!this.type.isAbstract()) {
			for (int i = length; --i >= 0;) {
				if (mustImplementAbstractMethod(methods[i].declaringClass)) {
					TypeDeclaration typeDeclaration = this.type.scope.referenceContext;
					if (typeDeclaration != null) {
						MethodDeclaration missingAbstractMethod = typeDeclaration.addMissingAbstractMethodFor(methods[0]);
						missingAbstractMethod.scope.problemReporter().abstractMethodMustBeImplemented(this.type, methods[0]);
					} else {
						problemReporter().abstractMethodMustBeImplemented(this.type, methods[0]);
					}
					return;
				}
			}
		}
		return;
	}

	MethodBinding[] abstractMethods = new MethodBinding[length - 1];
	int index = 0;
	for (int i = length; --i >= 0;)
		if (methods[i] != concreteMethod)
			abstractMethods[index++] = methods[i];
	checkConcreteInheritedMethod(concreteMethod, abstractMethods);
}
boolean checkInheritedReturnTypes(MethodBinding[] methods, int length) {
	MethodBinding first = methods[0];
	int index = length;
	while (--index > 0 && areReturnTypesEqual(first, methods[index])){/*empty*/}
	if (index == 0) 
		return true;

	// All inherited methods do NOT have the same vmSignature
	if (this.type.isInterface())
		for (int i = length; --i >= 0;)
			if (methods[i].declaringClass.id == TypeIds.T_JavaLangObject)
				return false; // do not complain since the super interface already got blamed
	problemReporter().inheritedMethodsHaveIncompatibleReturnTypes(this.type, methods, length);
	return false;
}
/*
For each inherited method identifier (message pattern - vm signature minus the return type)
	if current method exists
		if current's vm signature does not match an inherited signature then complain 
		else compare current's exceptions & visibility against each inherited method
	else
		if inherited methods = 1
			if inherited is abstract && type is NOT an interface or abstract, complain
		else
			if vm signatures do not match complain
			else
				find the concrete implementation amongst the abstract methods (can only be 1)
				if one exists then
					it must be a public instance method
					compare concrete's exceptions against each abstract method
				else
					complain about missing implementation only if type is NOT an interface or abstract
*/
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
		if (current != null) {
			for (int i = 0, length1 = current.length; i < length1; i++) {
				MethodBinding currentMethod = current[i];
				for (int j = 0, length2 = inherited.length; j < length2; j++) {
					MethodBinding inheritedMethod = computeSubstituteMethod(inherited[j], currentMethod);
					if (inheritedMethod != null) {
						if (doesMethodOverride(currentMethod, inheritedMethod)) {
							matchingInherited[++index] = inheritedMethod;
							inherited[j] = null; // do not want to find it again
						}
					}
				}
				if (index >= 0) {
					checkAgainstInheritedMethods(currentMethod, matchingInherited, index + 1, inherited); // pass in the length of matching
					while (index >= 0) matchingInherited[index--] = null; // clear the contents of the matching methods
				}
			}
		}

		for (int i = 0, length = inherited.length; i < length; i++) {
			MethodBinding inheritedMethod = inherited[i];
			if (inheritedMethod == null) continue;

			matchingInherited[++index] = inheritedMethod;
			for (int j = i + 1; j < length; j++) {
				MethodBinding otherInheritedMethod = inherited[j];
				if (canSkipInheritedMethods(inheritedMethod, otherInheritedMethod))
					continue;
				otherInheritedMethod = computeSubstituteMethod(otherInheritedMethod, inheritedMethod);
				if (otherInheritedMethod != null) {
					if (doesMethodOverride(inheritedMethod, otherInheritedMethod)) {
						matchingInherited[++index] = otherInheritedMethod;
						inherited[j] = null; // do not want to find it again
					}
				}
			}
			if (index == -1) continue;
			if (index > 0)
				checkInheritedMethods(matchingInherited, index + 1); // pass in the length of matching
			else if (mustImplementAbstractMethods && matchingInherited[0].isAbstract())
				checkAbstractMethod(matchingInherited[0]);
			while (index >= 0) matchingInherited[index--] = null; // clear the contents of the matching methods
		}
	}
}
void checkPackagePrivateAbstractMethod(MethodBinding abstractMethod) {
	// check that the inherited abstract method (package private visibility) is implemented within the same package
	PackageBinding necessaryPackage = abstractMethod.declaringClass.fPackage;
	if (necessaryPackage == this.type.fPackage) return; // not a problem

	ReferenceBinding superType = this.type.superclass();
	char[] selector = abstractMethod.selector;
	do {
		if (!superType.isValidBinding()) return;
		if (!superType.isAbstract()) return; // closer non abstract super type will be flagged instead

		if (necessaryPackage == superType.fPackage) {
			MethodBinding[] methods = superType.getMethods(selector);
			nextMethod : for (int m = methods.length; --m >= 0;) {
				MethodBinding method = methods[m];
				if (method.isPrivate() || method.isConstructor() || method.isDefaultAbstract())
					continue nextMethod;
				if (areMethodsEqual(method, abstractMethod))
					return; // found concrete implementation of abstract method in same package
			}
		}
	} while ((superType = superType.superclass()) != abstractMethod.declaringClass);

	// non visible abstract methods cannot be overridden so the type must be defined abstract
	problemReporter().abstractMethodCannotBeOverridden(this.type, abstractMethod);
}
void computeInheritedMethods() {
	ReferenceBinding superclass = this.type.isInterface()
		? this.type.scope.getJavaLangObject() // check interface methods against Object
		: this.type.superclass(); // class or enum
	computeInheritedMethods(superclass, type.superInterfaces());
}
/*
Binding creation is responsible for reporting:
	- all modifier problems (duplicates & multiple visibility modifiers + incompatible combinations)
		- plus invalid modifiers given the context... examples:
			- interface methods can only be public
			- abstract methods can only be defined by abstract classes
	- collisions... 2 methods with identical vmSelectors
	- multiple methods with the same message pattern but different return types
	- ambiguous, invisible or missing return/argument/exception types
	- check the type of any array is not void
	- check that each exception type is Throwable or a subclass of it
*/
void computeInheritedMethods(ReferenceBinding superclass, ReferenceBinding[] superInterfaces) {
	// only want to remember inheritedMethods that can have an impact on the current type
	// if an inheritedMethod has been 'replaced' by a supertype's method then skip it

	this.inheritedMethods = new HashtableOfObject(51); // maps method selectors to an array of methods... must search to match paramaters & return type
	ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[3][];
	int lastPosition = -1;
	ReferenceBinding[] itsInterfaces = superInterfaces;
	if (itsInterfaces != Binding.NO_SUPERINTERFACES)
		interfacesToVisit[++lastPosition] = itsInterfaces;

	ReferenceBinding superType = superclass;
	HashtableOfObject nonVisibleDefaultMethods = new HashtableOfObject(3); // maps method selectors to an array of methods
	boolean allSuperclassesAreAbstract = true;

	while (superType != null && superType.isValidBinding()) {
	    if (allSuperclassesAreAbstract) {
		    if (superType.isAbstract()) {
				// only need to include superinterfaces if immediate superclasses are abstract
				if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
					if (++lastPosition == interfacesToVisit.length)
						System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
					interfacesToVisit[lastPosition] = itsInterfaces;
				}
			} else {
			    allSuperclassesAreAbstract = false;
			}
		}

		MethodBinding[] methods = superType.unResolvedMethods();
		nextMethod : for (int m = methods.length; --m >= 0;) {
			MethodBinding inheritedMethod = methods[m];
			if (inheritedMethod.isPrivate() || inheritedMethod.isConstructor() || inheritedMethod.isDefaultAbstract())
				continue nextMethod;
			MethodBinding[] existingMethods = (MethodBinding[]) this.inheritedMethods.get(inheritedMethod.selector);
			if (existingMethods != null) {
				for (int i = 0, length = existingMethods.length; i < length; i++) {
					if (existingMethods[i].declaringClass != inheritedMethod.declaringClass && areMethodsEqual(existingMethods[i], inheritedMethod)) {
						if (inheritedMethod.isDefault() && inheritedMethod.isAbstract())
							checkPackagePrivateAbstractMethod(inheritedMethod);
						continue nextMethod;
					}
				}
			}
			MethodBinding[] nonVisible = (MethodBinding[]) nonVisibleDefaultMethods.get(inheritedMethod.selector);
			if (nonVisible != null)
				for (int i = 0, l = nonVisible.length; i < l; i++)
					if (areMethodsEqual(nonVisible[i], inheritedMethod))
						continue nextMethod;

			if (!inheritedMethod.isDefault() || inheritedMethod.declaringClass.fPackage == type.fPackage) {
				if (existingMethods == null) {
					existingMethods = new MethodBinding[] {inheritedMethod};
				} else {
					int length = existingMethods.length;
					System.arraycopy(existingMethods, 0, existingMethods = new MethodBinding[length + 1], 0, length);
					existingMethods[length] = inheritedMethod;
				}
				this.inheritedMethods.put(inheritedMethod.selector, existingMethods);
			} else {
				if (nonVisible == null) {
					nonVisible = new MethodBinding[] {inheritedMethod};
				} else {
					int length = nonVisible.length;
					System.arraycopy(nonVisible, 0, nonVisible = new MethodBinding[length + 1], 0, length);
					nonVisible[length] = inheritedMethod;
				}
				nonVisibleDefaultMethods.put(inheritedMethod.selector, nonVisible);

				if (inheritedMethod.isAbstract() && !this.type.isAbstract()) // non visible abstract methods cannot be overridden so the type must be defined abstract
					problemReporter().abstractMethodCannotBeOverridden(this.type, inheritedMethod);

				MethodBinding[] current = (MethodBinding[]) this.currentMethods.get(inheritedMethod.selector);
				if (current != null) { // non visible methods cannot be overridden so a warning is issued
					foundMatch : for (int i = 0, length = current.length; i < length; i++) {
						if (areMethodsEqual(current[i], inheritedMethod)) {
							problemReporter().overridesPackageDefaultMethod(current[i], inheritedMethod);
							break foundMatch;
						}
					}
				}
			}
		}
		superType = superType.superclass();
	}

	for (int i = 0; i <= lastPosition; i++) {
		ReferenceBinding[] interfaces = interfacesToVisit[i];
		for (int j = 0, l = interfaces.length; j < l; j++) {
			superType = interfaces[j];
			if ((superType.tagBits & InterfaceVisited) == 0) {
				superType.tagBits |= InterfaceVisited;
				if (superType.isValidBinding()) {
					if ((itsInterfaces = superType.superInterfaces()) != Binding.NO_SUPERINTERFACES) {
						if (++lastPosition == interfacesToVisit.length)
							System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
						interfacesToVisit[lastPosition] = itsInterfaces;
					}

					MethodBinding[] methods = superType.unResolvedMethods();
					nextMethod : for (int m = methods.length; --m >= 0;) { // Interface methods are all abstract public
						MethodBinding inheritedMethod = methods[m];
						MethodBinding[] existingMethods = (MethodBinding[]) this.inheritedMethods.get(inheritedMethod.selector);
						if (existingMethods == null) {
							existingMethods = new MethodBinding[] {inheritedMethod};
						} else {
							int length = existingMethods.length;
							// look to see if any of the existingMethods implement this inheritedMethod
							for (int e = 0; e < length; e++)
								if (isInterfaceMethodImplemented(inheritedMethod, existingMethods[e], superType))
									continue nextMethod; // skip interface method with the same signature if visible to its declaringClass
							System.arraycopy(existingMethods, 0, existingMethods = new MethodBinding[length + 1], 0, length);
							existingMethods[length] = inheritedMethod;
						}
						this.inheritedMethods.put(inheritedMethod.selector, existingMethods);
					}
				}
			}
		}
	}

	// bit reinitialization
	for (int i = 0; i <= lastPosition; i++) {
		ReferenceBinding[] interfaces = interfacesToVisit[i];
		for (int j = 0, length = interfaces.length; j < length; j++)
			interfaces[j].tagBits &= ~InterfaceVisited;
	}
}
void computeMethods() {
	MethodBinding[] methods = type.methods();
	int size = methods.length;
	this.currentMethods = new HashtableOfObject(size == 0 ? 1 : size); // maps method selectors to an array of methods... must search to match paramaters & return type
	for (int m = size; --m >= 0;) {
		MethodBinding method = methods[m];
		if (!(method.isConstructor() || method.isDefaultAbstract())) { // keep all methods which are NOT constructors or default abstract
			MethodBinding[] existingMethods = (MethodBinding[]) this.currentMethods.get(method.selector);
			if (existingMethods == null)
				existingMethods = new MethodBinding[1];
			else
				System.arraycopy(existingMethods, 0,
					(existingMethods = new MethodBinding[existingMethods.length + 1]), 0, existingMethods.length - 1);
			existingMethods[existingMethods.length - 1] = method;
			this.currentMethods.put(method.selector, existingMethods);
		}
	}
}
MethodBinding computeSubstituteMethod(MethodBinding inheritedMethod, MethodBinding currentMethod) {
	if (inheritedMethod == null) return null;
	if (currentMethod.parameters.length != inheritedMethod.parameters.length) return null; // no match
	return inheritedMethod;
}
public boolean doesMethodOverride(MethodBinding method, MethodBinding inheritedMethod) {
	return areParametersEqual(method, inheritedMethod);
}
boolean isAsVisible(MethodBinding newMethod, MethodBinding inheritedMethod) {
	if (inheritedMethod.modifiers == newMethod.modifiers) return true;

	if (newMethod.isPublic()) return true;		// Covers everything
	if (inheritedMethod.isPublic()) return false;

	if (newMethod.isProtected()) return true;
	if (inheritedMethod.isProtected()) return false;

	return !newMethod.isPrivate();		// The inheritedMethod cannot be private since it would not be visible
}
boolean isInterfaceMethodImplemented(MethodBinding inheritedMethod, MethodBinding existingMethod, ReferenceBinding superType) {
	// skip interface method with the same signature if visible to its declaringClass
	return areParametersEqual(existingMethod, inheritedMethod) && existingMethod.declaringClass.implementsInterface(superType, true);
}
boolean isSameClassOrSubclassOf(ReferenceBinding testClass, ReferenceBinding superclass) {
	do {
		if (testClass == superclass) return true;
	} while ((testClass = testClass.superclass()) != null);
	return false;
}
boolean mustImplementAbstractMethod(ReferenceBinding declaringClass) {
	// if the type's superclass is an abstract class, then all abstract methods must be implemented
	// otherwise, skip it if the type's superclass must implement any of the inherited methods
	ReferenceBinding superclass = this.type.superclass();
	if (declaringClass.isClass()) {
		while (superclass.isAbstract() && superclass != declaringClass)
			superclass = superclass.superclass(); // find the first concrete superclass or the abstract declaringClass
	} else {
		if (this.type.implementsInterface(declaringClass, false)) {
			if (this.type.isAbstract()) return false; // leave it for the subclasses
			if (!superclass.implementsInterface(declaringClass, true)) // only if a superclass does not also implement the interface
				return true;
		}
		while (superclass.isAbstract() && !superclass.implementsInterface(declaringClass, false))
			superclass = superclass.superclass(); // find the first concrete superclass or the superclass which implements the interface
	}
	return superclass.isAbstract();		// if it is a concrete class then we have already reported problem against it
}
boolean mustImplementAbstractMethods() {
	return !this.type.isInterface() && !this.type.isAbstract();
}
ProblemReporter problemReporter() {
	return this.type.scope.problemReporter();
}
ProblemReporter problemReporter(MethodBinding currentMethod) {
	ProblemReporter reporter = problemReporter();
	if (currentMethod.declaringClass == type && currentMethod.sourceMethod() != null)	// only report against the currentMethod if its implemented by the type
		reporter.referenceContext = currentMethod.sourceMethod();
	return reporter;
}
ReferenceBinding[] resolvedExceptionTypesFor(MethodBinding method) {
	ReferenceBinding[] exceptions = method.thrownExceptions;
	if ((method.modifiers & ExtraCompilerModifiers.AccUnresolved) == 0)
		return exceptions;

	if (!(method.declaringClass instanceof BinaryTypeBinding))
		return Binding.NO_EXCEPTIONS; // safety check

	for (int i = exceptions.length; --i >= 0;)
		exceptions[i] = BinaryTypeBinding.resolveType(exceptions[i], this.environment, true);
	return exceptions;
}
void verify(SourceTypeBinding someType) {
	this.type = someType;
	computeMethods();
	computeInheritedMethods();
	checkMethods();
}
public String toString() {
	StringBuffer buffer = new StringBuffer(10);
	buffer.append("MethodVerifier for type: "); //$NON-NLS-1$
	buffer.append(type.readableName());
	buffer.append('\n');
	buffer.append("\t-inherited methods: "); //$NON-NLS-1$
	buffer.append(this.inheritedMethods);
	return buffer.toString();
}
}
