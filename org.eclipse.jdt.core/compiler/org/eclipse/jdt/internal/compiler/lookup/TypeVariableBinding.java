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

import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Binding for a type parameter, held by source or binary type..
 */
public class TypeVariableBinding extends ReferenceBinding {
	
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
	
	public TypeVariableBinding(char[] sourceName, int rank){

		this.sourceName = sourceName;
		this.rank = rank; 
	}

	public char[] constantPoolName() { /* java/lang/Object */ 
	    if (this.firstBound != null) {
			return this.firstBound.constantPoolName();
	    }
	    return this.superclass.constantPoolName(); // java/lang/Object
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
		    if (i > 0) sig.append(':');
		    sig.append(this.superInterfaces[i].genericTypeSignature());
		}
		sig.append(';');
		return sig.toString().toCharArray();
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
	 * Returns true if the type was declared as a type variable
	 */
	public boolean isTypeVariable() {
	    return true;
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
		buffer.append('<').append(this.sourceName).append('[').append(this.rank).append(']');
		if (this.superclass != null) {
		    buffer.append(" extends ").append(this.superclass.debugName()); //$NON-NLS-1$
		}
		if (this.superInterfaces != null && this.superInterfaces != NoSuperInterfaces) {
		   if (this.superclass == null) {
		        buffer.append(" extends "); //$NON-NLS-1$
	        }
		    for (int i = 0, length = this.superInterfaces.length; i < length; i++) {
		        if (i > 0 || this.superclass != null) {
		            buffer.append(", "); //$NON-NLS-1$
		        }
				buffer.append(this.superInterfaces[i].debugName());
			}
		}
		buffer.append('>');
		return buffer.toString();
	}	
}