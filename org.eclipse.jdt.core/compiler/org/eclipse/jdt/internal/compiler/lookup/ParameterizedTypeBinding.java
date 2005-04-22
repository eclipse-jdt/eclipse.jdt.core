/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

/**
 * A parameterized type encapsulates a type with type arguments,
 */
public class ParameterizedTypeBinding extends ReferenceBinding implements Substitution {

	public ReferenceBinding type; 
	public TypeBinding[] arguments;
	public LookupEnvironment environment; 
	public char[] genericTypeSignature;
	public ReferenceBinding superclass;
	public ReferenceBinding[] superInterfaces;	
	public FieldBinding[] fields;	
	public ReferenceBinding[] memberTypes;
	public MethodBinding[] methods;
	private ReferenceBinding enclosingType;
	
	public ParameterizedTypeBinding(ReferenceBinding type, TypeBinding[] arguments,  ReferenceBinding enclosingType, LookupEnvironment environment){

		this.environment = environment;
		initialize(type, arguments);
		this.enclosingType = enclosingType; // never unresolved, never lazy per construction

		if (type instanceof UnresolvedReferenceBinding)
			((UnresolvedReferenceBinding) type).addWrapper(this);
		if (arguments != null) {
			for (int i = 0, l = arguments.length; i < l; i++)
				if (arguments[i] instanceof UnresolvedReferenceBinding)
					((UnresolvedReferenceBinding) arguments[i]).addWrapper(this);
		}
		this.tagBits |=  HasUnresolvedTypeVariables; // cleared in resolve()
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#canBeInstantiated()
	 */
	public boolean canBeInstantiated() {
		return ((this.tagBits & HasDirectWildcard) == 0) && super.canBeInstantiated(); // cannot instantiate param type with wildcard arguments
	}
	public int kind() {
		return PARAMETERIZED_TYPE;
	}	
	/**
	 * Perform capture conversion for a parameterized type with wildcard arguments
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#capture(Scope,int)
	 */
	public TypeBinding capture(Scope scope, int position) {
		TypeBinding[] originalArguments = arguments, capturedArguments = originalArguments;
		if ((this.tagBits & TagBits.HasDirectWildcard) != 0) {
			int length = originalArguments.length;
			capturedArguments = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				TypeBinding argument = originalArguments[i];
				if (argument.kind() == Binding.WILDCARD_TYPE) {
					capturedArguments[i] = 
						new CaptureBinding(
								(WildcardBinding) argument, 
								scope.enclosingSourceType().outermostEnclosingType(),
								position);
				} else {
					capturedArguments[i] = argument;
				}
			}
		}
		if (capturedArguments != originalArguments) {
			return this.environment.createParameterizedType(this.type, capturedArguments, enclosingType());
		}
		return this;
	}
	/**
	 * Collect the substitutes into a map for certain type variables inside the receiver type
	 * e.g.   Collection<T>.collectSubstitutes(Collection<List<X>>, Map), will populate Map with: T --> List<X>
	 */
	public void collectSubstitutes(Scope scope, TypeBinding otherType, Map substitutes, int constraint) {
		
		if ((this.tagBits & TagBits.HasTypeVariable) == 0) return;
		if (otherType == NullBinding) return;
	
		if (this.arguments == null) return;
		if (!(otherType instanceof ReferenceBinding)) return;
		ReferenceBinding equivalent, otherEquivalent;
		switch (constraint) {
			case CONSTRAINT_EQUAL :
			case CONSTRAINT_EXTENDS :
				equivalent = this;
		        otherEquivalent = ((ReferenceBinding)otherType).findSuperTypeErasingTo((ReferenceBinding)this.type.erasure());
		        if (otherEquivalent == null) return;
		        break;
			case CONSTRAINT_SUPER :
	        default:
		        equivalent = this.findSuperTypeErasingTo((ReferenceBinding)(otherType.erasure()));
		        if (equivalent == null) return;
		        otherEquivalent = (ReferenceBinding) otherType;
		        break;
		}
        TypeBinding[] elements;
        switch (equivalent.kind()) {
        	case Binding.GENERIC_TYPE :
        		elements = equivalent.typeVariables();
        		break;
        	case Binding.PARAMETERIZED_TYPE :
        		elements = ((ParameterizedTypeBinding)equivalent).arguments;
        		break;
        	case Binding.RAW_TYPE :
        		substitutes.clear(); // clear all variables to indicate raw generic method in the end
        		return;
        	default :
        		return;
        }
        TypeBinding[] otherElements;
        switch (otherEquivalent.kind()) {
        	case Binding.GENERIC_TYPE :
        		otherElements = otherEquivalent.typeVariables();
        		break;
        	case Binding.PARAMETERIZED_TYPE :
        		otherElements = ((ParameterizedTypeBinding)otherEquivalent).arguments;
        		break;
        	case Binding.RAW_TYPE :
        		substitutes.clear(); // clear all variables to indicate raw generic method in the end
        		return;
        	default :
        		return;
        }
        for (int i = 0, length = elements.length; i < length; i++) {
        	TypeBinding element = elements[i];
            element.collectSubstitutes(scope, otherElements[i], substitutes, element.isWildcard() ? constraint : CONSTRAINT_EQUAL);
        }
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#computeId()
	 */
	public void computeId() {
		this.id = NoId;		
	}
	
	public char[] computeUniqueKey(boolean withAccessFlags) {
	    StringBuffer sig = new StringBuffer(10);
		if (this.isMemberType() && enclosingType().isParameterizedType()) {
		    char[] typeSig = enclosingType().computeUniqueKey(false/*without access flags*/);
		    for (int i = 0; i < typeSig.length-1; i++) sig.append(typeSig[i]); // copy all but trailing semicolon
		    sig.append('.').append(sourceName());
		} else if(this.type.isLocalType()){
			LocalTypeBinding localTypeBinding = (LocalTypeBinding) this.type;
			ReferenceBinding enclosing = localTypeBinding.enclosingType();
			ReferenceBinding temp;
			while ((temp = enclosing.enclosingType()) != null)
				enclosing = temp;
			char[] typeSig = enclosing.signature();
		    for (int i = 0; i < typeSig.length-1; i++) sig.append(typeSig[i]); // copy all but trailing semicolon
			sig.append('$');
			sig.append(localTypeBinding.sourceStart);
		} else {
		    char[] typeSig = this.type.signature();
		    for (int i = 0; i < typeSig.length-1; i++) sig.append(typeSig[i]); // copy all but trailing semicolon
		}	   	    
		if (this.arguments != null) {
		    sig.append('<');
		    for (int i = 0, length = this.arguments.length; i < length; i++) {
		        sig.append(this.arguments[i].computeUniqueKey(false/*without access flags*/));
		    }
		    sig.append('>'); //$NON-NLS-1$
		}
		sig.append(';');
		if (withAccessFlags) {
			sig.append('^');
			sig.append(getAccessFlags());
		}
		int sigLength = sig.length();
		char[] uniqueKey = new char[sigLength];
		sig.getChars(0, sigLength, uniqueKey, 0);			
		return uniqueKey;
   	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#constantPoolName()
	 */
	public char[] constantPoolName() {
		return this.type.constantPoolName(); // erasure
	}

	public ParameterizedMethodBinding createParameterizedMethod(MethodBinding originalMethod) {
		return new ParameterizedMethodBinding(this, originalMethod);
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#debugName()
	 */
	public String debugName() {
	    StringBuffer nameBuffer = new StringBuffer(10);
		nameBuffer.append(this.type.sourceName());
		if (this.arguments != null) {
			nameBuffer.append('<');
		    for (int i = 0, length = this.arguments.length; i < length; i++) {
		        if (i > 0) nameBuffer.append(',');
		        nameBuffer.append(this.arguments[i].debugName());
		    }
		    nameBuffer.append('>');
		}
	    return nameBuffer.toString();		
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#enclosingType()
	 */
	public ReferenceBinding enclosingType() {
		if (this.isMemberType() && this.enclosingType == null) {
			ReferenceBinding originalEnclosing = this.type.enclosingType();
			this.enclosingType = originalEnclosing.isGenericType()
				? this.environment.createRawType(originalEnclosing, originalEnclosing.enclosingType()) // TODO (need to propagate in depth on enclosing type)
				: originalEnclosing;
		}
	    return this.enclosingType;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Substitution#environment()
	 */
	public LookupEnvironment environment() {
		return this.environment;
	}
	
	/**
     * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#erasure()
     */
    public TypeBinding erasure() {
        return this.type.erasure(); // erasure
    }
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#fieldCount()
	 */
	public int fieldCount() {
		return this.type.fieldCount(); // same as erasure (lazy)
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#fields()
	 */
	public FieldBinding[] fields() {
		if ((tagBits & AreFieldsComplete) != 0)
			return this.fields;

		try {
			FieldBinding[] originalFields = this.type.fields();
			int length = originalFields.length;
			FieldBinding[] parameterizedFields = new FieldBinding[length];
			for (int i = 0; i < length; i++)
				// substitute all fields, so as to get updated declaring class at least
				parameterizedFields[i] = new ParameterizedFieldBinding(this, originalFields[i]);
			this.fields = parameterizedFields;	    
		} finally {
			// if the original fields cannot be retrieved (ex. AbortCompilation), then assume we do not have any fields
			if (this.fields == null) 
				this.fields = NoFields;
			tagBits |= AreFieldsComplete;
		}
		return this.fields;
	}

	/**
	 * Ltype<param1 ... paramN>;
	 * LY<TT;>;
	 */
	public char[] genericTypeSignature() {
	    if (this.genericTypeSignature == null) {
		    StringBuffer sig = new StringBuffer(10);
			if (this.isMemberType() && this.enclosingType().isParameterizedType()) {
			    char[] typeSig = this.enclosingType().genericTypeSignature();
			    for (int i = 0; i < typeSig.length-1; i++) sig.append(typeSig[i]); // copy all but trailing semicolon
			    sig.append('.').append(this.sourceName());
			} else {
			    char[] typeSig = this.type.signature();
			    for (int i = 0; i < typeSig.length-1; i++) sig.append(typeSig[i]); // copy all but trailing semicolon
			}	   	    
			if (this.arguments != null) {
			    sig.append('<');
			    for (int i = 0, length = this.arguments.length; i < length; i++) {
			        sig.append(this.arguments[i].genericTypeSignature());
			    }
			    sig.append('>'); //$NON-NLS-1$
			}
			sig.append(';');
			int sigLength = sig.length();
			this.genericTypeSignature = new char[sigLength];
			sig.getChars(0, sigLength, this.genericTypeSignature, 0);			
	    }
		return this.genericTypeSignature;	    
	}	

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getAnnotationTagBits()
	 */
	public long getAnnotationTagBits() {
		return this.type.getAnnotationTagBits();
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getExactConstructor(TypeBinding[])
	 */
	public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
		int argCount = argumentTypes.length;

		if ((tagBits & AreMethodsComplete) != 0) { // have resolved all arg types & return type of the methods
			nextMethod : for (int m = methods.length; --m >= 0;) {
				MethodBinding method = methods[m];
				if (method.selector == TypeConstants.INIT && method.parameters.length == argCount) {
					TypeBinding[] toMatch = method.parameters;
					for (int p = 0; p < argCount; p++)
						if (toMatch[p] != argumentTypes[p])
							continue nextMethod;
					return method;
				}
			}
		} else {
			MethodBinding[] constructors = getMethods(TypeConstants.INIT); // takes care of duplicates & default abstract methods
			nextConstructor : for (int c = constructors.length; --c >= 0;) {
				MethodBinding constructor = constructors[c];
				TypeBinding[] toMatch = constructor.parameters;
				if (toMatch.length == argCount) {
					for (int p = 0; p < argCount; p++)
						if (toMatch[p] != argumentTypes[p])
							continue nextConstructor;
					return constructor;
				}
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getExactMethod(char[], TypeBinding[],CompilationUnitScope)
	 */
	public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
		// sender from refScope calls recordTypeReference(this)
		int argCount = argumentTypes.length;
		int selectorLength = selector.length;
		boolean foundNothing = true;
		MethodBinding match = null;

		if ((tagBits & AreMethodsComplete) != 0) { // have resolved all arg types & return type of the methods
			nextMethod : for (int m = methods.length; --m >= 0;) {
				MethodBinding method = methods[m];
				if (method.selector.length == selectorLength && CharOperation.equals(method.selector, selector)) {
					foundNothing = false; // inner type lookups must know that a method with this name exists
					if (method.parameters.length == argCount) {
						TypeBinding[] toMatch = method.parameters;
						for (int p = 0; p < argCount; p++)
							if (toMatch[p] != argumentTypes[p])
								continue nextMethod;
						if (match != null) return null; // collision case
						match = method;
					}
				}
			}
		} else {
			MethodBinding[] matchingMethods = getMethods(selector); // takes care of duplicates & default abstract methods
			foundNothing = matchingMethods == NoMethods;
			nextMethod : for (int m = matchingMethods.length; --m >= 0;) {
				MethodBinding method = matchingMethods[m];
				TypeBinding[] toMatch = method.parameters;
				if (toMatch.length == argCount) {
					for (int p = 0; p < argCount; p++)
						if (toMatch[p] != argumentTypes[p])
							continue nextMethod;
						if (match != null) return null; // collision case
						match = method;
				}
			}
		}
		if (match != null) {
			// cannot be picked up as an exact match if its a possible anonymous case
			if (match.hasSubstitutedParameters() && this.arguments != null && this.arguments.length > 1) return null;
			return match;
		}
	
		if (foundNothing && (this.arguments == null || this.arguments.length <= 1)) {
			if (isInterface()) {
				 if (superInterfaces().length == 1) {
					if (refScope != null)
						refScope.recordTypeReference(superInterfaces[0]);
					return superInterfaces[0].getExactMethod(selector, argumentTypes, refScope);
				 }
			} else if (superclass() != null) {
				if (refScope != null)
					refScope.recordTypeReference(superclass);
				return superclass.getExactMethod(selector, argumentTypes, refScope);
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getField(char[], boolean)
	 */
	public FieldBinding getField(char[] fieldName, boolean needResolve) {
		fields(); // ensure fields have been initialized... must create all at once unlike methods
		int fieldLength = fieldName.length;
		for (int i = fields.length; --i >= 0;) {
			FieldBinding field = fields[i];
			if (field.name.length == fieldLength && CharOperation.equals(field.name, fieldName))
				return field;
		}
		return null;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getMemberType(char[])
	 */
	public ReferenceBinding getMemberType(char[] typeName) {
		memberTypes(); // ensure memberTypes have been initialized... must create all at once unlike methods
		int typeLength = typeName.length;
		for (int i = this.memberTypes.length; --i >= 0;) {
			ReferenceBinding memberType = this.memberTypes[i];
			if (memberType.sourceName.length == typeLength && CharOperation.equals(memberType.sourceName, typeName))
				return memberType;
		}
		return null;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getMethods(char[])
	 */
	public MethodBinding[] getMethods(char[] selector) {
		java.util.ArrayList matchingMethods = null;
		if (this.methods != null) {
			int selectorLength = selector.length;
			for (int i = 0, length = this.methods.length; i < length; i++) {
				MethodBinding method = this.methods[i];
				if (method.selector.length == selectorLength && CharOperation.equals(method.selector, selector)) {
					if (matchingMethods == null)
						matchingMethods = new java.util.ArrayList(2);
					matchingMethods.add(method);
				}
			}
			if (matchingMethods != null) {
				MethodBinding[] result = new MethodBinding[matchingMethods.size()];
				matchingMethods.toArray(result);
				return result;
			}
		}
		if ((tagBits & AreMethodsComplete) != 0)
			return NoMethods; // have created all the methods and there are no matches

		MethodBinding[] parameterizedMethods = null;
		try {
		    MethodBinding[] originalMethods = this.type.getMethods(selector);
		    int length = originalMethods.length;
		    if (length == 0) return NoMethods; 

		    parameterizedMethods = new MethodBinding[length];
		    for (int i = 0; i < length; i++)
		    	// substitute methods, so as to get updated declaring class at least
	            parameterizedMethods[i] = createParameterizedMethod(originalMethods[i]);
		    if (this.methods == null) {
		    	MethodBinding[] temp = new MethodBinding[length];
		    	System.arraycopy(parameterizedMethods, 0, temp, 0, length);
		    	this.methods = temp; // must be a copy of parameterizedMethods since it will be returned below
		    } else {
		    	MethodBinding[] temp = new MethodBinding[length + this.methods.length];
		    	System.arraycopy(parameterizedMethods, 0, temp, 0, length);
		    	System.arraycopy(this.methods, 0, temp, length, this.methods.length);
		    	this.methods = temp;
			}
		    return parameterizedMethods;
		} finally {
			// if the original methods cannot be retrieved (ex. AbortCompilation), then assume we do not have any methods
		    if (parameterizedMethods == null) 
		        this.methods = parameterizedMethods = NoMethods;
		}
	}

	public boolean hasMemberTypes() {
	    return this.type.hasMemberTypes();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#implementsMethod(MethodBinding)
	 */
	public boolean implementsMethod(MethodBinding method) {
		return this.type.implementsMethod(method); // erasure
	}

	void initialize(ReferenceBinding someType, TypeBinding[] someArguments) {
		this.type = someType;
		this.sourceName = someType.sourceName;
		this.compoundName = someType.compoundName;
		this.fPackage = someType.fPackage;
		this.fileName = someType.fileName;
		// should not be set yet
		// this.superclass = null;
		// this.superInterfaces = null;
		// this.fields = null;
		// this.methods = null;		
		this.modifiers = someType.modifiers | AccGenericSignature;
		if (someArguments != null) {
			this.arguments = someArguments;
			for (int i = 0, length = someArguments.length; i < length; i++) {
				TypeBinding someArgument = someArguments[i];
				boolean isWildcardArgument = someArgument.isWildcard();
				if (isWildcardArgument) {
					this.tagBits |= HasDirectWildcard;
				}
				if (!isWildcardArgument || ((WildcardBinding) someArgument).boundKind != Wildcard.UNBOUND) {
					this.tagBits |= IsBoundParameterizedType;
				}
			    this.tagBits |= someArgument.tagBits & HasTypeVariable;
			}
		}	    
		this.tagBits |= someType.tagBits & (IsLocalType| IsMemberType | IsNestedType);
		this.tagBits &= ~(AreFieldsComplete|AreMethodsComplete);
	}

	protected void initializeArguments() {
	    // do nothing for true parameterized types (only for raw types)
	}
	
	public boolean isEquivalentTo(TypeBinding otherType) {
		if (this == otherType) 
		    return true;
	    if (otherType == null) 
	        return false;
	    switch(otherType.kind()) {
	
	    	case Binding.WILDCARD_TYPE :
	        	return ((WildcardBinding) otherType).boundCheck(this);
	    		
	    	case Binding.PARAMETERIZED_TYPE :
	            if ((otherType.tagBits & HasDirectWildcard) == 0 && (!this.isMemberType() || !otherType.isMemberType())) 
	            	return false; // should have been identical
	            ParameterizedTypeBinding otherParamType = (ParameterizedTypeBinding) otherType;
	            if (this.type != otherParamType.type) 
	                return false;
	            if (!isStatic()) { // static member types do not compare their enclosing
		            ReferenceBinding enclosing = enclosingType();
		            if (enclosing != null && !enclosing.isEquivalentTo(otherParamType.enclosingType()))
		                return false;
	            }
	            int length = this.arguments == null ? 0 : this.arguments.length;
	            TypeBinding[] otherArguments = otherParamType.arguments;
	            int otherLength = otherArguments == null ? 0 : otherArguments.length;
	            if (otherLength != length) 
	                return false;
	            for (int i = 0; i < length; i++) {
	            	if (!this.arguments[i].isTypeArgumentContainedBy(otherArguments[i]))
	            		return false;
	            }
	            return true;
	    	
	    	case Binding.RAW_TYPE :
	            return erasure() == otherType.erasure();
	    }
        return false;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#isParameterizedType()
	 */
	public boolean isParameterizedType() {
	    return true;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Substitution#isRawSubstitution()
	 */
	public boolean isRawSubstitution() {
		return isRawType();
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#memberTypes()
	 */
	public ReferenceBinding[] memberTypes() {
		if (this.memberTypes == null) {
			try {
				ReferenceBinding[] originalMemberTypes = this.type.memberTypes();
				int length = originalMemberTypes.length;
				ReferenceBinding[] parameterizedMemberTypes = new ReferenceBinding[length];
				for (int i = 0; i < length; i++)
					// substitute all member types, so as to get updated enclosing types
					parameterizedMemberTypes[i] = this.environment.createParameterizedType(originalMemberTypes[i], null, this);
				this.memberTypes = parameterizedMemberTypes;	    
			} finally {
				// if the original fields cannot be retrieved (ex. AbortCompilation), then assume we do not have any fields
				if (this.memberTypes == null) 
					this.memberTypes = NoMemberTypes;
			}
		}
		return this.memberTypes;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#methods()
	 */
	public MethodBinding[] methods() {
		if ((tagBits & AreMethodsComplete) != 0)
			return this.methods;

		try {
		    MethodBinding[] originalMethods = this.type.methods();
		    int length = originalMethods.length;
		    MethodBinding[] parameterizedMethods = new MethodBinding[length];
		    for (int i = 0; i < length; i++)
		    	// substitute all methods, so as to get updated declaring class at least
	            parameterizedMethods[i] = createParameterizedMethod(originalMethods[i]);
		    this.methods = parameterizedMethods;
		} finally {
			// if the original methods cannot be retrieved (ex. AbortCompilation), then assume we do not have any methods
		    if (this.methods == null) 
		        this.methods = NoMethods;

			tagBits |=  AreMethodsComplete;
		}		
		return this.methods;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#qualifiedSourceName()
	 */
	public char[] qualifiedSourceName() {
		return this.type.qualifiedSourceName();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#readableName()
	 */
	public char[] readableName() {
	    StringBuffer nameBuffer = new StringBuffer(10);
		if (this.isMemberType()) {
			nameBuffer.append(CharOperation.concat(this.enclosingType().readableName(), sourceName, '.'));
		} else {
			nameBuffer.append(CharOperation.concatWith(this.type.compoundName, '.'));
		}	    
		if (this.arguments != null) {
			nameBuffer.append('<');
		    for (int i = 0, length = this.arguments.length; i < length; i++) {
		        if (i > 0) nameBuffer.append(',');
		        nameBuffer.append(this.arguments[i].readableName());
		    }
		    nameBuffer.append('>');
		}
		int nameLength = nameBuffer.length();
		char[] readableName = new char[nameLength];
		nameBuffer.getChars(0, nameLength, readableName, 0);		
	    return readableName;
	}

	ReferenceBinding resolve() {
		if ((this.tagBits & HasUnresolvedTypeVariables) == 0)
			return this;

		this.tagBits &= ~HasUnresolvedTypeVariables; // can be recursive so only want to call once
		ReferenceBinding resolvedType = BinaryTypeBinding.resolveType(this.type, this.environment, false); // still part of parameterized type ref
		if (this.arguments != null) {
			int argLength = this.arguments.length;
			for (int i = 0; i < argLength; i++)
				BinaryTypeBinding.resolveType(this.arguments[i], this.environment, this, i);
			// arity check
			TypeVariableBinding[] refTypeVariables = resolvedType.typeVariables();
			if (refTypeVariables == NoTypeVariables) { // check generic
				this.environment.problemReporter.nonGenericTypeCannotBeParameterized(null, resolvedType, this.arguments);
				return this; // cannot reach here as AbortCompilation is thrown
			} else if (argLength != refTypeVariables.length) { // check arity
				this.environment.problemReporter.incorrectArityForParameterizedType(null, resolvedType, this.arguments);
				return this; // cannot reach here as AbortCompilation is thrown
			}			
			// check argument type compatibility
			for (int i = 0; i < argLength; i++) {
			    TypeBinding resolvedArgument = this.arguments[i];
				if (refTypeVariables[i].boundCheck(this, resolvedArgument) != TypeConstants.OK) {
					this.environment.problemReporter.typeMismatchError(resolvedArgument, refTypeVariables[i], resolvedType, null);
			    }
			}
		}
		return this;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#shortReadableName()
	 */
	public char[] shortReadableName() {
	    StringBuffer nameBuffer = new StringBuffer(10);
		if (this.isMemberType()) {
			nameBuffer.append(CharOperation.concat(this.enclosingType().shortReadableName(), sourceName, '.'));
		} else {
			nameBuffer.append(this.type.sourceName);
		}	    
		if (this.arguments != null) {
			nameBuffer.append('<');
		    for (int i = 0, length = this.arguments.length; i < length; i++) {
		        if (i > 0) nameBuffer.append(',');
		        nameBuffer.append(this.arguments[i].shortReadableName());
		    }
		    nameBuffer.append('>');
		}
		int nameLength = nameBuffer.length();
		char[] shortReadableName = new char[nameLength];
		nameBuffer.getChars(0, nameLength, shortReadableName, 0);	    
	    return shortReadableName;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#signature()
	 */
	public char[] signature() {
	    if (this.signature == null) {
	        this.signature = this.type.signature();  // erasure
	    }
		return this.signature; 
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#sourceName()
	 */
	public char[] sourceName() {
		return this.type.sourceName();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Substitution#substitute(org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding)
	 */
	public TypeBinding substitute(TypeVariableBinding originalVariable) {
		
		ParameterizedTypeBinding currentType = this;
		while (true) {
			TypeVariableBinding[] typeVariables = currentType.type.typeVariables();
			int length = typeVariables.length;
			// check this variable can be substituted given parameterized type
			if (originalVariable.rank < length && typeVariables[originalVariable.rank] == originalVariable) {
			    // lazy init, since cannot do so during binding creation if during supertype connection
			    if (currentType.arguments == null)
					currentType.initializeArguments(); // only for raw types
			    if (currentType.arguments != null)
					return currentType.arguments[originalVariable.rank];
			}
			// recurse on enclosing type, as it may hold more substitutions to perform
			if (currentType.isStatic()) break;
			ReferenceBinding enclosing = currentType.enclosingType();
			if (!(enclosing instanceof ParameterizedTypeBinding))
				break;
			currentType = (ParameterizedTypeBinding) enclosing;
		}
		return originalVariable;
	}	

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#superclass()
	 */
	public ReferenceBinding superclass() {
	    if (this.superclass == null) {
	        // note: Object cannot be generic
	        ReferenceBinding genericSuperclass = this.type.superclass();
	        if (genericSuperclass == null) return null; // e.g. interfaces
		    this.superclass = (ReferenceBinding) Scope.substitute(this, genericSuperclass);
	    }
		return this.superclass;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#superInterfaces()
	 */
	public ReferenceBinding[] superInterfaces() {
	    if (this.superInterfaces == null) {
	    	this.superInterfaces = Scope.substitute(this, this.type.superInterfaces());
	    }
		return this.superInterfaces;
	}

	public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment env) {
		boolean update = false;
		if (this.type == unresolvedType) {
			this.type = resolvedType; // cannot be raw since being parameterized below
			update = true;
		}
		if (this.arguments != null) {
			for (int i = 0, l = this.arguments.length; i < l; i++) {
				if (this.arguments[i] == unresolvedType) {
					this.arguments[i] = resolvedType.isGenericType() ? env.createRawType(resolvedType, resolvedType.enclosingType()) : resolvedType;
					update = true;
				}
			}
		}
		if (update)
			initialize(this.type, this.arguments);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#syntheticEnclosingInstanceTypes()
	 */
	public ReferenceBinding[] syntheticEnclosingInstanceTypes() {
		return this.type.syntheticEnclosingInstanceTypes();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#syntheticOuterLocalVariables()
	 */
	public SyntheticArgumentBinding[] syntheticOuterLocalVariables() {
		return this.type.syntheticOuterLocalVariables();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#qualifiedPackageName()
	 */
	public char[] qualifiedPackageName() {
		return this.type.qualifiedPackageName();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
	    StringBuffer buffer = new StringBuffer(30);
		if (isDeprecated()) buffer.append("deprecated "); //$NON-NLS-1$
		if (isPublic()) buffer.append("public "); //$NON-NLS-1$
		if (isProtected()) buffer.append("protected "); //$NON-NLS-1$
		if (isPrivate()) buffer.append("private "); //$NON-NLS-1$
		if (isAbstract() && isClass()) buffer.append("abstract "); //$NON-NLS-1$
		if (isStatic() && isNestedType()) buffer.append("static "); //$NON-NLS-1$
		if (isFinal()) buffer.append("final "); //$NON-NLS-1$
	
		buffer.append(isInterface() ? "interface " : "class "); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(this.debugName());
	
		buffer.append("\n\textends "); //$NON-NLS-1$
		buffer.append((superclass != null) ? superclass.debugName() : "NULL TYPE"); //$NON-NLS-1$
	
		if (superInterfaces != null) {
			if (superInterfaces != NoSuperInterfaces) {
				buffer.append("\n\timplements : "); //$NON-NLS-1$
				for (int i = 0, length = superInterfaces.length; i < length; i++) {
					if (i  > 0)
						buffer.append(", "); //$NON-NLS-1$
					buffer.append((superInterfaces[i] != null) ? superInterfaces[i].debugName() : "NULL TYPE"); //$NON-NLS-1$
				}
			}
		} else {
			buffer.append("NULL SUPERINTERFACES"); //$NON-NLS-1$
		}
	
		if (enclosingType() != null) {
			buffer.append("\n\tenclosing type : "); //$NON-NLS-1$
			buffer.append(enclosingType().debugName());
		}
	
		if (fields != null) {
			if (fields != NoFields) {
				buffer.append("\n/*   fields   */"); //$NON-NLS-1$
				for (int i = 0, length = fields.length; i < length; i++)
				    buffer.append('\n').append((fields[i] != null) ? fields[i].toString() : "NULL FIELD"); //$NON-NLS-1$ 
			}
		} else {
			buffer.append("NULL FIELDS"); //$NON-NLS-1$
		}
	
		if (methods != null) {
			if (methods != NoMethods) {
				buffer.append("\n/*   methods   */"); //$NON-NLS-1$
				for (int i = 0, length = methods.length; i < length; i++)
					buffer.append('\n').append((methods[i] != null) ? methods[i].toString() : "NULL METHOD"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			buffer.append("NULL METHODS"); //$NON-NLS-1$
		}
	
//		if (memberTypes != null) {
//			if (memberTypes != NoMemberTypes) {
//				buffer.append("\n/*   members   */"); //$NON-NLS-1$
//				for (int i = 0, length = memberTypes.length; i < length; i++)
//					buffer.append('\n').append((memberTypes[i] != null) ? memberTypes[i].toString() : "NULL TYPE"); //$NON-NLS-1$ //$NON-NLS-2$
//			}
//		} else {
//			buffer.append("NULL MEMBER TYPES"); //$NON-NLS-1$
//		}
	
		buffer.append("\n\n"); //$NON-NLS-1$
		return buffer.toString();
		
	}

	public TypeVariableBinding[] typeVariables() {
		if (this.arguments == null) {
			// retain original type variables if not substituted (member type of parameterized type)
			return this.type.typeVariables();
		} 
		return NoTypeVariables;
	}	
}
