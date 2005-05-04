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

import java.util.ArrayList;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IGenericType;
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
	    return environment.createRawType(type, type.enclosingType());
	return type;
}
public static TypeBinding resolveType(TypeBinding type, LookupEnvironment environment, ParameterizedTypeBinding parameterizedType, int rank) {
	switch (type.kind()) {
		
		case Binding.PARAMETERIZED_TYPE :
			return ((ParameterizedTypeBinding) type).resolve();
			
		case Binding.WILDCARD_TYPE :
			return ((WildcardBinding) type).resolve();
			
		case Binding.ARRAY_TYPE :
			resolveType(((ArrayBinding) type).leafComponentType, environment, parameterizedType, rank);
			break;
			
		case Binding.TYPE_PARAMETER :
			((TypeVariableBinding) type).resolve(environment);
			break;
						
		case Binding.GENERIC_TYPE :
			if (parameterizedType == null) // raw reference to generic ?
			    return environment.createRawType((ReferenceBinding) type, type.enclosingType());
			break;
			
		default:			
			if (type instanceof UnresolvedReferenceBinding)
				return ((UnresolvedReferenceBinding) type).resolve(environment, parameterizedType == null);
	}
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

	char[] typeSignature = environment.globalOptions.sourceLevel >= ClassFileConstants.JDK1_5 ? binaryType.getGenericSignature() : null;
	this.typeVariables = typeSignature != null && typeSignature.length > 0 && typeSignature[0] == '<'
		? null // is initialized in cachePartsFrom (called from LookupEnvironment.createBinaryTypeFrom())... must set to null so isGenericType() answers true
		: NoTypeVariables;

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
	if (binaryType.getKind() == IGenericType.INTERFACE_DECL)
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
	if ((tagBits & AreFieldsComplete) != 0)
		return fields;

	FieldBinding[] availableFields = new FieldBinding[fields.length];
	int count = 0;
	for (int i = 0; i < fields.length; i++) {
		try {
			availableFields[count] = resolveTypeFor(fields[i]);
			count++;
		} catch (AbortCompilation a){
			// silent abort
		}
	}
	if (count < availableFields.length)
		System.arraycopy(availableFields, 0, availableFields = new FieldBinding[count], 0, count);
	return availableFields;
}
public MethodBinding[] availableMethods() {
	if ((tagBits & AreMethodsComplete) != 0)
		return methods;

	MethodBinding[] availableMethods = new MethodBinding[methods.length];
	int count = 0;
	for (int i = 0; i < methods.length; i++) {
		try {
			availableMethods[count] = resolveTypesFor(methods[i]);
			count++;
		} catch (AbortCompilation a){
			// silent abort
		}
	}
	if (count < availableMethods.length)
		System.arraycopy(availableMethods, 0, availableMethods = new MethodBinding[count], 0, count);
	return availableMethods;
}
void cachePartsFrom(IBinaryType binaryType, boolean needFieldsAndMethods) {
	// default initialization for super-interfaces early, in case some aborting compilation error occurs,
	// and still want to use binaries passed that point (e.g. type hierarchy resolver, see bug 63748).
	this.typeVariables = NoTypeVariables;
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

	long sourceLevel = environment.globalOptions.sourceLevel;
	char[] typeSignature = null;
	if (sourceLevel >= ClassFileConstants.JDK1_5) {
		typeSignature = binaryType.getGenericSignature();
		this.tagBits |= binaryType.getTagBits();
	}
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
			this.typeVariables = createTypeVariables(wrapper, this);
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
		createFields(binaryType.getFields(), sourceLevel);
		createMethods(binaryType.getMethods(), sourceLevel);
	} else { // protect against incorrect use of the needFieldsAndMethods flag, see 48459
		this.fields = NoFields;
		this.methods = NoMethods;
	}
}
private void createFields(IBinaryField[] iFields, long sourceLevel) {
	this.fields = NoFields;
	if (iFields != null) {
		int size = iFields.length;
		if (size > 0) {
			this.fields = new FieldBinding[size];
			boolean use15specifics = sourceLevel >= ClassFileConstants.JDK1_5;
			boolean isViewedAsDeprecated = isViewedAsDeprecated();
			for (int i = 0; i < size; i++) {
				IBinaryField binaryField = iFields[i];
				char[] fieldSignature = use15specifics ? binaryField.getGenericSignature() : null;
				TypeBinding type = fieldSignature == null 
					? environment.getTypeFromSignature(binaryField.getTypeName(), 0, -1, false, this) 
					: environment.getTypeFromTypeSignature(new SignatureWrapper(fieldSignature), NoTypeVariables, this);
				FieldBinding field = 
					new FieldBinding(
						binaryField.getName(), 
						type, 
						binaryField.getModifiers() | AccUnresolved, 
						this, 
						binaryField.getConstant());
				field.id = i; // ordinal
				if (use15specifics)
					field.tagBits |= binaryField.getTagBits();
				if (isViewedAsDeprecated && !field.isDeprecated())
					field.modifiers |= AccDeprecatedImplicitly;
				if (fieldSignature != null)
					field.modifiers |= AccGenericSignature;
				this.fields[i] = field;
			}
		}
	}
}
private MethodBinding createMethod(IBinaryMethod method, long sourceLevel) {
	int methodModifiers = method.getModifiers() | AccUnresolved;
	if (sourceLevel < ClassFileConstants.JDK1_5)
		methodModifiers &= ~AccVarargs; // vararg methods are not recognized until 1.5
	ReferenceBinding[] exceptions = NoExceptions;
	TypeBinding[] parameters = NoParameters;
	TypeVariableBinding[] typeVars = NoTypeVariables;
	TypeBinding returnType = null;

	final boolean use15specifics = sourceLevel >= ClassFileConstants.JDK1_5;
	char[] methodSignature = use15specifics ? method.getGenericSignature() : null;
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
		methodModifiers |= AccGenericSignature;
		// MethodTypeSignature = ParameterPart(optional) '(' TypeSignatures ')' return_typeSignature ['^' TypeSignature (optional)]
		SignatureWrapper wrapper = new SignatureWrapper(methodSignature);
		if (wrapper.signature[wrapper.start] == '<') {
			// <A::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TA;>;)TA;
			// ParameterPart = '<' ParameterSignature(s) '>'
			wrapper.start++; // skip '<'
			typeVars = createTypeVariables(wrapper, this);
			wrapper.start++; // skip '>'
		}

		if (wrapper.signature[wrapper.start] == '(') {
			wrapper.start++; // skip '('
			if (wrapper.signature[wrapper.start] == ')') {
				wrapper.start++; // skip ')'
			} else {
				java.util.ArrayList types = new java.util.ArrayList(2);
				while (wrapper.signature[wrapper.start] != ')')
					types.add(environment.getTypeFromTypeSignature(wrapper, typeVars, this));
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
	if (use15specifics)
		result.tagBits |= method.getTagBits();
	result.typeVariables = typeVars;
	// fixup the declaring element of the type variable
	for (int i = 0, length = typeVars.length; i < length; i++)
		typeVars[i].declaringElement = result;
	return result;
}
/**
 * Create method bindings for binary type, filtering out <clinit> and synthetics
 */
private void createMethods(IBinaryMethod[] iMethods, long sourceLevel) {
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

	boolean isViewedAsDeprecated = isViewedAsDeprecated();
	this.methods = new MethodBinding[total];
	if (total == initialTotal) {
		for (int i = 0; i < initialTotal; i++) {
			MethodBinding method = createMethod(iMethods[i], sourceLevel);
			if (isViewedAsDeprecated && !method.isDeprecated())
				method.modifiers |= AccDeprecatedImplicitly;
			this.methods[i] = method;
		}
	} else {
		for (int i = 0, index = 0; i < initialTotal; i++) {
			if (iClinit != i && (toSkip == null || toSkip[i] != -1)) {
				MethodBinding method = createMethod(iMethods[i], sourceLevel);
				if (isViewedAsDeprecated && !method.isDeprecated())
					method.modifiers |= AccDeprecatedImplicitly;
				this.methods[index++] = method;
			}
		}
	}
}
private TypeVariableBinding[] createTypeVariables(SignatureWrapper wrapper, Binding declaringElement) {
	// detect all type variables first
	char[] typeSignature = wrapper.signature;
	int depth = 0, length = typeSignature.length;
	int rank = 0;
	ArrayList variables = new ArrayList(1);
	depth = 0;
	boolean pendingVariable = true;
	createVariables: {
		for (int i = 1; i < length; i++) {
			switch(typeSignature[i]) {
				case '<' : 
					depth++;
					break;
				case '>' : 
					if (--depth < 0)
						break createVariables;
					break;
				case ';' :
					if ((depth == 0) && (i +1 < length) && (typeSignature[i+1] != ':'))
						pendingVariable = true;
					break;
				default:
					if (pendingVariable) {
						pendingVariable = false;
						int colon = CharOperation.indexOf(':', typeSignature, i);
						char[] variableName = CharOperation.subarray(typeSignature, i, colon);
						variables.add(new TypeVariableBinding(variableName, declaringElement, rank++));
					}
			}
		}
	}
	// initialize type variable bounds - may refer to forward variables
	TypeVariableBinding[] result;
	variables.toArray(result = new TypeVariableBinding[rank]);
	for (int i = 0; i < rank; i++) {
		initializeTypeVariable(result[i], result, wrapper);
	}
	return result;
}
/* Answer the receiver's enclosing type... null if the receiver is a top level type.
*
* NOTE: enclosingType of a binary type is resolved when needed
*/
public ReferenceBinding enclosingType() {
	if ((this.tagBits & HasUnresolvedEnclosingType) == 0)
		return this.enclosingType;

	this.enclosingType = resolveUnresolvedType(this.enclosingType, this.environment, false); // no raw conversion for now
	this.tagBits &= ~HasUnresolvedEnclosingType;

	// finish resolving the type
	this.enclosingType = resolveType(this.enclosingType, this.environment, false);
	return this.enclosingType;
}
// NOTE: the type of each field of a binary type is resolved when needed
public FieldBinding[] fields() {
	if ((tagBits & AreFieldsComplete) != 0)
		return fields;

	for (int i = fields.length; --i >= 0;)
		resolveTypeFor(fields[i]);
	tagBits |= AreFieldsComplete;
	return fields;
}
/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#genericTypeSignature()
 */
public char[] genericTypeSignature() {
	return computeGenericTypeSignature(this.typeVariables);
}
// NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed
public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
	int argCount = argumentTypes.length;
	nextMethod : for (int m = methods.length; --m >= 0;) {
		MethodBinding method = methods[m];
		if (method.selector == TypeConstants.INIT && method.parameters.length == argCount) {
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
	// sender from refScope calls recordTypeReference(this)
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
			 if (superInterfaces().length == 1) { // ensure superinterfaces are resolved before checking
				if (refScope != null)
					refScope.recordTypeReference(superInterfaces[0]);
				return superInterfaces[0].getExactMethod(selector, argumentTypes, refScope);
			 }
		} else if (superclass() != null) { // ensure superclass is resolved before checking
			if (refScope != null)
				refScope.recordTypeReference(superclass);
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
	variable.resolve(this.environment);
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
	switch(otherType.kind()) {
		case Binding.WILDCARD_TYPE :
			return ((WildcardBinding) otherType).boundCheck(this);
		case Binding.RAW_TYPE :
			return otherType.erasure() == this;
	}
	return false;
}
public boolean isGenericType() {
    return this.typeVariables != NoTypeVariables;
}
public int kind() {
	if (this.typeVariables != NoTypeVariables)
		return Binding.GENERIC_TYPE;
	return Binding.TYPE;
}	
// NOTE: member types of binary types are resolved when needed
public ReferenceBinding[] memberTypes() {
 	if ((this.tagBits & HasUnresolvedMemberTypes) == 0)
		return this.memberTypes;

	for (int i = this.memberTypes.length; --i >= 0;)
		this.memberTypes[i] = resolveUnresolvedType(this.memberTypes[i], this.environment, false); // no raw conversion for now
	this.tagBits &= ~HasUnresolvedMemberTypes;

	for (int i = this.memberTypes.length; --i >= 0;)
		this.memberTypes[i] = resolveType(this.memberTypes[i], this.environment, false); // no raw conversion for now
	return this.memberTypes;
}
// NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed
public MethodBinding[] methods() {
	if ((tagBits & AreMethodsComplete) != 0)
		return methods;

	for (int i = methods.length; --i >= 0;)
		resolveTypesFor(methods[i]);
	tagBits |= AreMethodsComplete;
	return methods;
}
private FieldBinding resolveTypeFor(FieldBinding field) {
	if ((field.modifiers & AccUnresolved) == 0)
		return field;

	field.type = resolveType(field.type, this.environment, null, 0);
	field.modifiers &= ~AccUnresolved;
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
		method.typeVariables[i].resolve(this.environment);
	method.modifiers &= ~AccUnresolved;
	return method;
}
/* Answer the receiver's superclass... null if the receiver is Object or an interface.
*
* NOTE: superclass of a binary type is resolved when needed
*/
public ReferenceBinding superclass() {
	if ((this.tagBits & HasUnresolvedSuperclass) == 0)
		return this.superclass;

	this.superclass = resolveUnresolvedType(this.superclass, this.environment, true);
	this.tagBits &= ~HasUnresolvedSuperclass;

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
	this.tagBits &= ~HasUnresolvedSuperinterfaces;

	for (int i = this.superInterfaces.length; --i >= 0;)
		this.superInterfaces[i] = resolveType(this.superInterfaces[i], this.environment, true);
	return this.superInterfaces;
}
public TypeVariableBinding[] typeVariables() {
 	if ((this.tagBits & HasUnresolvedTypeVariables) == 0)
		return this.typeVariables;

 	for (int i = this.typeVariables.length; --i >= 0;)
		this.typeVariables[i].resolve(this.environment);
	this.tagBits &= ~HasUnresolvedTypeVariables;
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
