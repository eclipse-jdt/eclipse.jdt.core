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
	final int IsArrayType = ASTNode.Bit1;
	final int IsBaseType = ASTNode.Bit2;
	final int IsNestedType = ASTNode.Bit3;
	final int IsMemberType = ASTNode.Bit4;
	final int MemberTypeMask = IsNestedType | IsMemberType;
	final int IsLocalType = ASTNode.Bit5;
	final int LocalTypeMask = IsNestedType | IsLocalType;
	final int IsAnonymousType = ASTNode.Bit6;
	final int AnonymousTypeMask = LocalTypeMask | IsAnonymousType;
	final int IsBinaryBinding = ASTNode.Bit7;
	
	// for the type hierarchy check used by ClassScope
	final int BeginHierarchyCheck = ASTNode.Bit9;
	final int EndHierarchyCheck = ASTNode.Bit10;

	// test bit to see if default abstract methods were computed
	final int KnowsDefaultAbstractMethods = ASTNode.Bit11;

	// Reusable bit currently used by Scopes
	final int InterfaceVisited = ASTNode.Bit12;

	// test bits to see if parts of binary types are faulted
	final int AreFieldsComplete = ASTNode.Bit13;
	final int AreMethodsComplete = ASTNode.Bit14;

	// test bit to avoid asking a type for a member type (includes inherited member types)
	final int HasNoMemberTypes = ASTNode.Bit15;

	// test bit to identify if the type's hierarchy is inconsistent
	final int HierarchyHasProblems = ASTNode.Bit16;

	// set for parameterized type NOT of the form X<?,?>
	final int IsBoundParameterizedType = ASTNode.Bit24; 

	// used by BinaryTypeBinding
	final int HasUnresolvedTypeVariables = ASTNode.Bit25;
	final int HasUnresolvedSuperclass = ASTNode.Bit26;
	final int HasUnresolvedSuperinterfaces = ASTNode.Bit27;
	final int HasUnresolvedEnclosingType = ASTNode.Bit28;
	final int HasUnresolvedMemberTypes = ASTNode.Bit29;

	final int HasTypeVariable = ASTNode.Bit30; // set either for type variables (direct) or parameterized types indirectly referencing type variables
	final int HasWildcard = ASTNode.Bit31; // set either for wildcards (direct) or parameterized types indirectly referencing wildcards
}
