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
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

/*
Not all fields defined by this type are initialized when it is created.
Some are initialized only when needed.

Accessors have been provided for some public fields so all TypeBindings have the same API...
but access public fields directly whenever possible.
Non-public fields have accessors which should be used everywhere you expect the field to be initialized.

null is NOT a valid value for a non-public field... it just means the field is not initialized.
*/

public final class BinaryTypeBinding extends ReferenceBinding {

// all of these fields are ONLY guaranteed to be initialized if accessed using their public accessor method
private ReferenceBinding superclass;
private ReferenceBinding enclosingType;
private ReferenceBinding[] superInterfaces;
private FieldBinding[] fields;
private MethodBinding[] methods;
private ReferenceBinding[] memberTypes;
protected TypeVariableBinding[] typeVariables;

// For the link with the principle structure
private LookupEnvironment environment;

public static ReferenceBinding resolveType(ReferenceBinding type, LookupEnvironment environment, boolean convertGenericToRawType) {
	if (type instanceof UnresolvedReferenceBinding)
		return ((UnresolvedReferenceBinding) type).resolve(environment, convertGenericToRawType);
	if (type.isParameterizedType())
		return ((ParameterizedTypeBinding) type).resolve();
	if (type.isWildcard())
		return ((WildcardBinding) type).resolve();

	if (convertGenericToRawType && type.isGenericType()) // raw reference to generic ?
	    return environment.createRawType(type, null);
	return type;
}
public static TypeBinding resolveType(TypeBinding type, LookupEnvironment environment, ParameterizedTypeBinding parameterizedType, int rank) {
	if (type instanceof UnresolvedReferenceBinding)
		return ((UnresolvedReferenceBinding) type).resolve(environment, parameterizedType == null);
	if (type.isParameterizedType())
		return ((ParameterizedTypeBinding) type).resolve();
	if (type.isWildcard())
		return ((WildcardBinding) type).resolve();
	if (type.isArrayType())
		resolveType(((ArrayBinding) type).leafComponentType, environment, parameterizedType, rank);

	if (parameterizedType == null && type.isGenericType()) // raw reference to generic ?
	    return environment.createRawType((ReferenceBinding) type, null);
	return type;
}
// resolve hierarchy types in 2 steps by first resolving any UnresolvedTypes
static ReferenceBinding resolveUnresolvedType(ReferenceBinding type, LookupEnvironment environment, boolean convertGenericToRawType) {
	if (type instanceof UnresolvedReferenceBinding)
		return ((UnresolvedReferenceBinding) type).resolve(environment, convertGenericToRawType);

	if (type.isParameterizedType())
		resolveUnresolvedType(((ParameterizedTypeBinding) type).type, environment, false); // still part of parameterized type ref
	else if (type.isWildcard())
		resolveType(((WildcardBinding) type).genericType, environment, null, 0);
	return type;
}


public BinaryTypeBinding(PackageBinding packageBinding, IBinaryType binaryType, LookupEnvironment environment) {
	this.compoundName = CharOperation.splitOn('/', binaryType.getName());
	computeId();

	this.tagBits |= IsBinaryBinding;
	this.environment = environment;
	this.fPackage = packageBinding;
	this.fileName = binaryType.getFileName();
	this.typeVariables = NoTypeVariables;

	// source name must be one name without "$".
	char[] possibleSourceName = this.compoundName[this.compoundName.length - 1];
	int start = CharOperation.lastIndexOf('$', possibleSourceName) + 1;
	if (start == 0) {
		this.sourceName = possibleSourceName;
	} else {
		this.sourceName = new char[possibleSourceName.length - start];
		System.arraycopy(possibleSourceName, start, this.sourceName, 0, this.sourceName.length);
	}

	this.modifiers = binaryType.getModifiers();
	if (binaryType.isInterface())
		this.modifiers |= AccInterface;
		
	if (binaryType.isAnonymous()) {
		this.tagBits |= AnonymousTypeMask;
	} else if (binaryType.isLocal()) {
		this.tagBits |= LocalTypeMask;
	} else if (binaryType.isMember()) {
		this.tagBits |= MemberTypeMask;
	}
}

public FieldBinding[] availableFields() {
	FieldBinding[] availableFields = new FieldBinding[fields.length];
	int count = 0;
	
	for (int i = 0; i < fields.length;i++) {
		try {
			availableFields[count] = resolveTypeFor(fields[i]);
			count++;
		} catch (AbortCompilation a){
			// silent abort
		}
	}
	
	System.arraycopy(availableFields, 0, availableFields = new FieldBinding[count], 0, count);
	return availableFields;
}

public MethodBinding[] availableMethods() {
	if ((modifiers & AccUnresolved) == 0)
		return methods;
		
	MethodBinding[] availableMethods = new MethodBinding[methods.length];
	int count = 0;
	
	for (int i = 0; i < methods.length;i++) {
		try {
			availableMethods[count] = resolveTypesFor(methods[i]);
			count++;
		} catch (AbortCompilation a){
			// silent abort
		}
	}
	System.arraycopy(availableMethods, 0, availableMethods = new MethodBinding[count], 0, count);
	return availableMethods;
}

void cachePartsFrom(IBinaryType binaryType, boolean needFieldsAndMethods) {
	
	// default initialization for super-interfaces early, in case some aborting compilation error occurs,
	// and still want to use binaries passed that point (e.g. type hierarchy resolver, see bug 63748).
	this.superInterfaces = NoSuperInterfaces;
	
	// need enclosing type to access type variables
	char[] enclosingTypeName = binaryType.getEnclosingTypeName();
	if (enclosingTypeName != null) {
		// attempt to find the enclosing type if it exists in the cache (otherwise - resolve it when requested)
		this.enclosingType = environment.getTypeFromConstantPoolName(enclosingTypeName, 0, -1, true); // pretend parameterized to avoid raw
		this.tagBits |= MemberTypeMask;   // must be a member type not a top-level or local type
		this.tagBits |= 	HasUnresolvedEnclosingType;
		if (this.enclosingType().isStrictfp())
			this.modifiers |= AccStrictfp;
		if (this.enclosingType().isDeprecated())
			this.modifiers |= AccDeprecatedImplicitly;
	}

	boolean checkGenericSignatures = environment.options.sourceLevel >= ClassFileConstants.JDK1_5;
	char[] typeSignature = checkGenericSignatures ? binaryType.getGenericSignature() : null;
	if (typeSignature == null) {
		char[] superclassName = binaryType.getSuperclassName();
		if (superclassName != null) {
			// attempt to find the superclass if it exists in the cache (otherwise - resolve it when requested)
			this.superclass = environment.getTypeFromConstantPoolName(superclassName, 0, -1, false);
			this.tagBits |= 	HasUnresolvedSuperclass;
		}

		this.superInterfaces = NoSuperInterfaces;
		char[][] interfaceNames = binaryType.getInterfaceNames();
		if (interfaceNames != null) {
			int size = interfaceNames.length;
			if (size > 0) {
				this.superInterfaces = new ReferenceBinding[size];
				for (int i = 0; i < size; i++)
					// attempt to find each superinterface if it exists in the cache (otherwise - resolve it when requested)
					this.superInterfaces[i] = environment.getTypeFromConstantPoolName(interfaceNames[i], 0, -1, false);
				this.tagBits |= 	HasUnresolvedSuperinterfaces;
			}
		}
	} else {
		// ClassSignature = ParameterPart(optional) super_TypeSignature interface_signature
		SignatureWrapper wrapper = new SignatureWrapper(typeSignature);
		if (wrapper.signature[wrapper.start] == '<') {
			// ParameterPart = '<' ParameterSignature(s) '>'
			wrapper.start++; // skip '<'
			int rank = 0;
			do {
				TypeVariableBinding variable = createTypeVariable(wrapper, rank);
				variable.fPackage = this.fPackage;
				System.arraycopy(this.typeVariables, 0, this.typeVariables = new TypeVariableBinding[rank + 1], 0, rank);
				this.typeVariables[rank++] = variable;
				initializeTypeVariable(variable, this.typeVariables, wrapper);
			} while (wrapper.signature[wrapper.start] != '>');
			wrapper.start++; // skip '>'
			this.tagBits |=  HasUnresolvedTypeVariables;
			this.modifiers |= AccGenericSignature;
		}

		// attempt to find the superclass if it exists in the cache (otherwise - resolve it when requested)
		this.superclass = (ReferenceBinding) environment.getTypeFromTypeSignature(wrapper, NoTypeVariables, this);
		this.tagBits |= 	HasUnresolvedSuperclass;

		this.superInterfaces = NoSuperInterfaces;
		if (!wrapper.atEnd()) {
			// attempt to find each superinterface if it exists in the cache (otherwise - resolve it when requested)
			java.util.ArrayList types = new java.util.ArrayList(2);
			do {
				types.add(environment.getTypeFromTypeSignature(wrapper, NoTypeVariables, this));
			} while (!wrapper.atEnd());
			this.superInterfaces = new ReferenceBinding[types.size()];
			types.toArray(this.superInterfaces);
			this.tagBits |= 	HasUnresolvedSuperinterfaces;
		}
	}

	this.memberTypes = NoMemberTypes;
	IBinaryNestedType[] memberTypeStructures = binaryType.getMemberTypes();
	if (memberTypeStructures != null) {
		int size = memberTypeStructures.length;
		if (size > 0) {
			this.memberTypes = new ReferenceBinding[size];
			for (int i = 0; i < size; i++)
				// attempt to find each member type if it exists in the cache (otherwise - resolve it when requested)
				this.memberTypes[i] = environment.getTypeFromConstantPoolName(memberTypeStructures[i].getName(), 0, -1, false);
			this.tagBits |= 	HasUnresolvedMemberTypes;
		}
	}

	if (needFieldsAndMethods) {
		createFields(binaryType.getFields(), checkGenericSignatures);
		createMethods(binaryType.getMethods(), checkGenericSignatures);
	} else { // protect against incorrect use of the needFieldsAndMethods flag, see 48459
		this.fields = NoFields;
		this.methods = NoMethods;
	}
}
private void createFields(IBinaryField[] iFields, boolean checkGenericSignatures) {
	this.fields = NoFields;
	if (iFields != null) {
		int size = iFields.length;
		if (size > 0) {
			this.fields = new FieldBinding[size];
			for (int i = 0; i < size; i++) {
				IBinaryField field = iFields[i];
				char[] fieldSignature = checkGenericSignatures ? field.getGenericSignature() : null;
				TypeBinding type = fieldSignature == null
					? environment.getTypeFromSignature(field.getTypeName(), 0, -1, false, this)
					: environment.getTypeFromTypeSignature(new SignatureWrapper(fieldSignature), NoTypeVariables, this);
				this.fields[i] =
					new FieldBinding(
						field.getName(),
						type,
						field.getModifiers() | AccUnresolved,
						this,
						field.getConstant());
			}
		}
	}
}
private MethodBinding createMethod(IBinaryMethod method, boolean checkGenericSignatures) {
	int methodModifiers = method.getModifiers() | AccUnresolved;
	ReferenceBinding[] exceptions = NoExceptions;
	TypeBinding[] parameters = NoParameters;
	TypeVariableBinding[] typeVars = NoTypeVariables;
	TypeBinding returnType = null;

	char[] methodSignature = checkGenericSignatures ? method.getGenericSignature() : null;
	if (methodSignature == null) { // no generics
		char[] methodDescriptor = method.getMethodDescriptor();   // of the form (I[Ljava/jang/String;)V
		int numOfParams = 0;
		char nextChar;
		int index = 0;   // first character is always '(' so skip it
		while ((nextChar = methodDescriptor[++index]) != ')') {
			if (nextChar != '[') {
				numOfParams++;
				if (nextChar == 'L')
					while ((nextChar = methodDescriptor[++index]) != ';'){/*empty*/}
			}
		}

		// Ignore synthetic argument for member types.
		int startIndex = (method.isConstructor() && isMemberType() && !isStatic()) ? 1 : 0;
		int size = numOfParams - startIndex;
		if (size > 0) {
			parameters = new TypeBinding[size];
			index = 1;
			int end = 0;   // first character is always '(' so skip it
			for (int i = 0; i < numOfParams; i++) {
				while ((nextChar = methodDescriptor[++end]) == '['){/*empty*/}
				if (nextChar == 'L')
					while ((nextChar = methodDescriptor[++end]) != ';'){/*empty*/}
	
				if (i >= startIndex)   // skip the synthetic arg if necessary
					parameters[i - startIndex] = environment.getTypeFromSignature(methodDescriptor, index, end, false, this);
				index = end + 1;
			}
		}

		char[][] exceptionTypes = method.getExceptionTypeNames();
		if (exceptionTypes != null) {
			size = exceptionTypes.length;
			if (size > 0) {
				exceptions = new ReferenceBinding[size];
				for (int i = 0; i < size; i++)
					exceptions[i] = environment.getTypeFromConstantPoolName(exceptionTypes[i], 0, -1, false);
			}
		}

		if (!method.isConstructor())
			returnType = environment.getTypeFromSignature(methodDescriptor, index + 1, -1, false, this);   // index is currently pointing at the ')'
	} else {
		// MethodTypeSignature = ParameterPart(optional) '(' TypeSignatures ')' return_typeSignature ['^' TypeSignature (optional)]
		SignatureWrapper wrapper = new SignatureWrapper(methodSignature);
		if (wrapper.signature[wrapper.start] == '<') {
			// <A::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TA;>;)TA;
			// ParameterPart = '<' ParameterSignature(s) '>'
			wrapper.start++; // skip '<'
			int rank = 0;
			do {
				TypeVariableBinding variable = createTypeVariable(wrapper, rank);
				System.arraycopy(typeVars, 0, typeVars = new TypeVariableBinding[rank + 1], 0, rank);
				typeVars[rank++] = variable;
				initializeTypeVariable(variable,typeVars, wrapper);
			} while (wrapper.signature[wrapper.start] != '>');
			wrapper.start++; // skip '>'
		}

		if (wrapper.signature[wrapper.start] == '(') {
			wrapper.start++; // skip '('
			if (wrapper.signature[wrapper.start] == ')') {
				wrapper.start++; // skip ')'
			} else {
				java.util.ArrayList types = new java.util.ArrayList(2);
				int startIndex = (method.isConstructor() && isMemberType() && !isStatic()) ? 1 : 0;
				if (startIndex == 1)
					environment.getTypeFromTypeSignature(wrapper, typeVars, this); // skip synthetic argument
				while (wrapper.signature[wrapper.start] != ')') {
					types.add(environment.getTypeFromTypeSignature(wrapper, typeVars, this));
				}
				wrapper.start++; // skip ')'
				parameters = new TypeBinding[types.size()];
				types.toArray(parameters);
			}
		}

		if (!method.isConstructor())
			returnType = environment.getTypeFromTypeSignature(wrapper, typeVars, this);

		if (!wrapper.atEnd() && wrapper.signature[wrapper.start] == '^') {
			// attempt to find each superinterface if it exists in the cache (otherwise - resolve it when requested)
			java.util.ArrayList types = new java.util.ArrayList(2);
			do {
				wrapper.start++; // skip '^'
				types.add(environment.getTypeFromTypeSignature(wrapper, typeVars, this));
			} while (!wrapper.atEnd() && wrapper.signature[wrapper.start] == '^');
			exceptions = new ReferenceBinding[types.size()];
			types.toArray(exceptions);
		} else { // get the exceptions the old way
			char[][] exceptionTypes = method.getExceptionTypeNames();
			if (exceptionTypes != null) {
				int size = exceptionTypes.length;
				if (size > 0) {
					exceptions = new ReferenceBinding[size];
					for (int i = 0; i < size; i++)
						exceptions[i] = environment.getTypeFromConstantPoolName(exceptionTypes[i], 0, -1, false);
				}
			}
		}
	}

	MethodBinding result = method.isConstructor()
		? new MethodBinding(methodModifiers, parameters, exceptions, this)
		: new MethodBinding(methodModifiers, method.getSelector(), returnType, parameters, exceptions, this);
	result.typeVariables = typeVars;
	return result;
}
/**
 * Create method bindings for binary type, filtering out <clinit> and synthetics
 */
private void createMethods(IBinaryMethod[] iMethods, boolean checkGenericSignatures) {
	int total = 0, initialTotal = 0, iClinit = -1;
	int[] toSkip = null;
	if (iMethods != null) {
		total = initialTotal = iMethods.length;
		for (int i = total; --i >= 0;) {
			IBinaryMethod method = iMethods[i];
			if ((method.getModifiers() & AccSynthetic) != 0) {
				// discard synthetics methods
				if (toSkip == null) toSkip = new int[iMethods.length];
				toSkip[i] = -1;
				total--;
			} else if (iClinit == -1) {
				char[] methodName = method.getSelector();
				if (methodName.length == 8 && methodName[0] == '<') {
					// discard <clinit>
					iClinit = i;
					total--;
				}
			}
		}
	}
	if (total == 0) {
		this.methods = NoMethods;
		return;
	}

	this.methods = new MethodBinding[total];
	if (total == initialTotal) {
		for (int i = 0; i < initialTotal; i++)
			this.methods[i] = createMethod(iMethods[i], checkGenericSignatures);
	} else {
		for (int i = 0, index = 0; i < initialTotal; i++)
			if (iClinit != i && (toSkip == null || toSkip[i] != -1))
				this.methods[index++] = createMethod(iMethods[i], checkGenericSignatures);
	}
	modifiers |= AccUnresolved; // until methods() is sent
}
private TypeVariableBinding createTypeVariable(SignatureWrapper wrapper, int rank) {
	// ParameterSignature = Identifier ':' TypeSignature
	//   or Identifier ':' TypeSignature(optional) InterfaceBound(s)
	// InterfaceBound = ':' TypeSignature
	int colon = CharOperation.indexOf(':', wrapper.signature, wrapper.start);
	char[] variableName = CharOperation.subarray(wrapper.signature, wrapper.start, colon);
	TypeVariableBinding variable = new TypeVariableBinding(variableName, this, rank);
	return variable;
}

/* Answer the receiver's enclosing type... null if the receiver is a top level type.
*
* NOTE: enclosingType of a binary type is resolved when needed
*/

public ReferenceBinding enclosingType() {
	if ((this.tagBits & HasUnresolvedEnclosingType) == 0)
		return this.enclosingType;

	this.enclosingType = resolveUnresolvedType(this.enclosingType, this.environment, false); // no raw conversion for now
	this.tagBits ^= HasUnresolvedEnclosingType;

	// finish resolving the type
	this.enclosingType = resolveType(this.enclosingType, this.environment, false);
	return this.enclosingType;
}
// NOTE: the type of each field of a binary type is resolved when needed

public FieldBinding[] fields() {
	for (int i = fields.length; --i >= 0;)
		resolveTypeFor(fields[i]);
	return fields;
}
// NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed

public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
	int argCount = argumentTypes.length;
	nextMethod : for (int m = methods.length; --m >= 0;) {
		MethodBinding method = methods[m];
		if (method.selector == ConstructorDeclaration.ConstantPoolName && method.parameters.length == argCount) {
			resolveTypesFor(method);
			TypeBinding[] toMatch = method.parameters;
			for (int p = 0; p < argCount; p++)
				if (toMatch[p] != argumentTypes[p])
					continue nextMethod;
			return method;
		}
	}
	return null;
}
// NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed
// searches up the hierarchy as long as no potential (but not exact) match was found.

public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
	if (refScope != null)
		refScope.recordTypeReference(this);

	int argCount = argumentTypes.length;
	int selectorLength = selector.length;
	boolean foundNothing = true;
	nextMethod : for (int m = methods.length; --m >= 0;) {
		MethodBinding method = methods[m];
		if (method.selector.length == selectorLength && CharOperation.equals(method.selector, selector)) {
			foundNothing = false; // inner type lookups must know that a method with this name exists
			if (method.parameters.length == argCount) {
				resolveTypesFor(method);
				TypeBinding[] toMatch = method.parameters;
				for (int p = 0; p < argCount; p++)
					if (toMatch[p] != argumentTypes[p])
						continue nextMethod;
				return method;
			}
		}
	}

	if (foundNothing) {
		if (isInterface()) {
			 if (superInterfaces.length == 1)
				return superInterfaces[0].getExactMethod(selector, argumentTypes, refScope);
		} else if (superclass != null) {
			return superclass.getExactMethod(selector, argumentTypes, refScope);
		}
	}
	return null;
}
// NOTE: the type of a field of a binary type is resolved when needed

public FieldBinding getField(char[] fieldName, boolean needResolve) {
	int fieldLength = fieldName.length;
	for (int f = fields.length; --f >= 0;) {
		char[] name = fields[f].name;
		if (name.length == fieldLength && CharOperation.equals(name, fieldName))
			return needResolve ? resolveTypeFor(fields[f]) : fields[f];
	}
	return null;
}
/**
 *  Rewrite of default getMemberType to avoid resolving eagerly all member types when one is requested
 */
public ReferenceBinding getMemberType(char[] typeName) {
	for (int i = this.memberTypes.length; --i >= 0;) {
	    ReferenceBinding memberType = this.memberTypes[i];
	    if (memberType instanceof UnresolvedReferenceBinding) {
			char[] name = memberType.sourceName; // source name is qualified with enclosing type name
			int prefixLength = this.compoundName[this.compoundName.length - 1].length + 1; // enclosing$
			if (name.length == (prefixLength + typeName.length)) // enclosing $ typeName
				if (CharOperation.fragmentEquals(typeName, name, prefixLength, true)) // only check trailing portion
					return this.memberTypes[i] = resolveType(memberType, this.environment, false); // no raw conversion for now
	    } else if (CharOperation.equals(typeName, memberType.sourceName)) {
	        return memberType;
	    }
	}
	return null;
}
// NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed

public MethodBinding[] getMethods(char[] selector) {
	int count = 0;
	int lastIndex = -1;
	int selectorLength = selector.length;
	for (int m = 0, length = methods.length; m < length; m++) {
		MethodBinding method = methods[m];
		if (method.selector.length == selectorLength && CharOperation.equals(method.selector, selector)) {
			resolveTypesFor(method);
			count++;
			lastIndex = m;
		}
	}
	if (count == 1)
		return new MethodBinding[] {methods[lastIndex]};
	if (count > 0) {
		MethodBinding[] result = new MethodBinding[count];
		count = 0;
		for (int m = 0; m <= lastIndex; m++) {
			MethodBinding method = methods[m];
			if (method.selector.length == selectorLength && CharOperation.equals(method.selector, selector))
				result[count++] = method;
		}
		return result;
	}
	return NoMethods;
}
public boolean hasMemberTypes() {
    return this.memberTypes.length > 0;
}
// NOTE: member types of binary types are resolved when needed

public TypeVariableBinding getTypeVariable(char[] variableName) {
	TypeVariableBinding variable = super.getTypeVariable(variableName);
	resolveTypesFor(variable);
	return variable;
}
private void initializeTypeVariable(TypeVariableBinding variable, TypeVariableBinding[] existingVariables, SignatureWrapper wrapper) {
	// ParameterSignature = Identifier ':' TypeSignature
	//   or Identifier ':' TypeSignature(optional) InterfaceBound(s)
	// InterfaceBound = ':' TypeSignature
	int colon = CharOperation.indexOf(':', wrapper.signature, wrapper.start);
	wrapper.start = colon + 1; // skip name + ':'
	ReferenceBinding type, firstBound = null;
	if (wrapper.signature[wrapper.start] == ':') {
		type = environment.getType(JAVA_LANG_OBJECT);
	} else {
		type = (ReferenceBinding) environment.getTypeFromTypeSignature(wrapper, existingVariables, this);
		firstBound = type;
	}

	// variable is visible to its bounds
	variable.modifiers |= AccUnresolved;
	variable.superclass = type;

	ReferenceBinding[] bounds = null;
	if (wrapper.signature[wrapper.start] == ':') {
		java.util.ArrayList types = new java.util.ArrayList(2);
		do {
			wrapper.start++; // skip ':'
			types.add(environment.getTypeFromTypeSignature(wrapper, existingVariables, this));
		} while (wrapper.signature[wrapper.start] == ':');
		bounds = new ReferenceBinding[types.size()];
		types.toArray(bounds);
	}

	variable.superInterfaces = bounds == null ? NoSuperInterfaces : bounds;
	if (firstBound == null) {
		firstBound = variable.superInterfaces.length == 0 ? null : variable.superInterfaces[0];
		variable.modifiers |= AccInterface;
	}
	variable.firstBound = firstBound;
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
    if (this.typeVariables == NoTypeVariables) return false;
    if (otherType.isRawType())
        return otherType.erasure() == this;
	return false;
}
public boolean isGenericType() {
    return this.typeVariables != NoTypeVariables;
}
// NOTE: member types of binary types are resolved when needed

public ReferenceBinding[] memberTypes() {
 	if ((this.tagBits & HasUnresolvedMemberTypes) == 0)
		return this.memberTypes;

	for (int i = this.memberTypes.length; --i >= 0;)
		this.memberTypes[i] = resolveUnresolvedType(this.memberTypes[i], this.environment, false); // no raw conversion for now
	this.tagBits ^= HasUnresolvedMemberTypes;

	for (int i = this.memberTypes.length; --i >= 0;)
		this.memberTypes[i] = resolveType(this.memberTypes[i], this.environment, false); // no raw conversion for now
	return this.memberTypes;
}
// NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed

public MethodBinding[] methods() {
	if ((modifiers & AccUnresolved) == 0)
		return methods;

	for (int i = methods.length; --i >= 0;)
		resolveTypesFor(methods[i]);
	modifiers ^= AccUnresolved;
	return methods;
}
private FieldBinding resolveTypeFor(FieldBinding field) {
	if ((field.modifiers & AccUnresolved) == 0)
		return field;

	field.type = resolveType(field.type, this.environment, null, 0);
	field.modifiers ^= AccUnresolved;
	return field;
}
MethodBinding resolveTypesFor(MethodBinding method) {
	if ((method.modifiers & AccUnresolved) == 0)
		return method;

	if (!method.isConstructor())
		method.returnType = resolveType(method.returnType, this.environment, null, 0);
	for (int i = method.parameters.length; --i >= 0;)
		method.parameters[i] = resolveType(method.parameters[i], this.environment, null, 0);
	for (int i = method.thrownExceptions.length; --i >= 0;)
		method.thrownExceptions[i] = resolveType(method.thrownExceptions[i], this.environment, true);
	for (int i = method.typeVariables.length; --i >= 0;)
		resolveTypesFor(method.typeVariables[i]);
	method.modifiers ^= AccUnresolved;
	return method;
}
private TypeVariableBinding resolveTypesFor(TypeVariableBinding variable) {
	if ((variable.modifiers & AccUnresolved) == 0)
		return variable;

	if (variable.superclass != null)
		variable.superclass = resolveUnresolvedType(variable.superclass, this.environment, true);
	if (variable.firstBound != null)
		variable.firstBound = resolveUnresolvedType(variable.firstBound, this.environment, true);
	ReferenceBinding[] interfaces = variable.superInterfaces;
	for (int i = interfaces.length; --i >= 0;)
		interfaces[i] = resolveUnresolvedType(interfaces[i], this.environment, true);
	variable.modifiers ^= AccUnresolved;

	// finish resolving the types
	if (variable.superclass != null)
		variable.superclass = resolveType(variable.superclass, this.environment, true);
	if (variable.firstBound != null)
		variable.firstBound = resolveType(variable.firstBound, this.environment, true);
	for (int i = interfaces.length; --i >= 0;)
		interfaces[i] = resolveType(interfaces[i], this.environment, true);
	return variable;
}
/* Answer the receiver's superclass... null if the receiver is Object or an interface.
*
* NOTE: superclass of a binary type is resolved when needed
*/

public ReferenceBinding superclass() {
	if ((this.tagBits & HasUnresolvedSuperclass) == 0)
		return this.superclass;

	this.superclass = resolveUnresolvedType(this.superclass, this.environment, true);
	this.tagBits ^= HasUnresolvedSuperclass;

	// finish resolving the type
	this.superclass = resolveType(this.superclass, this.environment, true);
	return this.superclass;
}
// NOTE: superInterfaces of binary types are resolved when needed

public ReferenceBinding[] superInterfaces() {
	if ((this.tagBits & HasUnresolvedSuperinterfaces) == 0)
		return this.superInterfaces;

	for (int i = this.superInterfaces.length; --i >= 0;)
		this.superInterfaces[i] = resolveUnresolvedType(this.superInterfaces[i], this.environment, true);
	this.tagBits ^= HasUnresolvedSuperinterfaces;

	for (int i = this.superInterfaces.length; --i >= 0;)
		this.superInterfaces[i] = resolveType(this.superInterfaces[i], this.environment, true);
	return this.superInterfaces;
}
public TypeVariableBinding[] typeVariables() {
 	if ((this.tagBits & HasUnresolvedTypeVariables) == 0)
		return this.typeVariables;

 	for (int i = this.typeVariables.length; --i >= 0;)
		resolveTypesFor(this.typeVariables[i]);
	this.tagBits ^= HasUnresolvedTypeVariables;
	return this.typeVariables;
}
public String toString() {
	String s = ""; //$NON-NLS-1$

	if (isDeprecated()) s += "deprecated "; //$NON-NLS-1$
	if (isPublic()) s += "public "; //$NON-NLS-1$
	if (isProtected()) s += "protected "; //$NON-NLS-1$
	if (isPrivate()) s += "private "; //$NON-NLS-1$
	if (isAbstract() && isClass()) s += "abstract "; //$NON-NLS-1$
	if (isStatic() && isNestedType()) s += "static "; //$NON-NLS-1$
	if (isFinal()) s += "final "; //$NON-NLS-1$

	s += isInterface() ? "interface " : "class "; //$NON-NLS-1$ //$NON-NLS-2$
	s += (compoundName != null) ? CharOperation.toString(compoundName) : "UNNAMED TYPE"; //$NON-NLS-1$

	s += "\n\textends "; //$NON-NLS-1$
	s += (superclass != null) ? superclass.debugName() : "NULL TYPE"; //$NON-NLS-1$

	if (superInterfaces != null) {
		if (superInterfaces != NoSuperInterfaces) {
			s += "\n\timplements : "; //$NON-NLS-1$
			for (int i = 0, length = superInterfaces.length; i < length; i++) {
				if (i  > 0)
					s += ", "; //$NON-NLS-1$
				s += (superInterfaces[i] != null) ? superInterfaces[i].debugName() : "NULL TYPE"; //$NON-NLS-1$
			}
		}
	} else {
		s += "NULL SUPERINTERFACES"; //$NON-NLS-1$
	}

	if (enclosingType != null) {
		s += "\n\tenclosing type : "; //$NON-NLS-1$
		s += enclosingType.debugName();
	}

	if (fields != null) {
		if (fields != NoFields) {
			s += "\n/*   fields   */"; //$NON-NLS-1$
			for (int i = 0, length = fields.length; i < length; i++)
				s += (fields[i] != null) ? "\n" + fields[i].toString() : "\nNULL FIELD"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		s += "NULL FIELDS"; //$NON-NLS-1$
	}

	if (methods != null) {
		if (methods != NoMethods) {
			s += "\n/*   methods   */"; //$NON-NLS-1$
			for (int i = 0, length = methods.length; i < length; i++)
				s += (methods[i] != null) ? "\n" + methods[i].toString() : "\nNULL METHOD"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		s += "NULL METHODS"; //$NON-NLS-1$
	}

	if (memberTypes != null) {
		if (memberTypes != NoMemberTypes) {
			s += "\n/*   members   */"; //$NON-NLS-1$
			for (int i = 0, length = memberTypes.length; i < length; i++)
				s += (memberTypes[i] != null) ? "\n" + memberTypes[i].toString() : "\nNULL TYPE"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		s += "NULL MEMBER TYPES"; //$NON-NLS-1$
	}

	s += "\n\n\n"; //$NON-NLS-1$
	return s;
}
MethodBinding[] unResolvedMethods() { // for the MethodVerifier so it doesn't resolve types
	return methods;
}
}
