/*******************************************************************************
 * Copyright (c) 2000, 2004 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Map;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;

/**
 * Binding for a type parameter, held by source/binary type or method.
 */
public class TypeVariableBinding extends ReferenceBinding {

	public Binding declaringElement; // binding of declaring type or method 
	public int rank; // declaration rank, can be used to match variable in parameterized type

	/**
	 * Denote the first explicit (binding) bound amongst the supertypes (from declaration in source)
	 * If no superclass was specified, then it denotes the first superinterface, or null if none was specified.
	 */
	public ReferenceBinding firstBound; 

	// actual resolved variable supertypes (if no superclass bound, then associated to Object)
	public ReferenceBinding superclass;
	public ReferenceBinding[] superInterfaces; 
	public char[] genericTypeSignature;

	public TypeVariableBinding(char[] sourceName, Binding declaringElement, int rank) {
		this.sourceName = sourceName;
		this.declaringElement = declaringElement;
		this.rank = rank;
		this.modifiers = AccPublic | AccGenericSignature; // treat type var as public
		this.tagBits |= HasTypeVariable;
	}

	/**
	 * Returns true if the argument type satisfies all bounds of the type parameter
	 */
	public boolean boundCheck(Substitution substitution, TypeBinding argumentType) {
		if (argumentType == NullBinding) 
			return true;
		if (!(argumentType instanceof ReferenceBinding || argumentType.isArrayType()))
			return false;	
		if (this.superclass.id != T_Object && !argumentType.isCompatibleWith(substitution.substitute(this.superclass))) {
		    return false;
		}
	    for (int i = 0, length = this.superInterfaces.length; i < length; i++) {
	        if (!argumentType.isCompatibleWith(substitution.substitute(this.superInterfaces[i]))) {
				return false;
	        }
	    }
	    if (argumentType.isWildcard()) {
	        WildcardBinding wildcard = (WildcardBinding) argumentType;
	        if (wildcard.kind == Wildcard.SUPER) {
	            if (!boundCheck(substitution, wildcard.bound)) return false;
	        }
	    }
	    return true;
	}

	/**
	 * Collect the substitutes into a map for certain type variables inside the receiver type
	 * e.g.   Collection<T>.findSubstitute(T, Collection<List<X>>):   T --> List<X>
	 */
	public void collectSubstitutes(TypeBinding otherType, Map substitutes) {
		// cannot infer anything from a null type
		if (otherType == NullBinding) return;
		
	    TypeBinding[] variableSubstitutes = (TypeBinding[])substitutes.get(this);
	    if (variableSubstitutes != null) {
	        int length = variableSubstitutes.length;
	        for (int i = 0; i < length; i++) {
	            if (variableSubstitutes[i] == otherType) return; // already there
	            if (variableSubstitutes[i] == null) {
	                variableSubstitutes[i] = otherType;
	                return;
	            }
	        }
	        // no free spot found, need to grow
	        System.arraycopy(variableSubstitutes, 0, variableSubstitutes = new TypeBinding[2*length], 0, length);
	        variableSubstitutes[length] = otherType;
	        substitutes.put(this, variableSubstitutes);
	    }
	}
	
	public char[] constantPoolName() { /* java/lang/Object */ 
	    if (this.firstBound != null) {
			return this.firstBound.constantPoolName();
	    }
	    return this.superclass.constantPoolName(); // java/lang/Object
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#debugName()
	 */
	public String debugName() {
	    return new String(this.sourceName);		
	}		
	public TypeBinding erasure() {
	    if (this.firstBound != null) {
			return this.firstBound.erasure();
	    }
	    return this.superclass; // java/lang/Object
	}	

	/**
	 * T::Ljava/util/Map;:Ljava/io/Serializable;
	 * T:LY<TT;>
	 */
	public char[] genericSignature() {
	    StringBuffer sig = new StringBuffer(10);
	    sig.append(this.sourceName).append(':');
	   	int interfaceLength = this.superInterfaces.length;
	    if (interfaceLength == 0 || this.firstBound == this.superclass) {
	        sig.append(this.superclass.genericTypeSignature());
	    }
		for (int i = 0; i < interfaceLength; i++) {
		    sig.append(':').append(this.superInterfaces[i].genericTypeSignature());
		}
		int sigLength = sig.length();
		char[] genericSignature = new char[sigLength];
		sig.getChars(0, sigLength, genericSignature, 0);					
		return genericSignature;
	}
	/**
	 * T::Ljava/util/Map;:Ljava/io/Serializable;
	 * T:LY<TT;>
	 */
	public char[] genericTypeSignature() {
	    if (this.genericTypeSignature != null) return this.genericTypeSignature;
		return this.genericTypeSignature = CharOperation.concat('T', this.sourceName, ';');
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
	/**
	 * Returns true if the type variable is directly bound to a given type
	 */
	public boolean isErasureBoundTo(TypeBinding type) {
		if (this.superclass.erasure() == type) 
			return true;
		for (int i = 0, length = this.superInterfaces.length; i < length; i++) {
			if (this.superInterfaces[i].erasure() == type)
				return true;
		}
		return false;
	}
	/**
	 * Returns true if the type was declared as a type variable
	 */
	public boolean isTypeVariable() {
	    return true;
	}
	/**
     * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#readableName()
     */
    public char[] readableName() {
        return this.sourceName;
    }
	/**
     * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#shortReadableName()
     */
    public char[] shortReadableName() {
        return this.readableName();
    }
	public ReferenceBinding superclass() {
		return superclass;
	}
	public ReferenceBinding[] superInterfaces() {
		return superInterfaces;
	}	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer(10);
		buffer.append('<').append(this.sourceName);//.append('[').append(this.rank).append(']');
		if (this.superclass != null && this.firstBound == this.superclass) {
		    buffer.append(" extends ").append(this.superclass.debugName()); //$NON-NLS-1$
		}
		if (this.superInterfaces != null && this.superInterfaces != NoSuperInterfaces) {
		   if (this.firstBound != this.superclass) {
		        buffer.append(" extends "); //$NON-NLS-1$
	        }
		    for (int i = 0, length = this.superInterfaces.length; i < length; i++) {
		        if (i > 0 || this.firstBound == this.superclass) {
		            buffer.append(" & "); //$NON-NLS-1$
		        }
				buffer.append(this.superInterfaces[i].debugName());
			}
		}
		buffer.append('>');
		return buffer.toString();
	}	
}