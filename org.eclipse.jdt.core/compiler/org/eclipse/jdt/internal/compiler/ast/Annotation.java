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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Annotation
 */
public abstract class Annotation extends Expression {
	
	public TypeReference type;
	public int declarationSourceEnd;
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append('@');
		this.type.printExpression(0, output);
		return output;
	}
	
	TypeBinding internalResolveType(TypeBinding annotationType, Scope scope) {
		
		this.constant = NotAConstant;
		if (annotationType == null)
			return null;
		this.resolvedType = annotationType;
		// ensure type refers to an annotation type
		if (!annotationType.isAnnotationType()) {
			scope.problemReporter().typeMismatchError(annotationType, scope.getJavaLangAnnotationAnnotation(), this.type);
			return null;
		}
		return this.resolvedType;
	}
	
	public static void checkAnnotationValue(TypeBinding requiredType, TypeBinding annotationType, char[] memberName, Expression memberValue, Scope scope) {
		if (requiredType == null) 
			return;
		if (memberValue == null) 
			return;
		
		// annotation methods can only return base types, String, Class, enum type, annotation types and arrays of these
		checkAnnotationMethodType: {
			TypeBinding leafType = requiredType.leafComponentType();
			switch (leafType.erasure().id) {
				case T_byte :
				case T_short :
				case T_char :
				case T_int :
				case T_long :
				case T_float :
				case T_double :
				case T_boolean :
				case T_JavaLangString :
					if (memberValue.constant == NotAConstant) {
						scope.problemReporter().annotationValueMustBeConstant(annotationType, memberName, memberValue);
					}
					break checkAnnotationMethodType;
				case T_JavaLangClass :
					if (!(memberValue instanceof ClassLiteralAccess)) {
						scope.problemReporter().annotationValueMustBeClassLiteral(annotationType, memberName, memberValue);
					}
					break checkAnnotationMethodType;
			}
			if (leafType.isEnum()) {
				break checkAnnotationMethodType;
			}
			if (leafType.isAnnotationType()) {
				break checkAnnotationMethodType;
			}
		}
	}
	
	
	void checkMemberValues(MemberValuePair[] valuePairs, Scope scope) {
		
		ReferenceBinding annotationType = (ReferenceBinding) this.resolvedType;
		MethodBinding[] methods = annotationType.methods();
		TypeBinding expectedValueType = null;
		// clone valuePairs to keep track of unused ones
		MemberValuePair[] usedValuePairs;
		int pairsLength = valuePairs.length;
		System.arraycopy(valuePairs, 0, usedValuePairs = new MemberValuePair[pairsLength], 0, pairsLength);
		
		nextMember: for (int i = 0, requiredLength = methods.length; i < requiredLength; i++) {
			MethodBinding method = methods[i];
			char[] selector = method.selector;
			boolean foundValue = false;
			nextPair: for (int j = 0; j < pairsLength; j++) {
				MemberValuePair valuePair = usedValuePairs[j];
				if (valuePair == null) continue nextPair;
				char[] memberName = valuePair.name;
				if (CharOperation.equals(memberName, selector)) {
					usedValuePairs[j] = null; // consumed
					foundValue = true;
					boolean foundDuplicate = false;
					for (int k = j+1; k < pairsLength; k++) {
						if (CharOperation.equals(usedValuePairs[k].name, selector)) {
							foundDuplicate = true;
							scope.problemReporter().duplicateAnnotationValue(annotationType, usedValuePairs[k]);
							usedValuePairs[k] = null;
						}
					}
					if (foundDuplicate) {
						scope.problemReporter().duplicateAnnotationValue(annotationType, valuePair);
						continue nextMember;
					}
					Expression memberValue = valuePair.value;
					expectedValueType = method.returnType;
					memberValue.setExpectedType(expectedValueType); // needed in case of generic method invocation
					TypeBinding valueType = scope instanceof ClassScope
						? memberValue.resolveType((ClassScope)scope)
						: memberValue.resolveType((BlockScope)scope);
					if (expectedValueType == null || valueType == null)
						continue nextPair;

					checkAnnotationValue(expectedValueType, method.declaringClass, method.selector, memberValue, scope);
					
					// Compile-time conversion of base-types : implicit narrowing integer into byte/short/character
					// may require to widen the rhs expression at runtime
					if ((memberValue.isConstantValueOfTypeAssignableToType(valueType, expectedValueType)
							|| (expectedValueType.isBaseType() && BaseTypeBinding.isWidening(expectedValueType.id, valueType.id)))
							|| valueType.isCompatibleWith(expectedValueType)) {
						memberValue.computeConversion(scope, expectedValueType, valueType);
						continue nextMember;
					}
					scope.problemReporter().typeMismatchError(valueType, expectedValueType, memberValue);
					continue nextMember;
				}
			}
			if (!foundValue && (method.modifiers & AccAnnotationMethodWithDefault) == 0) {
				scope.problemReporter().missingValueForAnnotationMember(this, method.selector);
			}
		}
		// check unused pairs
		for (int i = 0; i < pairsLength; i++) {
			if (usedValuePairs[i] != null) {
				scope.problemReporter().undefinedAnnotationValue(annotationType, usedValuePairs[i]);
			}
		}
	}
				  
	public TypeBinding resolveType(BlockScope blockScope) {
		return internalResolveType(this.type.resolveType(blockScope), blockScope);
	}	
	public TypeBinding resolveType(ClassScope classScope) {
		return internalResolveType(this.type.resolveType(classScope), classScope);
	}
	
	public abstract void traverse(ASTVisitor visitor, BlockScope scope);
	public abstract void traverse(ASTVisitor visitor, ClassScope scope);
	public abstract void traverse(ASTVisitor visitor, CompilationUnitScope scope);
}
