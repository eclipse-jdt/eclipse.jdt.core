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

/*
 * A wildcard acts as an argument for parameterized types, allowing to
 * abstract parameterized types, e.g. List<String> is not compatible with List<Object>, 
 * but compatible with List<?>.
 */
public class WildcardBinding extends ReferenceBinding {

    TypeBinding bound;
	boolean isSuper;
	char[] genericSignature;
	
	public WildcardBinding(TypeBinding bound, boolean isSuper) {
	    this.bound = bound;
	    this.isSuper = isSuper;
		this.modifiers = AccPublic | AccGenericSignature; // treat wildcard as public
	}

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#signature()
     */
    public char[] genericTypeSignature() {
        if (this.genericSignature == null) {
			if (this.bound == null) {
			    this.genericSignature = WILDCARD_STAR;
			} else if (this.isSuper) {
			    this.genericSignature = CharOperation.concat(WILDCARD_MINUS, this.bound.genericTypeSignature());
			} else {
				this.genericSignature = CharOperation.concat(WILDCARD_PLUS, this.bound.genericTypeSignature());
			}
        } 
        return this.genericSignature;
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
        if (this.bound != null) {
            if (this.isSuper) {
                return CharOperation.concat(WILDCARD_NAME, WILDCARD_SUPER, this.bound.readableName());
            } else {
                return CharOperation.concat(WILDCARD_NAME, WILDCARD_EXTENDS, this.bound.readableName());
            }
        }
        return WILDCARD_NAME;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.Binding#shortReadableName()
     */
    public char[] shortReadableName() {
        if (this.bound != null) {
            if (this.isSuper) {
                return CharOperation.concat(WILDCARD_NAME, WILDCARD_SUPER, this.bound.shortReadableName());
            } else {
                return CharOperation.concat(WILDCARD_NAME, WILDCARD_EXTENDS, this.bound.shortReadableName());
            }
        }
        return WILDCARD_NAME;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#signature()
     */
    public char[] signature() {
        // TODO (philippe) per construction, should never be called 
        if (this.signature == null) {
			if (this.bound == null) {
			    this.signature = WILDCARD_STAR;
			} else if (this.isSuper) {
			    this.signature = CharOperation.concat(WILDCARD_MINUS, this.bound.signature());
			} else {
				this.signature = CharOperation.concat(WILDCARD_PLUS, this.bound.signature());
			}
        } 
        return this.signature;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#sourceName()
     */
    public char[] sourceName() {
        if (this.bound != null) {
            if (this.isSuper) {
                return CharOperation.concat(WILDCARD_NAME, WILDCARD_SUPER, this.bound.sourceName());
            } else {
                return CharOperation.concat(WILDCARD_NAME, WILDCARD_EXTENDS, this.bound.sourceName());
            }
        }
        return WILDCARD_NAME;
    }

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer(10);
		buffer.append('?');
		if (this.bound != null) {
		    if (this.isSuper) {
		        buffer.append(WILDCARD_SUPER);
		    } else {
		        buffer.append(WILDCARD_EXTENDS);
		    }
		    buffer.append(this.bound.debugName());
		}
		return buffer.toString();
	}		
}
