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
	    this.kind = kind;
		this.modifiers = AccPublic | AccGenericSignature; // treat wildcard as public
		this.tagBits |= HasWildcard;
		this.environment = environment;
		initialize(bound);

		if (bound instanceof UnresolvedReferenceBinding)
			((UnresolvedReferenceBinding) bound).addWrapper(this);
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
    
	void initialize(TypeBinding someBound) {
		this.bound = someBound;
		if (someBound != null) {
			this.fPackage = someBound.getPackage();
			// should not be set yet
			// this.superclass = null;
			// this.superInterfaces = null;
		}
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
    
	ReferenceBinding resolve(ParameterizedTypeBinding parameterizedType, int rank) {
	    switch(this.kind) {
	        case Wildcard.EXTENDS :
	        case Wildcard.SUPER :
				BinaryTypeBinding.resolveType(this.bound, this.environment, null, 0);
				break;
			case Wildcard.UNBOUND :
				if (this.bound == null) {
				    TypeVariableBinding[] typeVariables = parameterizedType.type.typeVariables();
				    if (rank < typeVariables.length) {
				        this.bound = typeVariables[rank];
						WildcardBinding newWildcard = environment.createWildcard(this.bound, rank); // create a new wildcard since shared null one
					    newWildcard.initialize(this.bound);				        
					    return newWildcard;
				    }
				}
	    }
		return this;
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
        if (this.signature == null) {
            this.signature = this.bound.signature();
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
			if (this.kind == Wildcard.UNBOUND)
				superType = ((TypeVariableBinding) superType).firstBound;
			this.superclass = superType != null && superType.isClass()
				? (ReferenceBinding) superType
				: environment.getType(JAVA_LANG_OBJECT);
		}
		return this.superclass;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#superInterfaces()
     */
    public ReferenceBinding[] superInterfaces() {
        if (this.superInterfaces == null) {
		    TypeBinding superType = this.bound;
			if (this.kind == Wildcard.UNBOUND)
				superType = ((TypeVariableBinding) superType).firstBound;
			this.superInterfaces = superType != null && superType.isInterface()
				? new ReferenceBinding[] { (ReferenceBinding) superType }
				: NoSuperInterfaces;
        }
        return this.superInterfaces;
    }

	public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType) {
		if (this.bound == unresolvedType) {
			this.bound = resolvedType;
			initialize(this.bound);
		}
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
