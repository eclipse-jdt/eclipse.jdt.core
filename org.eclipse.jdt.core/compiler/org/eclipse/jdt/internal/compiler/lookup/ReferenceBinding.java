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
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IDependent;

/*
Not all fields defined by this type (& its subclasses) are initialized when it is created.
Some are initialized only when needed.

Accessors have been provided for some public fields so all TypeBindings have the same API...
but access public fields directly whenever possible.
Non-public fields have accessors which should be used everywhere you expect the field to be initialized.

null is NOT a valid value for a non-public field... it just means the field is not initialized.
*/

abstract public class ReferenceBinding extends TypeBinding implements IDependent {
	
	public static ReferenceBinding LUB_GENERIC = new ReferenceBinding() { /* used for lub computation */};
	
	public char[][] compoundName;
	public char[] sourceName;
	public int modifiers;
	public PackageBinding fPackage;

	char[] fileName;
	char[] constantPoolName;
	char[] signature;

public FieldBinding[] availableFields() {
	return fields();
}

public MethodBinding[] availableMethods() {
	return methods();
}	
/* Answer true if the receiver can be instantiated
*/

public boolean canBeInstantiated() {
	return (this.modifiers & (ClassFileConstants.AccAbstract | ClassFileConstants.AccInterface | ClassFileConstants.AccEnum | ClassFileConstants.AccAnnotation)) == 0;
}
/* Answer true if the receiver is visible to the invocationPackage.
*/

public final boolean canBeSeenBy(PackageBinding invocationPackage) {
	if (isPublic()) return true;
	if (isPrivate()) return false;

	// isProtected() or isDefault()
	return invocationPackage == fPackage;
}
/* Answer true if the receiver is visible to the receiverType and the invocationType.
*/

public final boolean canBeSeenBy(ReferenceBinding receiverType, ReferenceBinding invocationType) {
	if (isPublic()) return true;

	if (invocationType == this && invocationType == receiverType) return true;

	if (isProtected()) {

		// answer true if the invocationType is the declaringClass or they are in the same package
		// OR the invocationType is a subclass of the declaringClass
		//    AND the invocationType is the invocationType or its subclass
		//    OR the type is a static method accessed directly through a type
		//    OR previous assertions are true for one of the enclosing type
		if (invocationType == this) return true;
		if (invocationType.fPackage == fPackage) return true;

		ReferenceBinding currentType = invocationType;
		ReferenceBinding declaringClass = enclosingType(); // protected types always have an enclosing one
		if (declaringClass == invocationType) return true;
		if (declaringClass == null) return false; // could be null if incorrect top-level protected type
		//int depth = 0;
		do {
			if (currentType.findSuperTypeWithSameErasure(declaringClass) != null) return true;
			//depth++;
			currentType = currentType.enclosingType();
		} while (currentType != null);
		return false;
	}

	if (isPrivate()) {
		// answer true if the receiverType is the receiver or its enclosingType
		// AND the invocationType and the receiver have a common enclosingType
		receiverCheck: {
			if (!(receiverType == this || receiverType == enclosingType())) {
				// special tolerance for type variable direct bounds
				if (receiverType.isTypeVariable()) {
					TypeVariableBinding typeVariable = (TypeVariableBinding) receiverType;
					if (typeVariable.isErasureBoundTo(this.erasure()) || typeVariable.isErasureBoundTo(enclosingType().erasure())) {
						break receiverCheck;
					}
				}
				return false;
			}
		}
		
		
		if (invocationType != this) {
			ReferenceBinding outerInvocationType = invocationType;
			ReferenceBinding temp = outerInvocationType.enclosingType();
			while (temp != null) {
				outerInvocationType = temp;
				temp = temp.enclosingType();
			}

			ReferenceBinding outerDeclaringClass = (ReferenceBinding)this.erasure();
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
	if (invocationType.fPackage != fPackage) return false;

	ReferenceBinding type = receiverType;
	ReferenceBinding declaringClass = enclosingType() == null ? this : enclosingType();
	do {
		if (declaringClass == type) return true;
		if (fPackage != type.fPackage) return false;
	} while ((type = type.superclass()) != null);
	return false;
}
/* 
 * Answer true if the receiver is visible to the type provided by the scope.
 */

public final boolean canBeSeenBy(Scope scope) {
	
	if (isPublic()) return true;

	if (scope.kind == Scope.COMPILATION_UNIT_SCOPE){
		return this.canBeSeenBy(((CompilationUnitScope)scope).fPackage);
	}
	
	SourceTypeBinding invocationType = scope.enclosingSourceType();
	if (invocationType == this) return true;

	if (isProtected()) {
		// answer true if the invocationType is the declaringClass or they are in the same package
		// OR the invocationType is a subclass of the declaringClass
		//    AND the invocationType is the invocationType or its subclass
		//    OR the type is a static method accessed directly through a type
		//    OR previous assertions are true for one of the enclosing type
		if (invocationType.fPackage == fPackage) return true;

		ReferenceBinding currentType = invocationType;
		ReferenceBinding declaringClass = enclosingType(); // protected types always have an enclosing one
		if (declaringClass == null) return false; // could be null if incorrect top-level protected type
		// int depth = 0;
		do {
			if (declaringClass == invocationType) return true;
			if (declaringClass.isSuperclassOf(currentType)) return true;
			// depth++;
			currentType = currentType.enclosingType();
		} while (currentType != null);
		return false;
	}
	if (isPrivate()) {
		// answer true if the receiver and the invocationType have a common enclosingType
		// already know they are not the identical type
		ReferenceBinding outerInvocationType = invocationType;
		ReferenceBinding temp = outerInvocationType.enclosingType();
		while (temp != null) {
			outerInvocationType = temp;
			temp = temp.enclosingType();
		}

		ReferenceBinding outerDeclaringClass = (ReferenceBinding)this.erasure();
		temp = outerDeclaringClass.enclosingType();
		while (temp != null) {
			outerDeclaringClass = temp;
			temp = temp.enclosingType();
		}
		return outerInvocationType == outerDeclaringClass;
	}

	// isDefault()
	return invocationType.fPackage == fPackage;
}
public char[] computeGenericTypeSignature(TypeVariableBinding[] typeVariables) {

	boolean isMemberOfGeneric = isMemberType() && (enclosingType().modifiers & ExtraCompilerModifiers.AccGenericSignature) != 0;
	if (typeVariables == NoTypeVariables && !isMemberOfGeneric) {
		return signature();
	}
	StringBuffer sig = new StringBuffer(10);
	if (isMemberOfGeneric) {
	    char[] typeSig = enclosingType().genericTypeSignature();
	    for (int i = 0; i < typeSig.length-1; i++) { // copy all but trailing semicolon
	    	sig.append(typeSig[i]);
	    }
	    sig.append('.'); // NOTE: cannot override trailing ';' with '.' in enclosing signature, since shared char[]
	    sig.append(this.sourceName);
	}	else {
	    char[] typeSig = signature();
	    for (int i = 0; i < typeSig.length-1; i++) { // copy all but trailing semicolon
	    	sig.append(typeSig[i]);
	    }
	}
	if (typeVariables == NoTypeVariables) {
	    sig.append(';');
	} else {
	    sig.append('<');
	    for (int i = 0, length = typeVariables.length; i < length; i++) {
	        sig.append(typeVariables[i].genericTypeSignature());
	    }
	    sig.append(">;"); //$NON-NLS-1$
	}
	int sigLength = sig.length();
	char[] result = new char[sigLength];
	sig.getChars(0, sigLength, result, 0);
	return result;
}
public void computeId() {
	
	switch (compoundName.length) {

		case 3 :
			if (!CharOperation.equals(JAVA, compoundName[0]))
				return;
		
			// remaining types MUST be in java.*.*
			if (!CharOperation.equals(LANG, compoundName[1])) {
				if (CharOperation.equals(JAVA_IO_PRINTSTREAM, compoundName))
					id = T_JavaIoPrintStream;
				else if (CharOperation.equals(JAVA_UTIL_ITERATOR, compoundName))
					id = T_JavaUtilIterator;
				else if (CharOperation.equals(JAVA_IO_SERIALIZABLE, compoundName))
				    id = T_JavaIoSerializable;
				return;
			}
		
			// remaining types MUST be in java.lang.*
			char[] typeName = compoundName[2];
			if (typeName.length == 0) return; // just to be safe
			switch (typeName[0]) {
				case 'A' :
					if (CharOperation.equals(typeName, JAVA_LANG_ASSERTIONERROR[2]))
						id = T_JavaLangAssertionError;
					return;
				case 'B' :
					if (CharOperation.equals(typeName, JAVA_LANG_BOOLEAN[2]))
						id = T_JavaLangBoolean;
					else if (CharOperation.equals(typeName, JAVA_LANG_BYTE[2]))
						id = T_JavaLangByte;
					return;
				case 'C' :
					if (CharOperation.equals(typeName, JAVA_LANG_CHARACTER[2]))
						id = T_JavaLangCharacter;
					else if (CharOperation.equals(typeName, JAVA_LANG_CLASS[2]))
						id = T_JavaLangClass;
					else if (CharOperation.equals(typeName, JAVA_LANG_CLASSNOTFOUNDEXCEPTION[2]))
						id = T_JavaLangClassNotFoundException;
					else if (CharOperation.equals(typeName, JAVA_LANG_CLONEABLE[2]))
					    id = T_JavaLangCloneable;
					return;
				case 'D' :
					if (CharOperation.equals(typeName, JAVA_LANG_DOUBLE[2]))
						id = T_JavaLangDouble;
					else if (CharOperation.equals(typeName, JAVA_LANG_DEPRECATED[2]))
						id = T_JavaLangDeprecated;
					return;
				case 'E' :
					if (CharOperation.equals(typeName, JAVA_LANG_ERROR[2]))
						id = T_JavaLangError;
					else if (CharOperation.equals(typeName, JAVA_LANG_EXCEPTION[2]))
						id = T_JavaLangException;
					else if (CharOperation.equals(typeName, JAVA_LANG_ENUM[2]))
						id = T_JavaLangEnum;
					return;
				case 'F' :
					if (CharOperation.equals(typeName, JAVA_LANG_FLOAT[2]))
						id = T_JavaLangFloat;
					return;
				case 'I' :
					if (CharOperation.equals(typeName, JAVA_LANG_INTEGER[2]))
						id = T_JavaLangInteger;
					else if (CharOperation.equals(typeName, JAVA_LANG_ITERABLE[2]))
						id = T_JavaLangIterable;
					else if (CharOperation.equals(typeName, JAVA_LANG_ILLEGALARGUMENTEXCEPTION[2]))
						id = T_JavaLangIllegalArgumentException;
					return;
				case 'L' :
					if (CharOperation.equals(typeName, JAVA_LANG_LONG[2]))
						id = T_JavaLangLong;
					return;
				case 'N' :
					if (CharOperation.equals(typeName, JAVA_LANG_NOCLASSDEFERROR[2]))
						id = T_JavaLangNoClassDefError;
					return;
				case 'O' :
					if (CharOperation.equals(typeName, JAVA_LANG_OBJECT[2]))
						id = T_JavaLangObject;
					else if (CharOperation.equals(typeName, JAVA_LANG_OVERRIDE[2]))
						id = T_JavaLangOverride;
					return;
				case 'R' :
					if (CharOperation.equals(typeName, JAVA_LANG_RUNTIMEEXCEPTION[2]))
						id = 	T_JavaLangRuntimeException;
					break;
				case 'S' :
					if (CharOperation.equals(typeName, JAVA_LANG_STRING[2]))
						id = T_JavaLangString;
					else if (CharOperation.equals(typeName, JAVA_LANG_STRINGBUFFER[2]))
						id = T_JavaLangStringBuffer;
					else if (CharOperation.equals(typeName, JAVA_LANG_STRINGBUILDER[2])) 
						id = T_JavaLangStringBuilder;
					else if (CharOperation.equals(typeName, JAVA_LANG_SYSTEM[2]))
						id = T_JavaLangSystem;
					else if (CharOperation.equals(typeName, JAVA_LANG_SHORT[2]))
						id = T_JavaLangShort;
					else if (CharOperation.equals(typeName, JAVA_LANG_SUPPRESSWARNINGS[2]))
						id = T_JavaLangSuppressWarnings;
					return;
				case 'T' :
					if (CharOperation.equals(typeName, JAVA_LANG_THROWABLE[2]))
						id = T_JavaLangThrowable;
					return;
				case 'V' :
					if (CharOperation.equals(typeName, JAVA_LANG_VOID[2]))
						id = T_JavaLangVoid;
					return;
			}
		break;
			
		case 4:
			if (!CharOperation.equals(JAVA, compoundName[0]))
				return;
			if (!CharOperation.equals(LANG, compoundName[1]))
				return;
			char[] packageName = compoundName[2];
			if (packageName.length == 0) return; // just to be safe			
			typeName = compoundName[3];
			if (typeName.length == 0) return; // just to be safe			
			if (CharOperation.equals(packageName, REFLECT)) {
				if (CharOperation.equals(typeName, JAVA_LANG_REFLECT_CONSTRUCTOR[3]))
					id = T_JavaLangReflectConstructor;
				return;
			} else if (CharOperation.equals(packageName, ANNOTATION)) {
				switch (typeName[0]) {
					case 'A' :			
						if (CharOperation.equals(typeName, JAVA_LANG_ANNOTATION_ANNOTATION[3]))
							id = T_JavaLangAnnotationAnnotation;
						return;
					case 'D' :
						if (CharOperation.equals(typeName, JAVA_LANG_ANNOTATION_DOCUMENTED[3]))
							id = T_JavaLangAnnotationDocumented;
						return;
					case 'E' :
						if (CharOperation.equals(typeName, JAVA_LANG_ANNOTATION_ELEMENTTYPE[3]))
							id = T_JavaLangAnnotationElementType;
						return;
					case 'I' :
						if (CharOperation.equals(typeName, JAVA_LANG_ANNOTATION_INHERITED[3]))
							id = T_JavaLangAnnotationInherited;
						return;
					case 'R' :
						if (CharOperation.equals(typeName, JAVA_LANG_ANNOTATION_RETENTION[3]))
							id = T_JavaLangAnnotationRetention;
						else if (CharOperation.equals(typeName, JAVA_LANG_ANNOTATION_RETENTIONPOLICY[3]))
							id = T_JavaLangAnnotationRetentionPolicy;
						return;
					case 'T' :
						if (CharOperation.equals(typeName, JAVA_LANG_ANNOTATION_TARGET[3]))
							id = T_JavaLangAnnotationTarget;
						return;
				}				
			}
			break;
	}
}
/*
 * p.X<T extends Y & I, U extends Y> {} -> Lp/X<TT;TU;>;
 */
public char[] computeUniqueKey(boolean isLeaf) {
	if (!isLeaf) return signature();
	return genericTypeSignature();
}
/* Answer the receiver's constant pool name.
*
* NOTE: This method should only be used during/after code gen.
*/

public char[] constantPoolName() /* java/lang/Object */ {
	if (constantPoolName != null) return constantPoolName;
	return constantPoolName = CharOperation.concatWith(compoundName, '/');
}
public String debugName() {
	return (compoundName != null) ? new String(readableName()) : "UNNAMED TYPE"; //$NON-NLS-1$
}
public final int depth() {
	int depth = 0;
	ReferenceBinding current = this;
	while ((current = current.enclosingType()) != null)
		depth++;
	return depth;
}
public boolean detectAnnotationCycle() {
	if ((this.tagBits & TagBits.EndAnnotationCheck) != 0) return false; // already checked
	if ((this.tagBits & TagBits.BeginAnnotationCheck) != 0) return true; // in the middle of checking its methods

	this.tagBits |= TagBits.BeginAnnotationCheck;
	MethodBinding[] currentMethods = methods();
	for (int i = 0, l = currentMethods.length; i < l; i++) {
		TypeBinding returnType = currentMethods[i].returnType.leafComponentType();
		if (returnType.isAnnotationType() && ((ReferenceBinding) returnType).detectAnnotationCycle()) {
			if (this instanceof SourceTypeBinding) {
				MethodDeclaration decl = (MethodDeclaration) currentMethods[i].sourceMethod();
				((SourceTypeBinding) this).scope.problemReporter().annotationCircularity(this, returnType, decl != null ? decl.returnType : null);
			}
			return true;
		}
	}
	this.tagBits |= TagBits.EndAnnotationCheck;
	return false;
}
public final ReferenceBinding enclosingTypeAt(int relativeDepth) {
	ReferenceBinding current = this;
	while (relativeDepth-- > 0 && current != null)
		current = current.enclosingType();
	return current;
}
public int enumConstantCount() {
	int count = 0;
	FieldBinding[] fields = fields();
	for (int i = 0, length = fields.length; i < length; i++) {
		if ((fields[i].modifiers & ClassFileConstants.AccEnum) != 0) count++;
	}
	return count;
}
public int fieldCount() {
	return fields().length;
}
public FieldBinding[] fields() {
	return NoFields;
}
/**
 * Find supertype which erases to a given well-known type, or null if not found
 * (using id avoids triggering the load of well-known type: 73740)
 * NOTE: only works for erasures of well-known types, as random other types may share
 * same id though being distincts.
 *
 */
public ReferenceBinding findSuperTypeErasingTo(int wellKnownErasureID, boolean erasureIsClass) {

    // do not allow type variables to match with erasures for free
    if (this.id == wellKnownErasureID || (!isTypeVariable() && erasure().id == wellKnownErasureID)) return this;

    ReferenceBinding currentType = this;
    // iterate superclass to avoid recording interfaces if searched supertype is class
    if (erasureIsClass) {
		while ((currentType = currentType.superclass()) != null) { 
			if (currentType.id == wellKnownErasureID || (!currentType.isTypeVariable() && currentType.erasure().id == wellKnownErasureID)) return currentType;
		}    
		return null;
    }
	ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
	int lastPosition = -1;
	do {
		ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
		if (itsInterfaces != NoSuperInterfaces) {
			if (++lastPosition == interfacesToVisit.length)
				System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
			interfacesToVisit[lastPosition] = itsInterfaces;
		}
	} while ((currentType = currentType.superclass()) != null);
			
	for (int i = 0; i <= lastPosition; i++) {
		ReferenceBinding[] interfaces = interfacesToVisit[i];
		for (int j = 0, length = interfaces.length; j < length; j++) {
			if ((currentType = interfaces[j]).id == wellKnownErasureID || (!currentType.isTypeVariable() && currentType.erasure().id == wellKnownErasureID))
				return currentType;

			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {
				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}
		}
	}
	return null;
}

/**
 * Find supertype which erases to a given type, or null if not found
 */
public ReferenceBinding findSuperTypeWithSameErasure(TypeBinding otherType) {

    // do not allow type variables to match with erasures for free
    if (!otherType.isTypeVariable()) otherType = otherType.erasure();
    if (this == otherType || (!isTypeVariable() && erasure() == otherType)) return this;
    
    ReferenceBinding currentType = this;
    if (!otherType.isInterface()) {
		while ((currentType = currentType.superclass()) != null) {
			if (currentType == otherType || (!currentType.isTypeVariable() && currentType.erasure() == otherType)) return currentType;
		}
		return null;
    }
	ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
	int lastPosition = -1;
	do {
		ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
		if (itsInterfaces != NoSuperInterfaces) {
			if (++lastPosition == interfacesToVisit.length)
				System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
			interfacesToVisit[lastPosition] = itsInterfaces;
		}
	} while ((currentType = currentType.superclass()) != null);
			
	for (int i = 0; i <= lastPosition; i++) {
		ReferenceBinding[] interfaces = interfacesToVisit[i];
		for (int j = 0, length = interfaces.length; j < length; j++) {
			if ((currentType = interfaces[j]) == otherType || (!currentType.isTypeVariable() && currentType.erasure() == otherType))
				return currentType;

			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {
				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}
		}
	}
	return null;
}

public final int getAccessFlags() {
	return modifiers & ExtraCompilerModifiers.AccJustFlag;
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#getAnnotationTagBits()
 */
public long getAnnotationTagBits() {
	return this.tagBits;
}

public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
	return null;
}

public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
	return null;
}
public FieldBinding getField(char[] fieldName, boolean needResolve) {
	return null;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
 */
public char[] getFileName() {
	return fileName;
}
public ReferenceBinding getMemberType(char[] typeName) {
	ReferenceBinding[] memberTypes = memberTypes();
	for (int i = memberTypes.length; --i >= 0;)
		if (CharOperation.equals(memberTypes[i].sourceName, typeName))
			return memberTypes[i];
	return null;
}
public MethodBinding[] getMethods(char[] selector) {
	return NoMethods;
}
public PackageBinding getPackage() {
	return fPackage;
}
public boolean hasMemberTypes() {
    return false;
}
public TypeVariableBinding getTypeVariable(char[] variableName) {
	TypeVariableBinding[] typeVariables = typeVariables();
	for (int i = typeVariables.length; --i >= 0;)
		if (CharOperation.equals(typeVariables[i].sourceName, variableName))
			return typeVariables[i];
	return null;
}
public int hashCode() {
	// ensure ReferenceBindings hash to the same posiiton as UnresolvedReferenceBindings so they can be replaced without rehashing
	// ALL ReferenceBindings are unique when created so equals() is the same as ==
	return (this.compoundName == null || this.compoundName.length == 0)
		? super.hashCode()
		: CharOperation.hashCode(this.compoundName[this.compoundName.length - 1]);
}

public final boolean hasRestrictedAccess() {
	return (modifiers & ExtraCompilerModifiers.AccRestrictedAccess) != 0;
}

/**
 * Returns true if the two types have an incompatible common supertype,
 * e.g. List<String> and List<Integer>
 */
public boolean hasIncompatibleSuperType(ReferenceBinding otherType) {

    if (this == otherType) return false;
    
    ReferenceBinding currentType = this;
	ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
	ReferenceBinding match;
	int lastPosition = -1;
	do {
		match = otherType.findSuperTypeWithSameErasure(currentType);
		if (match != null) {
			if (!match.isIntersectingWith(currentType))
					return true;
		}
		ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
		if (itsInterfaces != NoSuperInterfaces) {
			if (++lastPosition == interfacesToVisit.length)
				System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
			interfacesToVisit[lastPosition] = itsInterfaces;
		}
	} while ((currentType = currentType.superclass()) != null);
			
	for (int i = 0; i <= lastPosition; i++) {
		ReferenceBinding[] interfaces = interfacesToVisit[i];
		for (int j = 0, length = interfaces.length; j < length; j++) {
			if ((currentType = interfaces[j]) == otherType) return false;
			match = otherType.findSuperTypeWithSameErasure(currentType);
			if (match != null) {
				if (!match.isIntersectingWith(currentType))
						return true;				
			}
			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {
				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}
		}
	}
	return false;
}

/* Answer true if the receiver implements anInterface or is identical to anInterface.
* If searchHierarchy is true, then also search the receiver's superclasses.
*
* NOTE: Assume that anInterface is an interface.
*/
public boolean implementsInterface(ReferenceBinding anInterface, boolean searchHierarchy) {
	if (this == anInterface)
		return true;

	ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
	int lastPosition = -1;
	ReferenceBinding currentType = this;
	do {
		ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
		if (itsInterfaces != NoSuperInterfaces && itsInterfaces != null) { // in code assist cases when source types are added late, may not be finished connecting hierarchy
			if (++lastPosition == interfacesToVisit.length)
				System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
			interfacesToVisit[lastPosition] = itsInterfaces;
		}
	} while (searchHierarchy && (currentType = currentType.superclass()) != null);
			
	for (int i = 0; i <= lastPosition; i++) {
		ReferenceBinding[] interfaces = interfacesToVisit[i];
		for (int j = 0, length = interfaces.length; j < length; j++) {
			if ((currentType = interfaces[j]).isEquivalentTo(anInterface))
				return true;

			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces && itsInterfaces != null) { // in code assist cases when source types are added late, may not be finished connecting hierarchy
				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}
		}
	}
	return false;
}
// Internal method... assume its only sent to classes NOT interfaces

boolean implementsMethod(MethodBinding method) {
	ReferenceBinding type = this;
	while (type != null) {
		MethodBinding[] methods = type.getMethods(method.selector);
		for (int i = methods.length; --i >= 0;)
			if (methods[i].areParametersEqual(method))
				return true;
		type = type.superclass();
	}
	return false;
}
/* Answer true if the receiver is an abstract type
*/

public final boolean isAbstract() {
	return (modifiers & ClassFileConstants.AccAbstract) != 0;
}
public boolean isAnnotationType() {
	return (modifiers & ClassFileConstants.AccAnnotation) != 0;
}
public final boolean isBinaryBinding() {
	return (tagBits & IsBinaryBinding) != 0;
}
public boolean isClass() {
	return (modifiers & (ClassFileConstants.AccInterface | ClassFileConstants.AccAnnotation | ClassFileConstants.AccEnum)) == 0;
}
/*
 * Returns true if the type hierarchy is being connected
 */
public boolean isHierarchyBeingConnected() {
	return (this.tagBits & EndHierarchyCheck) == 0 && (this.tagBits & BeginHierarchyCheck) != 0;
}
/* Answer true if the receiver type can be assigned to the argument type (right)
*/
public boolean isCompatibleWith(TypeBinding otherType) {
    
	if (otherType == this) 
		return true;
	if (otherType.id == T_JavaLangObject) 
		return true;
	// equivalence may allow compatibility with array type through wildcard bound
	if (this.isEquivalentTo(otherType)) 
		return true;
	switch (otherType.kind()) {
		case Binding.WILDCARD_TYPE :
			return false; // should have passed equivalence check above if wildcard
		case Binding.TYPE_PARAMETER :
			// check compatibility with capture of ? super X
			if (otherType.isCapture()) {
				CaptureBinding otherCapture = (CaptureBinding) otherType;
				TypeBinding otherLowerBound;
				if ((otherLowerBound = otherCapture.lowerBound) != null) {
					if (otherLowerBound.isArrayType()) return false;
					return this.isCompatibleWith(otherLowerBound);
				}
			}
		case Binding.GENERIC_TYPE :
		case Binding.TYPE :
		case Binding.PARAMETERIZED_TYPE :
		case Binding.RAW_TYPE :
			switch (this.kind()) {
				case Binding.GENERIC_TYPE :
				case Binding.PARAMETERIZED_TYPE :
				case Binding.RAW_TYPE :
					if (this.erasure() == otherType.erasure())
						return false; // should have passed equivalence check above if same erasure
			}
			ReferenceBinding otherReferenceType = (ReferenceBinding) otherType;
			if (otherReferenceType.isInterface()) // could be annotation type
				return implementsInterface(otherReferenceType, true);
			if (this.isInterface())  // Explicit conversion from an interface to a class is not allowed
				return false;
			return otherReferenceType.isSuperclassOf(this);
		default :
			return false;
	}
}

/* Answer true if the receiver has default visibility
*/
public final boolean isDefault() {
	return (modifiers & (ClassFileConstants.AccPublic | ClassFileConstants.AccProtected | ClassFileConstants.AccPrivate)) == 0;
}

/* Answer true if the receiver is a deprecated type
*/
public final boolean isDeprecated() {
	return (modifiers & ClassFileConstants.AccDeprecated) != 0;
}
public boolean isEnum() {
	return (modifiers & ClassFileConstants.AccEnum) != 0;
}
/* Answer true if the receiver is final and cannot be subclassed
*/
public final boolean isFinal() {
	return (modifiers & ClassFileConstants.AccFinal) != 0;
}
public boolean isInterface() {
	// consider strict interfaces and annotation types
	return (modifiers & ClassFileConstants.AccInterface) != 0;
}
	
/* Answer true if the receiver has private visibility
*/
public final boolean isPrivate() {
	return (modifiers & ClassFileConstants.AccPrivate) != 0;
}
/* Answer true if the receiver has private visibility and is used locally
*/

public final boolean isUsed() {
	return (modifiers & ExtraCompilerModifiers.AccLocallyUsed) != 0;
}
/* Answer true if the receiver has protected visibility
*/

public final boolean isProtected() {
	return (modifiers & ClassFileConstants.AccProtected) != 0;
}
/* Answer true if the receiver has public visibility
*/

public final boolean isPublic() {
	return (modifiers & ClassFileConstants.AccPublic) != 0;
}
/* Answer true if the receiver is a static member type (or toplevel)
 */

public final boolean isStatic() {
	return (modifiers & (ClassFileConstants.AccStatic | ClassFileConstants.AccInterface)) != 0 || (tagBits & IsNestedType) == 0;
}
/* Answer true if all float operations must adher to IEEE 754 float/double rules
*/

public final boolean isStrictfp() {
	return (modifiers & ClassFileConstants.AccStrictfp) != 0;
}
/* Answer true if the receiver is in the superclass hierarchy of aType
*
* NOTE: Object.isSuperclassOf(Object) -> false
*/

public boolean isSuperclassOf(ReferenceBinding otherType) {
	while ((otherType = otherType.superclass()) != null) {
		if (otherType.isEquivalentTo(this)) return true;
	}
	return false;
}

/**
 * JLS 11.5 ensures that Throwable, Exception, RuntimeException and Error are directly connected.
 * (Throwable<- Exception <- RumtimeException, Throwable <- Error). Thus no need to check #isCompatibleWith
 * but rather check in type IDs so as to avoid some eager class loading for JCL writers.
 * When 'includeSupertype' is true, answers true if the given type can be a supertype of some unchecked exception
 * type (i.e. Throwable or Exception).
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#isUncheckedException(boolean)
 */
public boolean isUncheckedException(boolean includeSupertype) {
	switch (this.id) {
			case TypeIds.T_JavaLangError :
			case TypeIds.T_JavaLangRuntimeException :
				return true;
			case TypeIds.T_JavaLangThrowable :
			case TypeIds.T_JavaLangException :
				return includeSupertype;
	}
	ReferenceBinding current = this;
	while ((current = current.superclass()) != null) {
		switch (current.id) {
			case TypeIds.T_JavaLangError :
			case TypeIds.T_JavaLangRuntimeException :
				return true;
			case TypeIds.T_JavaLangThrowable :
			case TypeIds.T_JavaLangException :
				return false;
		}
	}
	return false;
}

/* Answer true if the receiver is deprecated (or any of its enclosing types)
*/
public final boolean isViewedAsDeprecated() {
	return (modifiers & (ClassFileConstants.AccDeprecated | ExtraCompilerModifiers.AccDeprecatedImplicitly)) != 0;
}
public ReferenceBinding[] memberTypes() {
	return NoMemberTypes;
}

public MethodBinding[] methods() {
	return NoMethods;
}
public final ReferenceBinding outermostEnclosingType() {
	ReferenceBinding current = this;
	while (true) {
		ReferenceBinding last = current;
		if ((current = current.enclosingType()) == null) 
			return last;
	}
}
/**
* Answer the source name for the type.
* In the case of member types, as the qualified name from its top level type.
* For example, for a member type N defined inside M & A: "A.M.N".
*/

public char[] qualifiedSourceName() {
	if (isMemberType())
		return CharOperation.concat(enclosingType().qualifiedSourceName(), sourceName(), '.');
	return sourceName();
}

public char[] readableName() /*java.lang.Object,  p.X<T> */ {
    char[] readableName;
	if (isMemberType()) {
		readableName = CharOperation.concat(enclosingType().readableName(), sourceName, '.');
	} else {
		readableName = CharOperation.concatWith(compoundName, '.');
	}
	TypeVariableBinding[] typeVars;
	if ((typeVars = this.typeVariables()) != NoTypeVariables) {
	    StringBuffer nameBuffer = new StringBuffer(10);
	    nameBuffer.append(readableName).append('<');
	    for (int i = 0, length = typeVars.length; i < length; i++) {
	        if (i > 0) nameBuffer.append(',');
	        nameBuffer.append(typeVars[i].readableName());
	    }
	    nameBuffer.append('>');
		int nameLength = nameBuffer.length();
		readableName = new char[nameLength];
		nameBuffer.getChars(0, nameLength, readableName, 0);  
	}
	return readableName;
}

public char[] shortReadableName() /*Object*/ {
    char[] shortReadableName;
	if (isMemberType()) {
		shortReadableName = CharOperation.concat(enclosingType().shortReadableName(), sourceName, '.');
	} else {
		shortReadableName = this.sourceName;
	}
	TypeVariableBinding[] typeVars;
	if ((typeVars = this.typeVariables()) != NoTypeVariables) {
	    StringBuffer nameBuffer = new StringBuffer(10);
	    nameBuffer.append(shortReadableName).append('<');
	    for (int i = 0, length = typeVars.length; i < length; i++) {
	        if (i > 0) nameBuffer.append(',');
	        nameBuffer.append(typeVars[i].shortReadableName());
	    }
	    nameBuffer.append('>');
		int nameLength = nameBuffer.length();
		shortReadableName = new char[nameLength];
		nameBuffer.getChars(0, nameLength, shortReadableName, 0);	    
	}
	return shortReadableName;
}

/* Answer the receiver's signature.
*
* NOTE: This method should only be used during/after code gen.
*/

public char[] signature() /* Ljava/lang/Object; */ {
	if (signature != null)
		return signature;

	return signature = CharOperation.concat('L', constantPoolName(), ';');
}
public char[] sourceName() {
	return sourceName;
}

public ReferenceBinding superclass() {
	return null;
}
public ReferenceBinding[] superInterfaces() {
	return NoSuperInterfaces;
}
public ReferenceBinding[] syntheticEnclosingInstanceTypes() {
	if (isStatic()) return null;

	ReferenceBinding enclosingType = enclosingType();
	if (enclosingType == null)
		return null;
	return new ReferenceBinding[] {enclosingType};
}
public SyntheticArgumentBinding[] syntheticOuterLocalVariables() {
	return null;		// is null if no enclosing instances are required
}

MethodBinding[] unResolvedMethods() { // for the MethodVerifier so it doesn't resolve types
	return methods();
}
}
