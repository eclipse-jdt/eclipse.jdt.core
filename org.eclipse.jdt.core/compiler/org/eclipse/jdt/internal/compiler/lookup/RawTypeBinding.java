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
	public RawTypeBinding(ReferenceBinding type, LookupEnvironment environment){
		super(type, null, environment);
		this.modifiers ^= AccGenericSignature;
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
	        this.genericTypeSignature = this.type.genericTypeSignature(); // erasure
	    }     
	   return this.genericTypeSignature;
	}		
	
    public boolean isEquivalentTo(TypeBinding otherType) {
	    if (this == otherType) return true;
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
	
	private void initializeArguments() {
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
			readableName = CharOperation.concat(this.type.enclosingType().readableName(), sourceName, '.');
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
	        TypeVariableBinding[] typeVariables = this.type.typeVariables();
	        int length = typeVariables.length;
	        // check this variable can be substituted given parameterized type
	        if (originalVariable.rank < length && typeVariables[originalVariable.rank] == originalVariable) {
			    // lazy init, since cannot do so during binding creation if during supertype connection
			    if (this.arguments == null)  initializeArguments();
	            return this.arguments[originalVariable.rank];
	        }		        
	    } else if (originalType.isParameterizedType()) {
//			// lazy init, since cannot do so during binding creation if during supertype connection
//			if (this.arguments == null)  initializeArguments();
//			parameterizedTypeBinding originalParameterizedType = (ParameterizedTypeBinding) originalType;
//		    TypeBinding[] originalArguments = originalParameterizedType.arguments;
//		    TypeBinding[] substitutedArguments = substitute(originalArguments);
//		    if (substitutedArguments != originalArguments) {
			return this.environment.createRawType(((ParameterizedTypeBinding)originalType).type);
//		    }
	    } else  if (originalType.isGenericType()) { 
            return this.environment.createRawType((ReferenceBinding)originalType);
	    }
	    return originalType;
	}	

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#shortReadableName()
	 */
	public char[] shortReadableName() /*Object*/ {
	    char[] shortReadableName;
		if (isMemberType()) {
			shortReadableName = CharOperation.concat(this.type.enclosingType().shortReadableName(), sourceName, '.');
		} else {
			shortReadableName = this.type.sourceName;
		}
		return shortReadableName;
	}
}
