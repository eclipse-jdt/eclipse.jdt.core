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
    
	public RawTypeBinding(ReferenceBinding type, LookupEnvironment environment){
		super(type, parameterErasures(type), environment);
		this.modifiers ^= AccGenericSignature;
	}    
	/**
	 * Ltype<param1 ... paremN>;
	 * LY<TT;>;
	 */
	public char[] genericTypeSignature() {
	    if (this.genericTypeSignature != null) return this.genericTypeSignature;
		return this.genericTypeSignature = this.type.genericTypeSignature();
	}		
	/**
     * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#isCompatibleWith(org.eclipse.jdt.internal.compiler.lookup.TypeBinding)
     */
    public boolean isCompatibleWith(TypeBinding right) {
      	if (this.type.isCompatibleWith(right)) return true;
      	if (right.isParameterizedType()) {
      		ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding) right;
      		return isCompatibleWith(parameterizedType.type);
      	}
      	return false;
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
	private static TypeBinding[] parameterErasures(ReferenceBinding type) {
		TypeVariableBinding[] typeVariables = type.typeVariables();
		int length = typeVariables.length;
		TypeBinding[] typeArguments = new TypeBinding[length];
		for (int i = 0; i < length; i++) {
		    typeArguments[i] = typeVariables[i].erasure();
		}
		return typeArguments;
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
	 */
	public TypeBinding substitute(TypeBinding originalType) {
	    
	    // substitute of a raw type is the raw type itself if denoting same type
	    if (((originalType.tagBits & TagBits.HasTypeVariable) != 0)
		        && (originalType.isParameterizedType() && ((ParameterizedTypeBinding)originalType).type == this.type)) {
            return this;
        }
		return super.substitute(originalType);
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
	/**
	 * The superclass of a raw type is raw if targeting generic
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#superclass()
	 */
	public ReferenceBinding superclass() {
	    if (this.superclass == null) {
		    ReferenceBinding superType = this.type.superclass();
		    if (superType.isGenericType()) {
		        this.superclass = this.environment.createRawType(superType);
		    } else {
			    this.superclass = superType;
		    }
	    }
	    return this.superclass;
	}	
	/**
	 * The superinterfaces of a raw type are raw if targeting generic
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#superInterfaces()
	 */
	public ReferenceBinding[] superInterfaces() {
	    if (this.superInterfaces == null) {
		    ReferenceBinding[] originalInterfaces = this.type.superInterfaces();
		    ReferenceBinding[] rawInterfaces = originalInterfaces;
		    for (int i = 0, length = originalInterfaces.length; i < length; i++) {
		        ReferenceBinding originalInterface = originalInterfaces[i];
		        if (originalInterface.isGenericType()) {
		            if (rawInterfaces == originalInterfaces) {
		                System.arraycopy(originalInterfaces, 0, rawInterfaces = new ReferenceBinding[length], 0, i);
		            }
		            rawInterfaces[i] = this.environment.createRawType(originalInterface);
		        } else if (rawInterfaces != originalInterfaces) {
		            rawInterfaces[i] = originalInterface;
		        }
		    }
		    this.superInterfaces = rawInterfaces;
	    }
	    return this.superInterfaces;
    }	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer(10);
		buffer.append(this.type);
		return buffer.toString();
	}	
}
