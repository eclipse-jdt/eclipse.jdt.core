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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Node to represent Wildcard
 */
public class Wildcard extends SingleTypeReference {

    public static final int UNBOUND = 0;
    public static final int EXTENDS = 1;
    public static final int SUPER = 2;
    
	public TypeReference bound;
	public int kind;

	public Wildcard(int kind) {
		super(null, 0);
		this.kind = kind;
	}
	
	private TypeBinding internalResolveType(Scope scope, ReferenceBinding genericType, int rank) {
	    TypeBinding boundType = null;
	    if (this.bound != null) {
			boundType = scope.kind == Scope.CLASS_SCOPE
	       		? this.bound.resolveType((ClassScope)scope)
	       		: this.bound.resolveType((BlockScope)scope);
	       		        
			if (boundType == null) {
				return null;
			}	    
	    } else { // unbound wildcard
	        TypeVariableBinding[] typeVariables = genericType.typeVariables();
	        if (rank < typeVariables.length) {
	            boundType = typeVariables[rank]; // record the type variable in bound
	        } else {
	            // error case, use Object, error reported when constructing parameterized type binding
		        boundType = scope.getJavaLangObject();
	        }
	    }
	    WildcardBinding wildcard = scope.environment().createWildcard(boundType, this.kind);
	    return this.resolvedType = wildcard;
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output){
        switch (this.kind) {
            case Wildcard.UNBOUND : 
                output.append(WILDCARD_NAME);
            case Wildcard.EXTENDS :
                output.append(WILDCARD_NAME).append(WILDCARD_EXTENDS);
            	this.bound.printExpression(0, output);
            	break;
			default: // SUPER
                output.append(WILDCARD_NAME).append(WILDCARD_SUPER);
            	this.bound.printExpression(0, output);
            	break;
        }        	    
		return output;
	}	
	
	public TypeBinding resolveTypeArgument(BlockScope blockScope, ReferenceBinding genericType, int rank) {
	    return internalResolveType(blockScope, genericType, rank);
	}
	
	public TypeBinding resolveTypeArgument(ClassScope classScope, ReferenceBinding genericType, int rank) {
	    return internalResolveType(classScope, genericType, rank);
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		if (this.bound != null) {
			this.bound.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

	public void traverse(ASTVisitor visitor, ClassScope scope) {
		visitor.visit(this, scope);
		if (this.bound != null) {
			this.bound.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
