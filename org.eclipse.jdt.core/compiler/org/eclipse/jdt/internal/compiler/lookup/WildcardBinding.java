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
import org.eclipse.jdt.internal.compiler.ast.Wildcard;

/*
 * A wildcard acts as an argument for parameterized types, allowing to
 * abstract parameterized types, e.g. List<String> is not compatible with List<Object>, 
 * but compatible with List<?>.
 */
public class WildcardBinding extends ReferenceBinding {

    TypeBinding bound; // when unbound denotes the corresponding type variable (so as to retrieve its bound lazily)
	char[] genericSignature;
	int kind;
	ReferenceBinding superclass;
	ReferenceBinding[] superInterfaces;
	LookupEnvironment environment;
	
	/**
	 * When unbound, the bound denotes the corresponding type variable (so as to retrieve its bound lazily)
	 */
	public WildcardBinding(TypeBinding bound, int kind, LookupEnvironment environment) {
	    this.bound = bound;
	    this.kind = kind;
		this.modifiers = AccPublic | AccGenericSignature; // treat wildcard as public
		this.tagBits |= HasWildcard;
		this.environment = environment;
	}

	/**
	 * Returns true if the argument type satisfies all bounds of the type parameter
	 */
	public boolean boundCheck(TypeBinding argumentType) {
	    switch (this.kind) {
	        case Wildcard.UNBOUND :
	            return true; // TODO (philippe) should it check against bound variable bounds ?
	       		//return ((TypeVariableBinding) this.bound).boundCheck(argumentType);
	        case Wildcard.EXTENDS :
	            return argumentType.isCompatibleWith(this.bound);
	        default: // SUPER
	            return this.bound.isCompatibleWith(argumentType);
	    }
    }
	
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#erasure()
     */
    public TypeBinding erasure() {
        return this.bound.erasure();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#signature()
     */
    public char[] genericTypeSignature() {
        if (this.genericSignature == null) {
            switch (this.kind) {
                case Wildcard.UNBOUND : 
                    this.genericSignature = WILDCARD_STAR;
                    break;
                case Wildcard.EXTENDS :
                    this.genericSignature = CharOperation.concat(WILDCARD_PLUS, this.bound.genericTypeSignature());
					break;
				default: // SUPER
				    this.genericSignature = CharOperation.concat(WILDCARD_MINUS, this.bound.genericTypeSignature());
            }
        } 
        return this.genericSignature;
    }
    
	/**
	 * Returns true if a type is identical to another one,
	 * or for generic types, true if compared to its raw type.
	 */
	public boolean isEquivalentTo(TypeBinding otherType) {
	    if (this == otherType) return true;
        return otherType.erasure() == this.erasure();
	}

	/* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#isSuperclassOf(org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding)
     */
    public boolean isSuperclassOf(ReferenceBinding otherType) {
        if (this.kind == Wildcard.SUPER) {
            if (this.bound instanceof ReferenceBinding) {
                return ((ReferenceBinding) this.bound).isSuperclassOf(otherType);
            } else { // array bound
                return otherType.id == T_Object;
            }
        }
        return false;
    }

    /**
	 * Returns true if the type is a wildcard
	 */
	public boolean isWildcard() {
	    return true;
	}

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.Binding#readableName()
     */
    public char[] readableName() {
        switch (this.kind) {
            case Wildcard.UNBOUND : 
                return WILDCARD_NAME;
            case Wildcard.EXTENDS :
                return CharOperation.concat(WILDCARD_NAME, WILDCARD_EXTENDS, this.bound.readableName());
			default: // SUPER
			    return CharOperation.concat(WILDCARD_NAME, WILDCARD_SUPER, this.bound.readableName());
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.Binding#shortReadableName()
     */
    public char[] shortReadableName() {
        switch (this.kind) {
            case Wildcard.UNBOUND : 
                return WILDCARD_NAME;
            case Wildcard.EXTENDS :
                return CharOperation.concat(WILDCARD_NAME, WILDCARD_EXTENDS, this.bound.shortReadableName());
			default: // SUPER
			    return CharOperation.concat(WILDCARD_NAME, WILDCARD_SUPER, this.bound.shortReadableName());
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#signature()
     */
    public char[] signature() {
        // TODO (philippe) per construction, should never be called 
        if (this.signature == null) {
            switch (this.kind) {
                case Wildcard.UNBOUND : 
                    this.signature = WILDCARD_STAR;
                    break;
                case Wildcard.EXTENDS :
                    this.signature = CharOperation.concat(WILDCARD_PLUS, this.bound.signature());
					break;
				default: // SUPER
				    this.signature = CharOperation.concat(WILDCARD_MINUS, this.bound.signature());
            }
        } 
        return this.signature;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#sourceName()
     */
    public char[] sourceName() {
        switch (this.kind) {
            case Wildcard.UNBOUND : 
                return WILDCARD_NAME;
            case Wildcard.EXTENDS :
                return CharOperation.concat(WILDCARD_NAME, WILDCARD_EXTENDS, this.bound.sourceName());
			default: // SUPER
			    return CharOperation.concat(WILDCARD_NAME, WILDCARD_SUPER, this.bound.sourceName());
        }        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding#superclass()
     */
    public ReferenceBinding superclass() {
		if (this.superclass == null) {
		    TypeBinding superType = this.bound;
		    if (this.kind == Wildcard.UNBOUND) {
		   		TypeVariableBinding boundVariable = (TypeVariableBinding) superType;
		   		superType = boundVariable.firstBound;
		   		if (superType == null) environment.getType(JAVA_LANG_OBJECT);
		    }
		    if (superType.isClass()) {
		        this.superclass = (ReferenceBinding) superType;
		    } else {
		        this.superclass = environment.getType(JAVA_LANG_OBJECT);
		    }
		}
		return this.superclass;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#superInterfaces()
     */
    public ReferenceBinding[] superInterfaces() {
        if (this.superInterfaces == null) {
		    TypeBinding superType = this.bound;
		    if (this.kind == Wildcard.UNBOUND) {
		   		TypeVariableBinding boundVariable = (TypeVariableBinding) superType;
		   		superType = boundVariable.firstBound;
		   		if (superType == null) environment.getType(JAVA_LANG_OBJECT);
		    }
            if (superType.isInterface()) {
                this.superInterfaces = new ReferenceBinding[] { (ReferenceBinding) superType };
            } else {
                this.superInterfaces = NoSuperInterfaces;
            }
        }
        return this.superInterfaces;
    }

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
        switch (this.kind) {
            case Wildcard.UNBOUND : 
                return new String(WILDCARD_NAME);
            case Wildcard.EXTENDS :
                return new String(CharOperation.concat(WILDCARD_NAME, WILDCARD_EXTENDS, this.bound.debugName().toCharArray()));
			default: // SUPER
			    return new String(CharOperation.concat(WILDCARD_NAME, WILDCARD_SUPER, this.bound.debugName().toCharArray()));
        }        
	}		
}
