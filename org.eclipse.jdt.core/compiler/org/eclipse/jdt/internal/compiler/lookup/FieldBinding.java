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
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.Constant;

public class FieldBinding extends VariableBinding {
	public ReferenceBinding declaringClass;
protected FieldBinding() {
	super(null, null, 0, null);
	// for creating problem field
}
public FieldBinding(char[] name, TypeBinding type, int modifiers, ReferenceBinding declaringClass, Constant constant) {
	super(name, type, modifiers, constant);
	this.declaringClass = declaringClass;
}
public FieldBinding(FieldDeclaration field, TypeBinding type, int modifiers, ReferenceBinding declaringClass) {
	this(field.name, type, modifiers, declaringClass, null);
	field.binding = this; // record binding in declaration
}
// special API used to change field declaring class for runtime visibility check
public FieldBinding(FieldBinding initialFieldBinding, ReferenceBinding declaringClass) {
	super(initialFieldBinding.name, initialFieldBinding.type, initialFieldBinding.modifiers, initialFieldBinding.constant());
	this.declaringClass = declaringClass;
	this.id = initialFieldBinding.id;
}
/* API
* Answer the receiver's binding type from Binding.BindingID.
*/

public final int kind() {
	return FIELD;
}
/* Answer true if the receiver is visible to the invocationPackage.
*/

public final boolean canBeSeenBy(PackageBinding invocationPackage) {
	if (isPublic()) return true;
	if (isPrivate()) return false;

	// isProtected() or isDefault()
	return invocationPackage == declaringClass.getPackage();
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
		int depth = 0;
		ReferenceBinding receiverErasure = (ReferenceBinding)receiverType.erasure();
		ReferenceBinding declaringErasure = (ReferenceBinding) declaringClass.erasure();
		do {
			if (currentType.findSuperTypeErasingTo(declaringErasure) != null) {
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
				if (currentType == receiverErasure || receiverErasure.findSuperTypeErasingTo(currentType) != null){
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
	ReferenceBinding currentType = (ReferenceBinding) receiverType;
	PackageBinding declaringPackage = declaringClass.fPackage;
	do {
		if (declaringClass == currentType) return true;
		if (declaringPackage != currentType.fPackage) return false;
	} while ((currentType = currentType.superclass()) != null);
	return false;
}
/*
 * declaringUniqueKey dot fieldName
 * p.X { X<T> x} --> Lp/X;.x)p/X<TT;>;
 */
public char[] computeUniqueKey(boolean isLeaf) {
	// declaring key
	char[] declaringKey = 
		this.declaringClass == null /*case of length field for an array*/ 
			? CharOperation.NO_CHAR 
			: this.declaringClass.computeUniqueKey(false/*not a leaf*/);
	int declaringLength = declaringKey.length;
	
	// name
	int nameLength = this.name.length;
	
	// return type
	char[] returnTypeKey = this.type == null ? new char[] {'V'} : this.type.computeUniqueKey(false/*not a leaf*/);
	int returnTypeLength = returnTypeKey.length;
	
	char[] uniqueKey = new char[declaringLength + 1 + nameLength + 1 + returnTypeLength];
	int index = 0;
	System.arraycopy(declaringKey, 0, uniqueKey, index, declaringLength);
	index += declaringLength;
	uniqueKey[index++] = '.';
	System.arraycopy(this.name, 0, uniqueKey, index, nameLength);
	index += nameLength;
	uniqueKey[index++] = ')';
	System.arraycopy(returnTypeKey, 0, uniqueKey, index, returnTypeLength);
	return uniqueKey;
}
/**
 * X<T> t   -->  LX<TT;>;
 */
public char[] genericSignature() {
    if ((this.modifiers & AccGenericSignature) == 0) return null;
    return this.type.genericTypeSignature();
}

public final int getAccessFlags() {
	return modifiers & AccJustFlag;
}

/**
 * Compute the tagbits for standard annotations. For source types, these could require
 * lazily resolving corresponding annotation nodes, in case of forward references.
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#getAnnotationTagBits()
 */
public long getAnnotationTagBits() {
	FieldBinding originalField = this.original();
	if ((originalField.tagBits & TagBits.AnnotationResolved) == 0 && originalField.declaringClass instanceof SourceTypeBinding) {
		TypeDeclaration typeDecl = ((SourceTypeBinding)originalField.declaringClass).scope.referenceContext;
		FieldDeclaration fieldDecl = typeDecl.declarationOf(originalField);
		if (fieldDecl != null) {
			MethodScope initializationScope = isStatic() ? typeDecl.staticInitializerScope : typeDecl.initializerScope;
			FieldBinding previousField = initializationScope.initializedField;
			int previousFieldID = initializationScope.lastVisibleFieldID;			
			try {
				initializationScope.initializedField = originalField;
				initializationScope.lastVisibleFieldID = originalField.id;
				ASTNode.resolveAnnotations(initializationScope, fieldDecl.annotations, originalField);
			} finally {
				initializationScope.initializedField = previousField;
				initializationScope.lastVisibleFieldID = previousFieldID;
			}
		}
	}
	return originalField.tagBits;
}

/* Answer true if the receiver has default visibility
*/

public final boolean isDefault() {
	return !isPublic() && !isProtected() && !isPrivate();
}
/* Answer true if the receiver is a deprecated field
*/

public final boolean isDeprecated() {
	return (modifiers & AccDeprecated) != 0;
}
/* Answer true if the receiver has private visibility
*/

public final boolean isPrivate() {
	return (modifiers & AccPrivate) != 0;
}
/* Answer true if the receiver has private visibility and is used locally
*/

public final boolean isPrivateUsed() {
	return (modifiers & AccPrivateUsed) != 0;
}
/* Answer true if the receiver has protected visibility
*/

public final boolean isProtected() {
	return (modifiers & AccProtected) != 0;
}
/* Answer true if the receiver has public visibility
*/

public final boolean isPublic() {
	return (modifiers & AccPublic) != 0;
}
/* Answer true if the receiver is a static field
*/

public final boolean isStatic() {
	return (modifiers & AccStatic) != 0;
}
/* Answer true if the receiver is not defined in the source of the declaringClass
*/

public final boolean isSynthetic() {
	return (modifiers & AccSynthetic) != 0;
}
/* Answer true if the receiver is a transient field
*/

public final boolean isTransient() {
	return (modifiers & AccTransient) != 0;
}
/* Answer true if the receiver's declaring type is deprecated (or any of its enclosing types)
*/

public final boolean isViewedAsDeprecated() {
	return (modifiers & (AccDeprecated | AccDeprecatedImplicitly)) != 0;
}
/* Answer true if the receiver is a volatile field
*/

public final boolean isVolatile() {
	return (modifiers & AccVolatile) != 0;
}
/**
 * Returns the original field (as opposed to parameterized instances)
 */
public FieldBinding original() {
	return this;
}
public FieldDeclaration sourceField() {
	SourceTypeBinding sourceType;
	try {
		sourceType = (SourceTypeBinding) declaringClass;
	} catch (ClassCastException e) {
		return null;		
	}

	FieldDeclaration[] fields = sourceType.scope.referenceContext.fields;
	if (fields != null) {
		for (int i = fields.length; --i >= 0;)
			if (this == fields[i].binding)
				return fields[i];
	}
	return null;		
}
}
