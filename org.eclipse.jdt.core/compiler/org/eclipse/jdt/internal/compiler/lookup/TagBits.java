/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;

public interface TagBits {
    
	// Tag bits in the tagBits int of every TypeBinding
	long IsArrayType = ASTNode.Bit1;
	long IsBaseType = ASTNode.Bit2;
	long IsNestedType = ASTNode.Bit3;
	long IsMemberType = ASTNode.Bit4;
	long MemberTypeMask = IsNestedType | IsMemberType;
	long IsLocalType = ASTNode.Bit5;
	long LocalTypeMask = IsNestedType | IsLocalType;
	long IsAnonymousType = ASTNode.Bit6;
	long AnonymousTypeMask = LocalTypeMask | IsAnonymousType;
	long IsBinaryBinding = ASTNode.Bit7;
	
	// for the type cycle hierarchy check used by ClassScope
	long BeginHierarchyCheck = ASTNode.Bit9;  // type
	long EndHierarchyCheck = ASTNode.Bit10; // type
	long HasParameterAnnotations = ASTNode.Bit11; // method
	
	// test bit to see if default abstract methods were computed
	long KnowsDefaultAbstractMethods = ASTNode.Bit11;

	// Reusable bit currently used by Scopes
	long InterfaceVisited = ASTNode.Bit12;

	// test bits to see if parts of binary types are faulted
	long AreFieldsComplete = ASTNode.Bit13;
	long AreMethodsComplete = ASTNode.Bit14;

	// test bit to avoid asking a type for a member type (includes inherited member types)
	long HasNoMemberTypes = ASTNode.Bit15;

	// test bit to identify if the type's hierarchy is inconsistent
	long HierarchyHasProblems = ASTNode.Bit16;

	// set for parameterized type with successfull bound check
	long PassedBoundCheck = ASTNode.Bit23;
	
	// set for parameterized type NOT of the form X<?,?>
	long IsBoundParameterizedType = ASTNode.Bit24; 
	
	// used by BinaryTypeBinding
	long HasUnresolvedTypeVariables = ASTNode.Bit25;
	long HasUnresolvedSuperclass = ASTNode.Bit26;
	long HasUnresolvedSuperinterfaces = ASTNode.Bit27;
	long HasUnresolvedEnclosingType = ASTNode.Bit28;
	long HasUnresolvedMemberTypes = ASTNode.Bit29;

	long HasTypeVariable = ASTNode.Bit30; // set either for type variables (direct) or parameterized types indirectly referencing type variables
	long HasDirectWildcard = ASTNode.Bit31; // set for parameterized types directly referencing wildcards
	
	// for the annotation cycle hierarchy check used by ClassScope
	long BeginAnnotationCheck = ASTNode.Bit32L;
	long EndAnnotationCheck = ASTNode.Bit33L;
	
	// standard annotations
	// 9-bits for targets
	long AnnotationResolved = ASTNode.Bit34L;
	long AnnotationTarget = ASTNode.Bit35L; // @Target({}) only sets this bit
	long AnnotationForType = ASTNode.Bit36L;
	long AnnotationForField = ASTNode.Bit37L;
	long AnnotationForMethod = ASTNode.Bit38L;
	long AnnotationForParameter = ASTNode.Bit39L;
	long AnnotationForConstructor = ASTNode.Bit40L;
	long AnnotationForLocalVariable = ASTNode.Bit41L;
	long AnnotationForAnnotationType = ASTNode.Bit42L;
	long AnnotationForPackage = ASTNode.Bit43L;
	long AnnotationTargetMASK = AnnotationTarget
				| AnnotationForType | AnnotationForField
				| AnnotationForMethod | AnnotationForParameter
				| AnnotationForConstructor | AnnotationForLocalVariable
				| AnnotationForAnnotationType | AnnotationForPackage;
	// 2-bits for retention (should check (tagBits & RetentionMask) == RuntimeRetention
	long AnnotationSourceRetention = ASTNode.Bit44L;
	long AnnotationClassRetention = ASTNode.Bit45L;
	long AnnotationRuntimeRetention = AnnotationSourceRetention | AnnotationClassRetention;
	long AnnotationRetentionMASK = AnnotationSourceRetention | AnnotationClassRetention | AnnotationRuntimeRetention;
	// marker annotations
	long AnnotationDeprecated = ASTNode.Bit46L;
	long AnnotationDocumented = ASTNode.Bit47L;
	long AnnotationInherited = ASTNode.Bit48L;
	long AnnotationOverride = ASTNode.Bit49L;
	long AnnotationSuppressWarnings = ASTNode.Bit50L;
}
