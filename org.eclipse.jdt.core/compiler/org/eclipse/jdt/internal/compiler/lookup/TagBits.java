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

import org.eclipse.jdt.internal.compiler.ast.ASTNode;

public interface TagBits {
    
	// Tag bits in the tagBits int of every TypeBinding
	final long IsArrayType = ASTNode.Bit1;
	final long IsBaseType = ASTNode.Bit2;
	final long IsNestedType = ASTNode.Bit3;
	final long IsMemberType = ASTNode.Bit4;
	final long MemberTypeMask = IsNestedType | IsMemberType;
	final long IsLocalType = ASTNode.Bit5;
	final long LocalTypeMask = IsNestedType | IsLocalType;
	final long IsAnonymousType = ASTNode.Bit6;
	final long AnonymousTypeMask = LocalTypeMask | IsAnonymousType;
	final long IsBinaryBinding = ASTNode.Bit7;
	
	// for the type cycle hierarchy check used by ClassScope
	final long BeginHierarchyCheck = ASTNode.Bit9;
	final long EndHierarchyCheck = ASTNode.Bit10;

	// test bit to see if default abstract methods were computed
	final long KnowsDefaultAbstractMethods = ASTNode.Bit11;

	// Reusable bit currently used by Scopes
	final long InterfaceVisited = ASTNode.Bit12;

	// test bits to see if parts of binary types are faulted
	final long AreFieldsComplete = ASTNode.Bit13;
	final long AreMethodsComplete = ASTNode.Bit14;

	// test bit to avoid asking a type for a member type (includes inherited member types)
	final long HasNoMemberTypes = ASTNode.Bit15;

	// test bit to identify if the type's hierarchy is inconsistent
	final long HierarchyHasProblems = ASTNode.Bit16;

	// set for parameterized type NOT of the form X<?,?>
	final long IsBoundParameterizedType = ASTNode.Bit24; 

	// used by BinaryTypeBinding
	final long HasUnresolvedTypeVariables = ASTNode.Bit25;
	final long HasUnresolvedSuperclass = ASTNode.Bit26;
	final long HasUnresolvedSuperinterfaces = ASTNode.Bit27;
	final long HasUnresolvedEnclosingType = ASTNode.Bit28;
	final long HasUnresolvedMemberTypes = ASTNode.Bit29;

	final long HasTypeVariable = ASTNode.Bit30; // set either for type variables (direct) or parameterized types indirectly referencing type variables
	final long HasDirectWildcard = ASTNode.Bit31; // set for parameterized types directly referencing wildcards
	
	// for the annotation cycle hierarchy check used by ClassScope
	final long BeginAnnotationCheck = ASTNode.Bit32L;
	final long EndAnnotationCheck = ASTNode.Bit33L;
	
}
