/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

public final class MethodVerifier implements TagBits, TypeConstants {
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
public MethodVerifier(LookupEnvironment environment) {
	this.type = null;  // Initialized with the public method verify(SourceTypeBinding)
	this.inheritedMethods = null;
	this.currentMethods = null;
	this.runtimeException = null;
	this.errorException = null;
	this.environment = environment;
}
private void checkAgainstInheritedMethods(MethodBinding currentMethod, MethodBinding[] methods, int length) {
	currentMethod.modifiers |= CompilerModifiers.AccOverriding;
	for (int i = length; --i >= 0;) {
		MethodBinding inheritedMethod = methods[i];
		if (!currentMethod.isAbstract() && inheritedMethod.isAbstract())
			currentMethod.modifiers |= CompilerModifiers.AccImplementing;

		if (currentMethod.returnType != inheritedMethod.returnType) {
			this.problemReporter(currentMethod).incompatibleReturnType(currentMethod, inheritedMethod);
		} else if (currentMethod.isStatic() != inheritedMethod.isStatic()) {  // Cannot override a static method or hide an instance method
			this.problemReporter(currentMethod).staticAndInstanceConflict(currentMethod, inheritedMethod);
		} else {
			if (currentMethod.thrownExceptions != NoExceptions)
				this.checkExceptions(currentMethod, inheritedMethod);
			if (inheritedMethod.isFinal())
				this.problemReporter(currentMethod).finalMethodCannotBeOverridden(currentMethod, inheritedMethod);
			if (!this.isAsVisible(currentMethod, inheritedMethod))
				this.problemReporter(currentMethod).visibilityConflict(currentMethod, inheritedMethod);
			if (inheritedMethod.isViewedAsDeprecated())
				if (!currentMethod.isViewedAsDeprecated() || environment.options.reportDeprecationInsideDeprecatedCode)
					this.problemReporter(currentMethod).overridesDeprecatedMethod(currentMethod, inheritedMethod);
		}
	}
}
/*
"8.4.4"
Verify that newExceptions are all included in inheritedExceptions.
Assumes all exceptions are valid and throwable.
Unchecked exceptions (compatible with runtime & error) are ignored (see the spec on pg. 203).
*/
private void checkExceptions(MethodBinding newMethod, MethodBinding inheritedMethod) {
	ReferenceBinding[] newExceptions = newMethod.thrownExceptions;
	ReferenceBinding[] inheritedExceptions = inheritedMethod.thrownExceptions;
	for (int i = newExceptions.length; --i >= 0;) {
		ReferenceBinding newException = newExceptions[i];
		int j = inheritedExceptions.length;
		while (--j > -1 && !this.isSameClassOrSubclassOf(newException, inheritedExceptions[j]));
		if (j == -1)
			if (!(newException.isCompatibleWith(this.runtimeException()) || newException.isCompatibleWith(this.errorException())))
				this.problemReporter(newMethod).incompatibleExceptionInThrowsClause(this.type, newMethod, inheritedMethod, newException);
	}
}
private void checkInheritedMethods(MethodBinding[] methods, int length) {
	TypeBinding returnType = methods[0].returnType;
	int index = length;
	while (--index > 0 && returnType == methods[index].returnType);
	if (index > 0) {  // All inherited methods do NOT have the same vmSignature
		this.problemReporter().inheritedMethodsHaveIncompatibleReturnTypes(this.type, methods, length);
		return;
	}

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
		if (this.type.isClass() && !this.type.isAbstract()) {
			for (int i = length; --i >= 0;)
				if (!mustImplementAbstractMethod(methods[i])) return;  // have already reported problem against the concrete superclass

			TypeDeclaration typeDeclaration = this.type.scope.referenceContext;
			if (typeDeclaration != null) {
				MethodDeclaration missingAbstractMethod = typeDeclaration.addMissingAbstractMethodFor(methods[0]);
				missingAbstractMethod.scope.problemReporter().abstractMethodMustBeImplemented(this.type, methods[0]);
			} else {
				this.problemReporter().abstractMethodMustBeImplemented(this.type, methods[0]);
			}
		}
		return;
	}

	MethodBinding[] abstractMethods = new MethodBinding[length - 1];
	index = 0;
	for (int i = length; --i >= 0;)
		if (methods[i] != concreteMethod)
			abstractMethods[index++] = methods[i];

	// Remember that interfaces can only define public instance methods
	if (concreteMethod.isStatic())
		// Cannot inherit a static method which is specified as an instance method by an interface
		this.problemReporter().staticInheritedMethodConflicts(type, concreteMethod, abstractMethods);	
	if (!concreteMethod.isPublic())
		// Cannot reduce visibility of a public method specified by an interface
		this.problemReporter().inheritedMethodReducesVisibility(type, concreteMethod, abstractMethods);
	if (concreteMethod.thrownExceptions != NoExceptions)
		for (int i = abstractMethods.length; --i >= 0;)
			this.checkExceptions(concreteMethod, abstractMethods[i]);
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
private void checkMethods() { 
	boolean mustImplementAbstractMethods = this.type.isClass() && !this.type.isAbstract();
	char[][] methodSelectors = this.inheritedMethods.keyTable;
	for (int s = methodSelectors.length; --s >= 0;) {
		if (methodSelectors[s] != null) {
			MethodBinding[] current = (MethodBinding[]) this.currentMethods.get(methodSelectors[s]);
			MethodBinding[] inherited = (MethodBinding[]) this.inheritedMethods.valueTable[s];

			int index = -1;
			MethodBinding[] matchingInherited = new MethodBinding[inherited.length];
			if (current != null) {
				for (int i = 0, length1 = current.length; i < length1; i++) {
					while (index >= 0) matchingInherited[index--] = null; // clear the previous contents of the matching methods
					MethodBinding currentMethod = current[i];
					for (int j = 0, length2 = inherited.length; j < length2; j++) {
						if (inherited[j] != null && currentMethod.areParametersEqual(inherited[j])) {
							matchingInherited[++index] = inherited[j];
							inherited[j] = null; // do not want to find it again
						}
					}
					if (index >= 0)
						this.checkAgainstInheritedMethods(currentMethod, matchingInherited, index + 1); // pass in the length of matching
				}
			}
			for (int i = 0, length = inherited.length; i < length; i++) {
				while (index >= 0) matchingInherited[index--] = null; // clear the previous contents of the matching methods
				if (inherited[i] != null) {
					matchingInherited[++index] = inherited[i];
					for (int j = i + 1; j < length; j++) {
						if (inherited[j] != null && inherited[i].areParametersEqual(inherited[j])) {
							matchingInherited[++index] = inherited[j];
							inherited[j] = null; // do not want to find it again
						}
					}
				}
				if (index > 0) {
					this.checkInheritedMethods(matchingInherited, index + 1); // pass in the length of matching
				} else if (mustImplementAbstractMethods && index == 0 && matchingInherited[0].isAbstract()) {
					if (mustImplementAbstractMethod(matchingInherited[0])) {
						TypeDeclaration typeDeclaration = this.type.scope.referenceContext;
						if (typeDeclaration != null) {
							MethodDeclaration missingAbstractMethod = typeDeclaration.addMissingAbstractMethodFor(matchingInherited[0]);
							missingAbstractMethod.scope.problemReporter().abstractMethodMustBeImplemented(this.type, matchingInherited[0]);
						} else {
							this.problemReporter().abstractMethodMustBeImplemented(this.type, matchingInherited[0]);
						}
					}
				}
			}
		}
	}
}
private void checkPackagePrivateAbstractMethod(MethodBinding abstractMethod) {
	ReferenceBinding superType = this.type.superclass();
	char[] selector = abstractMethod.selector;
	do {
		if (!superType.isValidBinding()) return;
		if (!superType.isAbstract()) return; // closer non abstract super type will be flagged instead

		MethodBinding[] methods = superType.getMethods(selector);
		nextMethod : for (int m = methods.length; --m >= 0;) {
			MethodBinding method = methods[m];
			if (method.returnType != abstractMethod.returnType || !method.areParametersEqual(abstractMethod))
				continue nextMethod;
			if (method.isPrivate() || method.isConstructor() || method.isDefaultAbstract())
				continue nextMethod;
			if (superType.fPackage == abstractMethod.declaringClass.fPackage) return; // found concrete implementation of abstract method in same package
		}
	} while ((superType = superType.superclass()) != abstractMethod.declaringClass);

	// non visible abstract methods cannot be overridden so the type must be defined abstract
	this.problemReporter().abstractMethodCannotBeOverridden(this.type, abstractMethod);
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
private void computeInheritedMethods() {
	this.inheritedMethods = new HashtableOfObject(51); // maps method selectors to an array of methods... must search to match paramaters & return type
	ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
	int lastPosition = 0;
	interfacesToVisit[lastPosition] = type.superInterfaces();

	ReferenceBinding superType = this.type.isClass()
		? this.type.superclass()
		: this.type.scope.getJavaLangObject(); // check interface methods against Object
	MethodBinding[] nonVisibleDefaultMethods = null;
	int nonVisibleCount = 0;

	while (superType != null) {
		if (superType.isValidBinding()) {
			ReferenceBinding[] itsInterfaces = superType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {
				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}

			MethodBinding[] methods = superType.methods();
			nextMethod : for (int m = methods.length; --m >= 0;) {
				MethodBinding method = methods[m];
				if (!(method.isPrivate() || method.isConstructor() || method.isDefaultAbstract())) { // look at all methods which are NOT private or constructors or default abstract
					MethodBinding[] existingMethods = (MethodBinding[]) this.inheritedMethods.get(method.selector);
					if (existingMethods != null) {
						for (int i = 0, length = existingMethods.length; i < length; i++) {
							if (method.returnType == existingMethods[i].returnType && method.areParametersEqual(existingMethods[i])) {
								if (method.isDefault() && method.isAbstract() && method.declaringClass.fPackage != type.fPackage)
									checkPackagePrivateAbstractMethod(method);
								continue nextMethod;
							}
						}
					}
					if (nonVisibleDefaultMethods != null)
						for (int i = 0; i < nonVisibleCount; i++)
							if (method.returnType == nonVisibleDefaultMethods[i].returnType
								&& CharOperation.equals(method.selector, nonVisibleDefaultMethods[i].selector)
								&& method.areParametersEqual(nonVisibleDefaultMethods[i])) 
									continue nextMethod;

					if (!(method.isDefault() && method.declaringClass.fPackage != type.fPackage)) { // ignore methods which have default visibility and are NOT defined in another package
						if (existingMethods == null)
							existingMethods = new MethodBinding[1];
						else
							System.arraycopy(existingMethods, 0,
								(existingMethods = new MethodBinding[existingMethods.length + 1]), 0, existingMethods.length - 1);
						existingMethods[existingMethods.length - 1] = method;
						this.inheritedMethods.put(method.selector, existingMethods);
					} else {
						if (nonVisibleDefaultMethods == null)
							nonVisibleDefaultMethods = new MethodBinding[10];
						else if (nonVisibleCount == nonVisibleDefaultMethods.length)
							System.arraycopy(nonVisibleDefaultMethods, 0,
								(nonVisibleDefaultMethods = new MethodBinding[nonVisibleCount * 2]), 0, nonVisibleCount);
						nonVisibleDefaultMethods[nonVisibleCount++] = method;

						if (method.isAbstract() && !this.type.isAbstract()) // non visible abstract methods cannot be overridden so the type must be defined abstract
							this.problemReporter().abstractMethodCannotBeOverridden(this.type, method);

						MethodBinding[] current = (MethodBinding[]) this.currentMethods.get(method.selector);
						if (current != null) { // non visible methods cannot be overridden so a warning is issued
							foundMatch : for (int i = 0, length = current.length; i < length; i++) {
								if (method.returnType == current[i].returnType && method.areParametersEqual(current[i])) {
									this.problemReporter().overridesPackageDefaultMethod(current[i], method);
									break foundMatch;
								}
							}
						}
					}
				}
			}
			superType = superType.superclass();
		}
	}

	for (int i = 0; i <= lastPosition; i++) {
		ReferenceBinding[] interfaces = interfacesToVisit[i];
		for (int j = 0, length = interfaces.length; j < length; j++) {
			superType = interfaces[j];
			if ((superType.tagBits & InterfaceVisited) == 0) {
				superType.tagBits |= InterfaceVisited;
				if (superType.isValidBinding()) {
					ReferenceBinding[] itsInterfaces = superType.superInterfaces();
					if (itsInterfaces != NoSuperInterfaces) {
						if (++lastPosition == interfacesToVisit.length)
							System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
						interfacesToVisit[lastPosition] = itsInterfaces;
					}

					MethodBinding[] methods = superType.methods();
					for (int m = methods.length; --m >= 0;) { // Interface methods are all abstract public
						MethodBinding method = methods[m];
						MethodBinding[] existingMethods = (MethodBinding[]) this.inheritedMethods.get(method.selector);
						if (existingMethods == null)
							existingMethods = new MethodBinding[1];
						else
							System.arraycopy(existingMethods, 0,
								(existingMethods = new MethodBinding[existingMethods.length + 1]), 0, existingMethods.length - 1);
						existingMethods[existingMethods.length - 1] = method;
						this.inheritedMethods.put(method.selector, existingMethods);
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
private void computeMethods() {
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
private ReferenceBinding errorException() {
	if (errorException == null)
		this.errorException = this.type.scope.getJavaLangError();
	return errorException;
}
private boolean isAsVisible(MethodBinding newMethod, MethodBinding inheritedMethod) {
	if (inheritedMethod.modifiers == newMethod.modifiers) return true;

	if (newMethod.isPublic()) return true;		// Covers everything
	if (inheritedMethod.isPublic()) return false;

	if (newMethod.isProtected()) return true;
	if (inheritedMethod.isProtected()) return false;

	return !newMethod.isPrivate();		// The inheritedMethod cannot be private since it would not be visible
}
private boolean isSameClassOrSubclassOf(ReferenceBinding testClass, ReferenceBinding superclass) {
	do {
		if (testClass == superclass) return true;
	} while ((testClass = testClass.superclass()) != null);
	return false;
}
private boolean mustImplementAbstractMethod(MethodBinding abstractMethod) {
	// if the type's superclass is an abstract class, then all abstract methods must be implemented
	// otherwise, skip it if the type's superclass must implement any of the inherited methods
	ReferenceBinding superclass = this.type.superclass();
	ReferenceBinding declaringClass = abstractMethod.declaringClass;
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
private ProblemReporter problemReporter() {
	return this.type.scope.problemReporter();
}
private ProblemReporter problemReporter(MethodBinding currentMethod) {
	ProblemReporter reporter = problemReporter();
	if (currentMethod.declaringClass == type)	// only report against the currentMethod if its implemented by the type
		reporter.referenceContext = currentMethod.sourceMethod();
	return reporter;
}
private ReferenceBinding runtimeException() {
	if (runtimeException == null)
		this.runtimeException = this.type.scope.getJavaLangRuntimeException();
	return runtimeException;
}
public void verify(SourceTypeBinding type) {
	this.type = type;
	this.computeMethods();
	this.computeInheritedMethods();
	this.checkMethods();
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