/*******************************************************************************
 * Copyright (c) 2000-2004 IBM Corporation and others.
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

/**
 * A parameterized type encapsulates a type with type arguments,
 */
public class ParameterizedTypeBinding extends ReferenceBinding {

	public ReferenceBinding type; 
	public TypeBinding[] arguments;
	public LookupEnvironment environment; 
	public char[] genericTypeSignature;
	public ReferenceBinding superclass;
	public ReferenceBinding[] superInterfaces;	
	public FieldBinding[] fields;	
	public MethodBinding[] methods;
	
	public ParameterizedTypeBinding(ReferenceBinding type, TypeBinding[] arguments, LookupEnvironment environment){
		this.environment = environment;
		initialize(type, arguments);
		// TODO determine if need to copy other tagBits from type so as to provide right behavior to all predicates

		if (type instanceof UnresolvedReferenceBinding)
			((UnresolvedReferenceBinding) type).addWrapper(this);
		for (int i = 0, l = arguments == null ? 0 : arguments.length; i < l; i++)
			if (arguments[i] instanceof UnresolvedReferenceBinding)
				((UnresolvedReferenceBinding) arguments[i]).addWrapper(this);
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#computeId()
	 */
	public void computeId() {
		this.id = NoId;		
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#constantPoolName()
	 */
	public char[] constantPoolName() {
		return this.type.constantPoolName(); // erasure
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
		return this.type.enclosingType();
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
		if (this.fields == null) {
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
			}
		}
		return this.fields;
	}

	/**
	 * Ltype<param1 ... paremN>;
	 * LY<TT;>;
	 */
	public char[] genericTypeSignature() {
	    if (this.genericTypeSignature != null) return this.genericTypeSignature;
	    StringBuffer sig = new StringBuffer(10);
	    char[] typeSig = this.type.genericTypeSignature();
	    for (int i = 0; i < typeSig.length-1; i++) { // copy all but trailing semicolon
	    	sig.append(typeSig[i]);
	    }
	    sig.append('<');
	    for (int i = 0, length = this.arguments.length; i < length; i++) {
	        sig.append(this.arguments[i].genericTypeSignature());
	    }
	    sig.append(">;"); //$NON-NLS-1$
		return this.genericTypeSignature = sig.toString().toCharArray();
	}	

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getExactConstructor(TypeBinding[])
	 */
	public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
		int argCount = argumentTypes.length;

		if ((modifiers & AccUnresolved) == 0) { // have resolved all arg types & return type of the methods
			nextMethod : for (int m = methods.length; --m >= 0;) {
				MethodBinding method = methods[m];
				if (method.selector == ConstructorDeclaration.ConstantPoolName && method.parameters.length == argCount) {
					TypeBinding[] toMatch = method.parameters;
					for (int p = 0; p < argCount; p++)
						if (toMatch[p] != argumentTypes[p])
							continue nextMethod;
					return method;
				}
			}
		} else {
			MethodBinding[] constructors = getMethods(ConstructorDeclaration.ConstantPoolName); // takes care of duplicates & default abstract methods
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
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getExactMethod(char[], TypeBinding[])
	 */
	public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes) {
		int argCount = argumentTypes.length;
		int selectorLength = selector.length;
		boolean foundNothing = true;

		if ((modifiers & AccUnresolved) == 0) { // have resolved all arg types & return type of the methods
			nextMethod : for (int m = methods.length; --m >= 0;) {
				MethodBinding method = methods[m];
				if (method.selector.length == selectorLength && CharOperation.equals(method.selector, selector)) {
					foundNothing = false; // inner type lookups must know that a method with this name exists
					if (method.parameters.length == argCount) {
						TypeBinding[] toMatch = method.parameters;
						for (int p = 0; p < argCount; p++)
							if (toMatch[p] != argumentTypes[p])
								continue nextMethod;
						return method;
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
					return method;
				}
			}
		}

		if (foundNothing) {
			if (isInterface()) {
				 if (superInterfaces.length == 1)
					return superInterfaces[0].getExactMethod(selector, argumentTypes);
			} else if (superclass != null) {
				return superclass.getExactMethod(selector, argumentTypes);
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
		for (int f = fields.length; --f >= 0;) {
			FieldBinding field = fields[f];
			if (field.name.length == fieldLength && CharOperation.equals(field.name, fieldName))
				return field;
		}
		return null;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getMemberType(char[])
	 */
	public ReferenceBinding getMemberType(char[] typeName) {
		return this.type.getMemberType(typeName);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getMethods(char[])
	 */
	public MethodBinding[] getMethods(char[] selector) {
		java.util.ArrayList matchingMethods = null;
		if (this.methods != null) {
			int selectorLength = selector.length;
			for (int i = 0, length = this.methods.length; i < length; i++) {
				MethodBinding method = methods[i];
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
		if ((modifiers & AccUnresolved) == 0) return NoMethods; // have created all the methods and there are no matches

		MethodBinding[] parameterizedMethods = null;
		try {
		    MethodBinding[] originalMethods = this.type.getMethods(selector);
		    int length = originalMethods.length;
		    if (length == 0) return NoMethods; 

		    parameterizedMethods = new MethodBinding[length];
		    for (int i = 0; i < length; i++)
		    	// substitute methods, so as to get updated declaring class at least
	            parameterizedMethods[i] = new ParameterizedMethodBinding(this, originalMethods[i]);
		    if (this.methods == null) {
		    	this.methods = parameterizedMethods;
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

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#implementsMethod(MethodBinding)
	 */
	public boolean implementsMethod(MethodBinding method) {
		return this.type.implementsMethod(method); // erasure
	}

	void initialize(ReferenceBinding someType, TypeBinding[] someArguments) {
		this.type = someType;
		this.compoundName = someType.compoundName;
		this.fPackage = someType.fPackage;
		this.fileName = someType.fileName;
		// expect the fields & methods to be initialized correctly later
		this.superclass = null; // recomputed when needed
		this.superInterfaces = null;
		this.fields = null;
		this.methods = null;		
		this.modifiers = someType.modifiers | AccGenericSignature | AccUnresolved; // until methods() is sent
		if (someArguments != null) {
			this.arguments = someArguments;
			for (int i = 0, length = someArguments.length; i < length; i++)
			    this.tagBits |= someArguments[i].tagBits & (HasTypeVariable | HasWildcard);
		}	    
	}
	
	public boolean isEquivalentTo(TypeBinding otherType) {
		if (this == otherType) 
		    return true;
        if (otherType.isRawType())
            return erasure() == otherType.erasure();
        if ((otherType.tagBits & HasWildcard) != 0) {
            if (otherType.isParameterizedType()) {
                ParameterizedTypeBinding otherParamType = (ParameterizedTypeBinding) otherType;
                if (this.type != otherParamType.type) 
                    return false;
                for (int i = 0, length = this.arguments.length; i < length; i++) {
                    if (!this.arguments[i].isEquivalentTo(otherParamType.arguments[i]))
                            return false;
                }
                return true;
            } else { // wildcard
                return ((WildcardBinding) otherType).boundCheck(this);
//                return this.isEquivalentTo(((WildcardBinding) otherType).bound);
            }
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
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#memberTypes()
	 */
	public ReferenceBinding[] memberTypes() {
		return this.type.memberTypes();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#methods()
	 */
	public MethodBinding[] methods() {
		if ((modifiers & AccUnresolved) == 0)
			return this.methods;

		try {
		    MethodBinding[] originalMethods = this.type.methods();
		    int length = originalMethods.length;
		    MethodBinding[] parameterizedMethods = new MethodBinding[length];
		    for (int i = 0; i < length; i++)
		    	// substitute all methods, so as to get updated declaring class at least
	            parameterizedMethods[i] = new ParameterizedMethodBinding(this, originalMethods[i]);
		    this.methods = parameterizedMethods;
		} finally {
			// if the original methods cannot be retrieved (ex. AbortCompilation), then assume we do not have any methods
		    if (this.methods == null) 
		        this.methods = NoMethods;

			modifiers ^= AccUnresolved;
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
		if (this.type.isMemberType()) {
			nameBuffer.append(CharOperation.concat(this.type.enclosingType().readableName(), sourceName, '.'));
		} else {
			nameBuffer.append(CharOperation.concatWith(this.type.compoundName, '.'));
		}	    
		nameBuffer.append('<');
	    for (int i = 0, length = this.arguments.length; i < length; i++) {
	        if (i > 0) nameBuffer.append(',');
	        nameBuffer.append(this.arguments[i].readableName());
	    }
	    nameBuffer.append('>');
	    return nameBuffer.toString().toCharArray();
	}

	ReferenceBinding resolve() {
		ReferenceBinding resolvedType = (ReferenceBinding) BinaryTypeBinding.resolveType(this.type, this.environment, null, 0);
		int argLength = this.arguments.length;
		for (int i = 0; i < argLength; i++)
			BinaryTypeBinding.resolveType(this.arguments[i], this.environment, this, i);
		// arity check
		TypeVariableBinding[] refTypeVariables = resolvedType.typeVariables();
		if (refTypeVariables == NoTypeVariables) { // check generic
			this.environment.problemReporter.nonGenericTypeCannotBeParameterized(null, resolvedType, this.arguments);
			return type; // TODO (philippe) what do you expect this to be? is it not == resolvedType?
		} else if (argLength != refTypeVariables.length) { // check arity
			this.environment.problemReporter.incorrectArityForParameterizedType(null, resolvedType, this.arguments);
			return type;
		}			
		// check argument type compatibility
		for (int i = 0; i < argLength; i++) {
		    TypeBinding resolvedArgument = this.arguments[i];
			if (!refTypeVariables[i].boundCheck(resolvedArgument)) {
				this.environment.problemReporter.typeMismatchError(resolvedArgument, refTypeVariables[i], resolvedType, null);
		    }
		}
		return this;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#shortReadableName()
	 */
	public char[] shortReadableName() {
	    StringBuffer nameBuffer = new StringBuffer(10);
		if (this.type.isMemberType()) {
			nameBuffer.append(CharOperation.concat(this.type.enclosingType().shortReadableName(), sourceName, '.'));
		} else {
			nameBuffer.append(this.type.sourceName);
		}	    
		nameBuffer.append('<');
	    for (int i = 0, length = this.arguments.length; i < length; i++) {
	        if (i > 0) nameBuffer.append(',');
	        nameBuffer.append(this.arguments[i].shortReadableName());
	    }
	    nameBuffer.append('>');
	    return nameBuffer.toString().toCharArray();
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
	 * Returns a type, where original type was substituted using the receiver
	 * parameterized type.
	 */
	public TypeBinding substitute(TypeBinding originalType) {
	    
	    if ((originalType.tagBits & TagBits.HasTypeVariable) != 0) {
		    if (originalType.isTypeVariable()) {
		        TypeVariableBinding originalVariable = (TypeVariableBinding) originalType;
		        TypeVariableBinding[] typeVariables = this.type.typeVariables();
		        int length = typeVariables.length;
		        // check this variable can be substituted given parameterized type
		        if (originalVariable.rank < length && typeVariables[originalVariable.rank] == originalVariable) {
		            return this.arguments[originalVariable.rank];
		        }		        
		    } else if (originalType.isParameterizedType()) {
		        ParameterizedTypeBinding originalParameterizedType = (ParameterizedTypeBinding) originalType;
		        TypeBinding[] originalArguments = originalParameterizedType.arguments;
		        TypeBinding[] substitutedArguments = substitute(originalArguments);
		        if (substitutedArguments != originalArguments) {
		            return this.environment.createParameterizedType(originalParameterizedType.type, substitutedArguments);
		        }
		    }
	    }
	    return originalType;
	}
	
	/**
	 * Returns an array of types, where original types got substituted using the receiver
	 * parameterized type. Only allocate an array if anything is different.
	 */
	public TypeBinding[] substitute(TypeBinding[] originalTypes) {
	    TypeBinding[] substitutedTypes = originalTypes;
	    for (int i = 0, length = originalTypes.length; i < length; i++) {
	        TypeBinding originalType = originalTypes[i];
	        TypeBinding substitutedParameter = this.substitute(originalType);
	        if (substitutedParameter != originalType) {
	            if (substitutedTypes == originalTypes) {
	                System.arraycopy(originalTypes, 0, substitutedTypes = new TypeBinding[length], 0, i);
	            }
	            substitutedTypes[i] = substitutedParameter;
	        } else if (substitutedTypes != originalTypes) {
	            substitutedTypes[i] = originalType;
	        }
	    }
	    return substitutedTypes;
    }
	
	/**
	 * Returns an array of types, where original types got substituted using the receiver
	 * parameterized type. Only allocate an array if anything is different.
	 */
	public ReferenceBinding[] substitute(ReferenceBinding[] originalTypes) {
	    ReferenceBinding[] substitutedTypes = originalTypes;
	    for (int i = 0, length = originalTypes.length; i < length; i++) {
	        ReferenceBinding originalType = originalTypes[i];
	        ReferenceBinding substitutedParameter = (ReferenceBinding)this.substitute(originalType);
	        if (substitutedParameter != originalType) {
	            if (substitutedTypes == originalTypes) {
	                System.arraycopy(originalTypes, 0, substitutedTypes = new ReferenceBinding[length], 0, i);
	            }
	            substitutedTypes[i] = substitutedParameter;
	        } else if (substitutedTypes != originalTypes) {
	            substitutedTypes[i] = originalType;
	        }
	    }
	    return substitutedTypes;
    }

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#superclass()
	 */
	public ReferenceBinding superclass() {
	    if (this.superclass == null) {
	        // note: Object cannot be generic
		    this.superclass = (ReferenceBinding) substitute(this.type.superclass());
	    }
		return this.superclass;
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#superInterfaces()
	 */
	public ReferenceBinding[] superInterfaces() {
	    if (this.superInterfaces == null) {
	    	this.superInterfaces = substitute(this.type.superInterfaces());
	    }
		return this.superInterfaces;
	}

	public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType) {
		boolean update = false;
		if (this.type == unresolvedType) {
			this.type = resolvedType;
			update = true;
		}
		if (this.arguments != null) {
			for (int i = 0, l = this.arguments.length; i < l; i++) {
				if (this.arguments[i] == unresolvedType) {
					this.arguments[i] = resolvedType;
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
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#leafComponentType()
	 */
	public TypeBinding leafComponentType() {
		return this.type.leafComponentType();
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
}
