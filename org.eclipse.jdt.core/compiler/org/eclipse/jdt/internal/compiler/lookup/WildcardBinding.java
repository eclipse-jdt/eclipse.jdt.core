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

import java.util.Map;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;

/*
 * A wildcard acts as an argument for parameterized types, allowing to
 * abstract parameterized types, e.g. List<String> is not compatible with List<Object>, 
 * but compatible with List<?>.
 */
public class WildcardBinding extends ReferenceBinding {

	ReferenceBinding genericType;
	int rank;
    public TypeBinding bound; // when unbound denotes the corresponding type variable (so as to retrieve its bound lazily)
	char[] genericSignature;
	public int kind;
	ReferenceBinding superclass;
	ReferenceBinding[] superInterfaces;
	TypeVariableBinding typeVariable; // corresponding variable
	LookupEnvironment environment;
	
	/**
	 * When unbound, the bound denotes the corresponding type variable (so as to retrieve its bound lazily)
	 */
	public WildcardBinding(ReferenceBinding genericType, int rank, TypeBinding bound, int kind, LookupEnvironment environment) {
		this.genericType = genericType;
		this.rank = rank;
	    this.kind = kind;
		this.modifiers = AccPublic | AccGenericSignature; // treat wildcard as public
		this.environment = environment;
		initialize(genericType, bound);

		if (genericType instanceof UnresolvedReferenceBinding)
			((UnresolvedReferenceBinding) genericType).addWrapper(this);
		if (bound instanceof UnresolvedReferenceBinding)
			((UnresolvedReferenceBinding) bound).addWrapper(this);
	}

	public int bindingType() {
		return WILDCARD_TYPE;
	}	
		
	/**
	 * Returns true if the argument type satisfies all bounds of the type parameter
	 */
	public boolean boundCheck(TypeBinding argumentType) {
	    switch (this.kind) {
	        case Wildcard.UNBOUND :
	            return true;
	        case Wildcard.EXTENDS :
	            return argumentType.isCompatibleWith(this.bound);
	        default: // SUPER
	        	// allowed as long as one is compatible with other (either way)
	        	// ? super Exception   ok for:  IOException, since it would be ok for (Exception)ioException
	            return this.bound.isCompatibleWith(argumentType)
					|| argumentType.isCompatibleWith(this.bound);
	    }
    }
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#canBeInstantiated()
	 */
	public boolean canBeInstantiated() {
		// cannot be asked per construction
		return false;
	}
	/**
	 * Collect the substitutes into a map for certain type variables inside the receiver type
	 * e.g.   Collection<T>.findSubstitute(T, Collection<List<X>>):   T --> List<X>
	 */
	public void collectSubstitutes(TypeBinding otherType, Map substitutes) {
	    switch(this.kind) {
	        case Wildcard.UNBOUND :
	            return;
	        case Wildcard.EXTENDS :
	            this.bound.collectSubstitutes(otherType, substitutes);
	            return;
	        default: // SUPER
	            this.bound.collectSubstitutes(otherType, substitutes);
	            return;
	    }
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#debugName()
	 */
	public String debugName() {
	    return toString();		
	}	
	
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#erasure()
     */
    public TypeBinding erasure() {
    	if (this.kind == Wildcard.EXTENDS)
	        return this.bound.erasure();
    	return typeVariable().erasure();
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
    
	void initialize(ReferenceBinding someGenericType, TypeBinding someBound) {
		this.genericType = someGenericType;
		this.bound = someBound;
		if (someGenericType != null) {
			this.fPackage = someGenericType.getPackage();
		}
		if (someBound != null) {
		    if (someBound.isTypeVariable())
		        this.tagBits |= HasTypeVariable;
		}
	}

	/**
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
	public boolean isUnboundWildcard() {
	    return this.kind == Wildcard.UNBOUND;
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
    
	ReferenceBinding resolve() {
		BinaryTypeBinding.resolveType(this.genericType, this.environment, null, 0);
	    switch(this.kind) {
	        case Wildcard.EXTENDS :
	        case Wildcard.SUPER :
				BinaryTypeBinding.resolveType(this.bound, this.environment, null, 0);
				break;
			case Wildcard.UNBOUND :
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
			TypeBinding superType = this.typeVariable().firstBound;
			if (this.kind == Wildcard.EXTENDS) {
				if (this.bound.isClass()) {
					superType = this.bound;
				}
			}
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
        	if (this.typeVariable() != null) {
        		this.superInterfaces = this.typeVariable.superInterfaces();
        	} else {
        		this.superInterfaces = NoSuperInterfaces;
        	}
			if (this.kind == Wildcard.EXTENDS) {
				if (this.bound.isInterface()) {
					// augment super interfaces with the wildcard bound
					int length = this.superInterfaces.length;
					System.arraycopy(this.superInterfaces, 0, this.superInterfaces = new ReferenceBinding[length+1], 1, length);
					this.superInterfaces[0] = (ReferenceBinding) this.bound; // make bound first
				}
			}
        }
        return this.superInterfaces;
    }

	public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment env) {
		boolean affected = false;
		if (this.genericType == unresolvedType) {
			this.genericType = resolvedType; // no raw conversion
			affected = true;
		} else if (this.bound == unresolvedType) {
			this.bound = resolvedType.isGenericType() ? env.createRawType(resolvedType, resolvedType.enclosingType()) : resolvedType;
			affected = true;
		}
		if (affected) 
			initialize(this.genericType, this.bound);
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
	/**
	 * Returns associated type variable, or null in case of inconsistency
	 */
	public TypeVariableBinding typeVariable() {
		if (this.typeVariable == null) {
			TypeVariableBinding[] typeVariables = this.genericType.typeVariables();
			if (this.rank < typeVariables.length)
				this.typeVariable = typeVariables[this.rank];
		}
		return this.typeVariable;
	}
}
