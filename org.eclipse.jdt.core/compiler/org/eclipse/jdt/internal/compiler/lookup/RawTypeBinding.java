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

/**
 * Denote a raw type, i.e. a generic type referenced without any type arguments.
 * e.g. X<T extends Exception> can be used a raw type 'X', in which case it
 * 	will behave as X<Exception>
 */
public class RawTypeBinding extends ParameterizedTypeBinding {
    
    /**
     * Raw type arguments are erasure of respective parameter bounds. But we may not have resolved
     * these bounds yet if creating raw types while supertype hierarchies are being connected.
     * Therefore, use 'null' instead, and access these in a lazy way later on (when substituting).
     */
	public RawTypeBinding(ReferenceBinding type, ReferenceBinding enclosingType, LookupEnvironment environment){
		super(type, null, enclosingType, environment);
		if (enclosingType == null || (enclosingType.modifiers & AccGenericSignature) == 0)
			this.modifiers ^= AccGenericSignature; // only need signature if enclosing needs one
	}    
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding#createParameterizedMethod(org.eclipse.jdt.internal.compiler.lookup.MethodBinding)
	 */
	public ParameterizedMethodBinding createParameterizedMethod(MethodBinding originalMethod) {
		if (originalMethod.typeVariables == NoTypeVariables) {
			return super.createParameterizedMethod(originalMethod);
		}
		return new ParameterizedGenericMethodBinding(originalMethod, this, this.environment);
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#debugName()
	 */
	public String debugName() {
	    StringBuffer nameBuffer = new StringBuffer(10);
		nameBuffer.append(this.type.sourceName()).append("#RAW"); //$NON-NLS-1$
	    return nameBuffer.toString();		
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
			    sig.append('.').append(this.sourceName()).append(';');
				int sigLength = sig.length();
				this.genericTypeSignature = new char[sigLength];
				sig.getChars(0, sigLength, this.genericTypeSignature, 0);						    
			} else {
			     this.genericTypeSignature = this.type.signature(); // erasure
			}
	    }
	   return this.genericTypeSignature;
	}		
	
    public boolean isEquivalentTo(TypeBinding otherType) {
	    if (this == otherType) return true;
        if (otherType == null) return false;
		if (otherType.isWildcard()) // wildcard
			return ((WildcardBinding) otherType).boundCheck(this);
        return otherType.erasure() == this.erasure();
    }
	/**
	 * Raw type is not treated as a standard parameterized type
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#isParameterizedType()
	 */
	public boolean isParameterizedType() {
	    return false;
	}	
	public boolean isRawType() {
	    return true;
	}	
	
	protected void initializeArguments() {
		TypeVariableBinding[] typeVariables = this.type.typeVariables();
		int length = typeVariables.length;
		TypeBinding[] typeArguments = new TypeBinding[length];
		for (int i = 0; i < length; i++) {
		    typeArguments[i] = typeVariables[i].erasure();
		}
		this.arguments = typeArguments;
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#readableName()
	 */
	public char[] readableName() /*java.lang.Object,  p.X<T> */ {
	    char[] readableName;
		if (isMemberType()) {
			readableName = CharOperation.concat(enclosingType().readableName(), sourceName, '.');
		} else {
			readableName = CharOperation.concatWith(this.type.compoundName, '.');
		}
		return readableName;
	}

	/**
	 * Returns a type, where original type was substituted using the receiver
	 * raw type.
	 * On raw types, all parameterized type denoting same original type are converted
	 * to raw types. e.g. 
	 * class X <T> {
	 *   X<T> foo;
	 *   X<String> bar;
	 * } when used in raw fashion, then type of both foo and bar is raw type X.
	 */
	public TypeBinding substitute(TypeBinding originalType) {
	    
	    if (originalType.isTypeVariable()) {
	        TypeVariableBinding originalVariable = (TypeVariableBinding) originalType;
		    ParameterizedTypeBinding currentType = this;
	        while (true) {
		        TypeVariableBinding[] typeVariables = currentType.type.typeVariables();
		        int length = typeVariables.length;
		        // check this variable can be substituted given parameterized type
		        if (originalVariable.rank < length && typeVariables[originalVariable.rank] == originalVariable) {
				    // lazy init, since cannot do so during binding creation if during supertype connection
				    if (currentType.arguments == null)  currentType.initializeArguments();
				    if (currentType.arguments != null)
			           return currentType.arguments[originalVariable.rank];
		        }
			    // recurse on enclosing type, as it may hold more substitutions to perform
			    ReferenceBinding enclosing = currentType.enclosingType();
			    if (!(enclosing instanceof ParameterizedTypeBinding))
			        break;
			    currentType = (ParameterizedTypeBinding) enclosing;
	        }
	    } else if (originalType.isParameterizedType()) {
	        ParameterizedTypeBinding originalParameterizedType = (ParameterizedTypeBinding) originalType;
			return this.environment.createRawType(originalParameterizedType.type, originalParameterizedType.enclosingType());
	    } else  if (originalType.isGenericType()) {
            return this.environment.createRawType((ReferenceBinding)originalType, null);
	    } else if (originalType.isArrayType()) {
			TypeBinding originalLeafComponentType = originalType.leafComponentType();
			TypeBinding substitute = substitute(originalLeafComponentType); // substitute could itself be array type
			if (substitute != originalLeafComponentType) {
				return this.environment.createArrayType(substitute.leafComponentType(), substitute.dimensions() + originalType.dimensions());
			}
	    }
	    return originalType;
	}	

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#shortReadableName()
	 */
	public char[] shortReadableName() /*Object*/ {
	    char[] shortReadableName;
		if (isMemberType()) {
			shortReadableName = CharOperation.concat(enclosingType().shortReadableName(), sourceName, '.');
		} else {
			shortReadableName = this.type.sourceName;
		}
		return shortReadableName;
	}
}
