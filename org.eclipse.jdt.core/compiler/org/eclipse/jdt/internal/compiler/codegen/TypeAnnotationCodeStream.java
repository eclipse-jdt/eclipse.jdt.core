/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.codegen;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class TypeAnnotationCodeStream extends StackMapFrameCodeStream {
	public List allTypeAnnotationContexts;

	public TypeAnnotationCodeStream(ClassFile givenClassFile) {
		super(givenClassFile);
		this.generateAttributes |= ClassFileConstants.ATTR_TYPE_ANNOTATION;
		this.allTypeAnnotationContexts = new ArrayList();
	}
	private void addAnnotationContext(TypeReference typeReference, int info, int targetType, Annotation[][] annotationsOnDimensions) {
//		if (this.allTypeAnnotationContexts == null) {
//			this.allTypeAnnotationContexts = new ArrayList();
//		}
		typeReference.getAllAnnotationContexts(targetType, info, this.allTypeAnnotationContexts, annotationsOnDimensions);
	}
	private void addAnnotationContext(TypeReference typeReference, int info, int targetType) {
//		if (this.allTypeAnnotationContexts == null) {
//			this.allTypeAnnotationContexts = new ArrayList();
//		}
		typeReference.getAllAnnotationContexts(targetType, info, this.allTypeAnnotationContexts);
	}
	private void addAnnotationContext(TypeReference typeReference, int info, int typeIndex, int targetType) {
//		if (this.allTypeAnnotationContexts == null) {
//			this.allTypeAnnotationContexts = new ArrayList();
//		}
		typeReference.getAllAnnotationContexts(targetType, info, typeIndex, this.allTypeAnnotationContexts);
	}
	public void instance_of(TypeReference typeReference, TypeBinding typeBinding) {
		if (typeReference != null && (typeReference.bits & ASTNode.HasTypeAnnotations) != 0) {
			addAnnotationContext(typeReference, this.position, AnnotationTargetTypeConstants.TYPE_INSTANCEOF);
		}
		super.instance_of(typeReference, typeBinding);
	}
	public void multianewarray(
			TypeReference typeReference,
			TypeBinding typeBinding,
			int dimensions,
			Annotation [][] annotationsOnDimensions) {
		if (typeReference != null && (typeReference.bits & ASTNode.HasTypeAnnotations) != 0) {
			addAnnotationContext(typeReference, this.position, AnnotationTargetTypeConstants.OBJECT_CREATION, annotationsOnDimensions);
		}
		super.multianewarray(typeReference, typeBinding, dimensions, annotationsOnDimensions);
	}
	public void new_(TypeReference typeReference, TypeBinding typeBinding) {
		if (typeReference != null && (typeReference.bits & ASTNode.HasTypeAnnotations) != 0) {
			addAnnotationContext(typeReference, this.position, AnnotationTargetTypeConstants.OBJECT_CREATION);
		}
		super.new_(typeReference, typeBinding);
	}
	public void newArray(TypeReference typeReference, ArrayBinding arrayBinding) {
		if (typeReference != null && (typeReference.bits & ASTNode.HasTypeAnnotations) != 0) {
			addAnnotationContext(typeReference, this.position, AnnotationTargetTypeConstants.OBJECT_CREATION);
		}
		super.newArray(typeReference, arrayBinding);
	}
	public void generateClassLiteralAccessForType(TypeReference typeReference, TypeBinding accessedType, FieldBinding syntheticFieldBinding) {
		if (typeReference != null && (typeReference.bits & ASTNode.HasTypeAnnotations) != 0) {
			addAnnotationContext(typeReference, this.position, AnnotationTargetTypeConstants.CLASS_LITERAL);
		}
		super.generateClassLiteralAccessForType(typeReference, accessedType, syntheticFieldBinding);
	}
	public void checkcast(TypeReference typeReference, TypeBinding typeBinding) {
		if (typeReference != null && (typeReference.bits & ASTNode.HasTypeAnnotations) != 0) {
			addAnnotationContext(typeReference, this.position, AnnotationTargetTypeConstants.TYPE_CAST);
		}
		super.checkcast(typeReference, typeBinding);
	}
	public void reset(ClassFile givenClassFile) {
		super.reset(givenClassFile);
		this.allTypeAnnotationContexts = new ArrayList();
	}
	public void init(ClassFile targetClassFile) {
		super.init(targetClassFile);
		this.allTypeAnnotationContexts = new ArrayList();
	}
	public void invoke(byte opcode, MethodBinding methodBinding, TypeBinding declaringClass, TypeReference[] typeArguments) {
		if (typeArguments != null) {
			int targetType = methodBinding.isConstructor()
					? AnnotationTargetTypeConstants.TYPE_ARGUMENT_CONSTRUCTOR_CALL
					: AnnotationTargetTypeConstants.TYPE_ARGUMENT_METHOD_CALL;
			for (int i = 0, max = typeArguments.length; i < max; i++) {
				TypeReference typeArgument = typeArguments[i];
				addAnnotationContext(typeArgument, this.position, i, targetType);
			}
		}
		super.invoke(opcode, methodBinding, declaringClass, typeArguments);
	}
}
