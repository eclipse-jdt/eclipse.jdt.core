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

import java.util.Map;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;

/*
 * Not all fields defined by this type (& its subclasses) are initialized when it is created.
 * Some are initialized only when needed.
 *
 * Accessors have been provided for some public fields so all TypeBindings have the same API...
 * but access public fields directly whenever possible.
 * Non-public fields have accessors which should be used everywhere you expect the field to be initialized.
 *
 * null is NOT a valid value for a non-public field... it just means the field is not initialized.
 */
abstract public class TypeBinding extends Binding implements BaseTypes, TagBits, TypeConstants, TypeIds {
	public int id = NoId;
	public long tagBits = 0; // See values in the interface TagBits below
/**
 * Match a well-known type id to its binding
 */
public static final TypeBinding wellKnownType(Scope scope, int id) {
		switch (id) { 
			case T_boolean :
				return BooleanBinding;
			case T_byte :
				return ByteBinding;
			case T_char :
				return CharBinding;
			case T_short :
				return ShortBinding;
			case T_double :
				return DoubleBinding;
			case T_float :
				return FloatBinding;
			case T_int :
				return IntBinding;
			case T_long :
				return LongBinding;
			case T_JavaLangObject :
				return scope.getJavaLangObject();
			case T_JavaLangString :
				return scope.getJavaLangString();
			default : 
				return null;
		}
	}
/* API
 * Answer the receiver's binding type from Binding.BindingID.
 */

public int kind() {
	return Binding.TYPE;
}
/* Answer true if the receiver can be instantiated
 */
public boolean canBeInstantiated() {
	return !isBaseType();
}

/**
 * Perform capture conversion on a given type (only effective on parameterized type with wildcards)
 */
public TypeBinding capture(Scope scope, int position) {
	return this;
}

/**
 * Collect the substitutes into a map for certain type variables inside the receiver type
 * e.g.   Collection<T>.findSubstitute(T, Collection<List<X>>):   T --> List<X>
 */
public void collectSubstitutes(Scope scope, TypeBinding otherType, Map substitutes, int constraint) {
    // no substitute by default
}
/**
 *  Answer the receiver's constant pool name.
 *  NOTE: This method should only be used during/after code gen.
 *  e.g. 'java/lang/Object' 
 */
public abstract char[] constantPoolName();

public String debugName() {
	return new String(readableName());
}
/*
 * Answer the receiver's dimensions - 0 for non-array types
 */
public int dimensions(){
	return 0;
}
/* Answer the receiver's enclosing type... null if the receiver is a top level type.
*/

public ReferenceBinding enclosingType() {
	return null;
}
public TypeBinding erasure() {
    return this;
}
/**
 * Returns the type to use for generic cast, or null if none required
 */
public TypeBinding genericCast(TypeBinding otherType) {
    if (this == otherType) return null;
	TypeBinding otherErasure = otherType.erasure();
	if (otherErasure == this.erasure()) return null;
	return otherErasure;
}

/**
 * Answer the receiver classfile signature.
 * Arrays & base types do not distinguish between signature() & constantPoolName().
 * NOTE: This method should only be used during/after code gen.
 */
public char[] genericTypeSignature() {
    return signature();
}
public abstract PackageBinding getPackage();

public final boolean isAnonymousType() {
	return (tagBits & IsAnonymousType) != 0;
}
public boolean isAnnotationType() {
	return false;
}
/* Answer true if the receiver is an array
*/
public final boolean isArrayType() {
	return (tagBits & IsArrayType) != 0;
}
/* Answer true if the receiver is a base type
*/
public final boolean isBaseType() {
	return (tagBits & IsBaseType) != 0;
}

	
/**
 *  Returns true if parameterized type AND not of the form List<?>
 */
public boolean isBoundParameterizedType() {
	return (this.tagBits & TagBits.IsBoundParameterizedType) != 0;
}

/**
 * Returns true if the type is the capture of some wildcard
 */
public boolean isCapture() {
    return false;
}

public boolean isClass() {
	return false;
}
/* Answer true if the receiver type can be assigned to the argument type (right)
*/
public abstract boolean isCompatibleWith(TypeBinding right);

public boolean isEnum() {
	return false;
}
/**
 * Returns true if a type is identical to another one,
 * or for generic types, true if compared to its raw type.
 */
public boolean isEquivalentTo(TypeBinding otherType) {
    if (this == otherType) return true;
    if (otherType == null) return false;
    if (otherType.isWildcard()) // wildcard
		return ((WildcardBinding) otherType).boundCheck(this);
	return false;
}

public boolean isGenericType() {
    return false;
}

/* Answer true if the receiver's hierarchy has problems (always false for arrays & base types)
*/
public final boolean isHierarchyInconsistent() {
	return (tagBits & HierarchyHasProblems) != 0;
}
public boolean isInterface() {
	return false;
}
/**
 * Returns true if a type is intersecting with another one,
 */
public boolean isIntersectingWith(TypeBinding otherType) {
    return this == otherType;
}
public final boolean isLocalType() {
	return (tagBits & IsLocalType) != 0;
}

public final boolean isMemberType() {
	return (tagBits & IsMemberType) != 0;
}

public final boolean isNestedType() {
	return (tagBits & IsNestedType) != 0;
}
public final boolean isNumericType() {
	switch (id) {
		case T_int :
		case T_float :
		case T_double :
		case T_short :
		case T_byte :
		case T_long :
		case T_char :
			return true;
		default :
			return false;
	}
}

/**
 * Returns true if the type is parameterized, e.g. List<String>
 */
public boolean isParameterizedType() {
    return false;
}

/**
 * Returns true if the two types are statically known to be different at compile-time,
 * e.g. a type variable is not provably known to be distinct from another type
 */
public boolean isProvablyDistinctFrom(TypeBinding otherType, int depth) {
	if (this == otherType) return false;
	if (depth > 1) return true;
	switch (otherType.kind()) {
		case Binding.TYPE_PARAMETER :
		case Binding.WILDCARD_TYPE :
			return false;
	}
	switch(kind()) {
		
		case Binding.TYPE_PARAMETER :
		case Binding.WILDCARD_TYPE :
			return false;
			
		case Binding.PARAMETERIZED_TYPE :
			ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding) this;
			if (parameterizedType.type.isProvablyDistinctFrom(otherType.erasure(), depth)) return true;
			switch (otherType.kind()) {
				case Binding.GENERIC_TYPE :
				case Binding.RAW_TYPE :
					return false;
				case Binding.PARAMETERIZED_TYPE :
					TypeBinding[] arguments = parameterizedType.arguments;
					if (arguments == null) return false;
					ParameterizedTypeBinding otherParameterizedType = (ParameterizedTypeBinding) otherType;
					TypeBinding[] otherArguments = otherParameterizedType.arguments;
					if (otherArguments == null) return false;
					for (int i = 0, length = arguments.length; i < length; i++) {
						if (arguments[i].isProvablyDistinctFrom(otherArguments[i], depth+1)) return true;
					}
					return false;
					
			}
			break;

		case Binding.RAW_TYPE :
			return this.erasure().isProvablyDistinctFrom(otherType.erasure(), 0);
			
		case Binding.GENERIC_TYPE :
			return this != otherType.erasure();
	}
	return this != otherType;
}

public boolean isRawType() {
    return false;
}

/**
 * JLS(3) 4.7
 */
public boolean isReifiable() {
	
	TypeBinding leafType = leafComponentType();
	if (!(leafType instanceof ReferenceBinding)) 
		return true;
	ReferenceBinding current = (ReferenceBinding) leafType;
	do {
		switch(current.kind()) {
			
			case Binding.TYPE_PARAMETER :
			case Binding.WILDCARD_TYPE :
			case Binding.GENERIC_TYPE :
				return false;
				
			case Binding.PARAMETERIZED_TYPE :
				if (isBoundParameterizedType()) 
					return false;
				break;
				
			case Binding.RAW_TYPE :
				return true;
		}
		if (current.isStatic()) 
			return true;
	} while ((current = current.enclosingType()) != null);
	return true;
}

// JLS3: 4.5.1.1
public boolean isTypeArgumentContainedBy(TypeBinding otherArgument) {
	if (this == otherArgument)
		return true;
	TypeBinding lowerBound = this;
	TypeBinding upperBound = this;
	if (isWildcard()) {
		WildcardBinding wildcard = (WildcardBinding) this;
		switch(wildcard.boundKind) {
			case Wildcard.EXTENDS :
				upperBound = wildcard.bound;
				lowerBound = null;
				break;
			case Wildcard. SUPER :
				upperBound = wildcard;
				lowerBound = wildcard.bound;
				break;
			case Wildcard.UNBOUND :
				upperBound = wildcard;
				lowerBound = null;
		}
	}
	if (otherArgument.isWildcard()) {
		WildcardBinding otherWildcard = (WildcardBinding) otherArgument;
		if (otherWildcard.otherBounds != null) return false; // not a true wildcard (intersection type)
		switch(otherWildcard.boundKind) {
			case Wildcard.EXTENDS:
				if (otherWildcard.bound == this) return true; // ? extends T  <=  ? extends ? extends T
				return upperBound != null && upperBound.isCompatibleWith(otherWildcard.bound);

			case Wildcard.SUPER :
				if (otherWildcard.bound == this) return true; // ? super T  <=  ? super ? super T
				return lowerBound != null && otherWildcard.bound.isCompatibleWith(lowerBound);

			case Wildcard.UNBOUND :
				return true;
		}
	}
	return false;
}

/**
 * Returns false if two given types could not intersect as argument types:
 * List<Throwable> & List<Runnable> --> false
 * List<? extends Throwable> & List<? extends Runnable> --> true
 * List<? extends String> & List<? extends Runnable> --> false
 */
public boolean isTypeArgumentIntersecting(TypeBinding otherArgument) {
	if (this == otherArgument)
		return true;
	if (this.isTypeVariable() || otherArgument.isTypeVariable())
		return true;
	if (this.isWildcard()) {
		if (!otherArgument.isWildcard()) {
			WildcardBinding wildcard = (WildcardBinding) this;
			switch(wildcard.boundKind) {
				case Wildcard.EXTENDS :
					return otherArgument.isCompatibleWith(wildcard.bound);
				case Wildcard. SUPER :
					return wildcard.bound.isCompatibleWith(otherArgument);
				case Wildcard.UNBOUND :
				default:
					return true;
			}
		}
	} else if (otherArgument.isWildcard()) {
		WildcardBinding otherWildcard = (WildcardBinding) otherArgument;
		switch(otherWildcard.boundKind) {
			case Wildcard.EXTENDS :
				return this.isCompatibleWith(otherWildcard.bound);
			case Wildcard. SUPER :
				return otherWildcard.bound.isCompatibleWith(this);
			case Wildcard.UNBOUND :
			default:
				return true;
		}
	}
	TypeBinding lowerBound1 = null;
	TypeBinding upperBound1 = null;
	WildcardBinding wildcard = (WildcardBinding) this;
	switch(wildcard.boundKind) {
		case Wildcard.EXTENDS :
			upperBound1 = wildcard.bound;
			break;
		case Wildcard. SUPER :
			lowerBound1 = wildcard.bound;
			break;
		case Wildcard.UNBOUND :
	}

	TypeBinding lowerBound2 = null;
	TypeBinding upperBound2 = null;
	WildcardBinding otherWildcard = (WildcardBinding) otherArgument;
	switch(otherWildcard.boundKind) {
		case Wildcard.EXTENDS :
			upperBound2 = otherWildcard.bound;
			break;
		case Wildcard. SUPER :
			lowerBound2 = otherWildcard.bound;
			break;
		case Wildcard.UNBOUND :
	}
	if (lowerBound1 != null) {
		if (lowerBound2 != null) {
			return true; // Object could always be a candidate
			
		} else if (upperBound2 != null) {
			return lowerBound1.isCompatibleWith(upperBound2);
		} else {
			return true;
		}
	} else if (upperBound1 != null) {
		if (lowerBound2 != null) {
			return lowerBound2.isCompatibleWith(upperBound1);

		} else if (upperBound2 != null) {
			if (upperBound1.isInterface()) {
				if (upperBound2.isInterface())
					return true;
				if (upperBound2.isArrayType() || ((upperBound2 instanceof ReferenceBinding) && ((ReferenceBinding)upperBound2).isFinal())) {
					return upperBound2.isCompatibleWith(upperBound1);
				}
				return true;
			} else if (upperBound2.isInterface()) {
				if (upperBound1.isArrayType() || ((upperBound1 instanceof ReferenceBinding) && ((ReferenceBinding)upperBound1).isFinal())) {
					return upperBound1.isCompatibleWith(upperBound2);
				}
			}
			return true;
		} else {
			return true;
		}
	} else {
		return true;
	}
}
/**
 * Returns true if the type was declared as a type variable
 */
public boolean isTypeVariable() {
    return false;
}
/**
 * Returns true if wildcard type of the form '?' (no bound)
 */
public boolean isUnboundWildcard() {
	return false;
}

/**
 * Returns true if the type is a subclass of java.lang.Error or java.lang.RuntimeException
 */
public boolean isUncheckedException(boolean includeSupertype) {
	return false;
}

/**
 * Returns true if the type is a wildcard
 */
public boolean isWildcard() {
    return false;
}
	
/**
 * Meant to be invoked on compatible types, to figure if unchecked conversion is necessary
 */
public boolean needsUncheckedConversion(TypeBinding targetType) {

	if (this == targetType) return false;
	targetType = targetType.leafComponentType();
	if (!(targetType instanceof ReferenceBinding)) 
		return false;

	TypeBinding currentType = this.leafComponentType();
	if (!(currentType instanceof ReferenceBinding))
		return false;
	
	ReferenceBinding compatible = ((ReferenceBinding)currentType).findSuperTypeErasingTo((ReferenceBinding)targetType.erasure());
	if (compatible == null) 
		return false;

	while (compatible.isRawType()) {
		if (targetType.isBoundParameterizedType() || targetType.isGenericType()) {
			return true;
		}
		if (compatible.isStatic()) break;
		if ((compatible = compatible.enclosingType()) == null) break;
		if ((targetType = targetType.enclosingType()) == null) break;
	}
	return false;
}

public TypeBinding leafComponentType(){
	return this;
}

/**
 * Answer the qualified name of the receiver's package separated by periods
 * or an empty string if its the default package.
 *
 * For example, {java.util.Hashtable}.
 */

public char[] qualifiedPackageName() {
	PackageBinding packageBinding = getPackage();
	return packageBinding == null  || packageBinding.compoundName == CharOperation.NO_CHAR_CHAR
		? CharOperation.NO_CHAR
		: packageBinding.readableName();
}
/**
* Answer the source name for the type.
* In the case of member types, as the qualified name from its top level type.
* For example, for a member type N defined inside M & A: "A.M.N".
*/

public abstract char[] qualifiedSourceName();

/**
 * Answer the receiver classfile signature.
 * Arrays & base types do not distinguish between signature() & constantPoolName().
 * NOTE: This method should only be used during/after code gen.
 */
public char[] signature() {
	return constantPoolName();
}

public abstract char[] sourceName();

public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment environment) {
	// subclasses must override if they wrap another type binding
}
public TypeVariableBinding[] typeVariables() {
	return NoTypeVariables;
}
}
