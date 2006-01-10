/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

public class CodeSnippetClassFile extends ClassFile {
/**
 * CodeSnippetClassFile constructor comment.
 * @param aType org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding
 * @param enclosingClassFile org.eclipse.jdt.internal.compiler.ClassFile
 * @param creatingProblemType boolean
 */
public CodeSnippetClassFile(
	org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding aType,
	org.eclipse.jdt.internal.compiler.ClassFile enclosingClassFile,
	boolean creatingProblemType) {
	/**
	 * INTERNAL USE-ONLY
	 * This methods creates a new instance of the receiver.
	 *
	 * @param aType org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding
	 * @param enclosingClassFile org.eclipse.jdt.internal.compiler.codegen.ClassFile
	 * @param creatingProblemType <CODE>boolean</CODE>
	 */
	this.referenceBinding = aType;
	initByteArrays();
	// generate the magic numbers inside the header
	this.header[this.headerOffset++] = (byte) (0xCAFEBABEL >> 24);
	this.header[this.headerOffset++] = (byte) (0xCAFEBABEL >> 16);
	this.header[this.headerOffset++] = (byte) (0xCAFEBABEL >> 8);
	this.header[this.headerOffset++] = (byte) (0xCAFEBABEL >> 0);

	this.targetJDK = this.referenceBinding.scope.compilerOptions().targetJDK;
	this.header[this.headerOffset++] = (byte) (targetJDK >> 8); // minor high
	this.header[this.headerOffset++] = (byte) (targetJDK >> 0); // minor low
	this.header[this.headerOffset++] = (byte) (targetJDK >> 24); // major high
	this.header[this.headerOffset++] = (byte) (targetJDK >> 16); // major low

	this.constantPoolOffset = this.headerOffset;
	this.headerOffset += 2;
	this.constantPool = new ConstantPool(this);
	int accessFlags = aType.getAccessFlags();
	
	if (!aType.isInterface()) { // class or enum
		accessFlags |= ClassFileConstants.AccSuper;
	}
	if (aType.isNestedType()) {
		if (aType.isStatic()) {
			// clear Acc_Static
			accessFlags &= ~ClassFileConstants.AccStatic;
		}
		if (aType.isPrivate()) {
			// clear Acc_Private and Acc_Public
			accessFlags &= ~(ClassFileConstants.AccPrivate | ClassFileConstants.AccPublic);
		}
		if (aType.isProtected()) {
			// clear Acc_Protected and set Acc_Public
			accessFlags &= ~ClassFileConstants.AccProtected;
			accessFlags |= ClassFileConstants.AccPublic;
		}
	}
	// clear Acc_Strictfp
	accessFlags &= ~ClassFileConstants.AccStrictfp;

	this.enclosingClassFile = enclosingClassFile;
	// now we continue to generate the bytes inside the contents array
	this.contents[this.contentsOffset++] = (byte) (accessFlags >> 8);
	this.contents[this.contentsOffset++] = (byte) accessFlags;
	int classNameIndex = this.constantPool.literalIndexForType(aType.constantPoolName());
	this.contents[this.contentsOffset++] = (byte) (classNameIndex >> 8);
	this.contents[this.contentsOffset++] = (byte) classNameIndex;
	int superclassNameIndex;
	if (aType.isInterface()) {
		superclassNameIndex = this.constantPool.literalIndexForType(ConstantPool.JavaLangObjectConstantPoolName);
	} else {
		superclassNameIndex =
			(aType.superclass == null ? 0 : this.constantPool.literalIndexForType(aType.superclass.constantPoolName()));
	}
	this.contents[this.contentsOffset++] = (byte) (superclassNameIndex >> 8);
	this.contents[this.contentsOffset++] = (byte) superclassNameIndex;
	ReferenceBinding[] superInterfacesBinding = aType.superInterfaces();
	int interfacesCount = superInterfacesBinding.length;
	this.contents[this.contentsOffset++] = (byte) (interfacesCount >> 8);
	this.contents[this.contentsOffset++] = (byte) interfacesCount;
	if (superInterfacesBinding != null) {
		for (int i = 0; i < interfacesCount; i++) {
			int interfaceIndex = this.constantPool.literalIndexForType(superInterfacesBinding[i].constantPoolName());
			this.contents[this.contentsOffset++] = (byte) (interfaceIndex >> 8);
			this.contents[this.contentsOffset++] = (byte) interfaceIndex;
		}
	}
	this.produceDebugAttributes = this.referenceBinding.scope.compilerOptions().produceDebugAttributes;
	this.innerClassesBindings = new ReferenceBinding[INNER_CLASSES_SIZE];
	this.creatingProblemType = creatingProblemType;
	this.codeStream = new CodeSnippetCodeStream(this);

	// retrieve the enclosing one guaranteed to be the one matching the propagated flow info
	// 1FF9ZBU: LFCOM:ALL - Local variable attributes busted (Sanity check)
	ClassFile outermostClassFile = this.outerMostEnclosingClassFile();
	if (this == outermostClassFile) {
		this.codeStream.maxFieldCount = aType.scope.referenceType().maxFieldCount;
	} else {
		this.codeStream.maxFieldCount = outermostClassFile.codeStream.maxFieldCount;
	}
}
/**
 * INTERNAL USE-ONLY
 * Request the creation of a ClassFile compatible representation of a problematic type
 *
 * @param typeDeclaration org.eclipse.jdt.internal.compiler.ast.TypeDeclaration
 * @param unitResult org.eclipse.jdt.internal.compiler.CompilationUnitResult
 */
public static void createProblemType(TypeDeclaration typeDeclaration, CompilationResult unitResult) {
	SourceTypeBinding typeBinding = typeDeclaration.binding;
	ClassFile classFile = new CodeSnippetClassFile(typeBinding, null, true);

	// inner attributes
	if (typeBinding.isMemberType())
		classFile.recordEnclosingTypeAttributes(typeBinding);

	// add its fields
	FieldBinding[] fields = typeBinding.fields;
	if ((fields != null) && (fields != Binding.NO_FIELDS)) {
		classFile.addFieldInfos();
	} else {
		// we have to set the number of fields to be equals to 0
		classFile.contents[classFile.contentsOffset++] = 0;
		classFile.contents[classFile.contentsOffset++] = 0;
	}
	// leave some space for the methodCount
	classFile.setForMethodInfos();
	// add its user defined methods
	MethodBinding[] methods = typeBinding.methods;
	AbstractMethodDeclaration[] methodDeclarations = typeDeclaration.methods;
	int maxMethodDecl = methodDeclarations == null ? 0 : methodDeclarations.length;
	int problemsLength;
	IProblem[] problems = unitResult.getErrors();
	if (problems == null) {
		problems = new IProblem[0];
	}
	IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
	System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
	if (methods != null) {
		if (typeBinding.isInterface()) {
			// we cannot create problem methods for an interface. So we have to generate a clinit
			// which should contain all the problem
			classFile.addProblemClinit(problemsCopy);
			for (int i = 0, max = methods.length; i < max; i++) {
				MethodBinding methodBinding;
				if ((methodBinding = methods[i]) != null) {
					// find the corresponding method declaration
					for (int j = 0; j < maxMethodDecl; j++) {
						if ((methodDeclarations[j] != null) && (methodDeclarations[j].binding == methods[i])) {
							if (!methodBinding.isConstructor()) {
								classFile.addAbstractMethod(methodDeclarations[j], methodBinding);
							}
							break;
						}
					}
				}
			}			
		} else {
			for (int i = 0, max = methods.length; i < max; i++) {
				MethodBinding methodBinding;
				if ((methodBinding = methods[i]) != null) {
					// find the corresponding method declaration
					for (int j = 0; j < maxMethodDecl; j++) {
						if ((methodDeclarations[j] != null) && (methodDeclarations[j].binding == methods[i])) {
							AbstractMethodDeclaration methodDecl;
							if ((methodDecl = methodDeclarations[j]).isConstructor()) {
								classFile.addProblemConstructor(methodDecl, methodBinding, problemsCopy);
							} else {
								classFile.addProblemMethod(methodDecl, methodBinding, problemsCopy);
							}
							break;
						}
					}
				}
			}
		}
		// add abstract methods
		classFile.addDefaultAbstractMethods();
	}
	// propagate generation of (problem) member types
	if (typeDeclaration.memberTypes != null) {
		for (int i = 0, max = typeDeclaration.memberTypes.length; i < max; i++) {
			TypeDeclaration memberType = typeDeclaration.memberTypes[i];
			if (memberType.binding != null) {
				classFile.recordNestedMemberAttribute(memberType.binding);
				ClassFile.createProblemType(memberType, unitResult);
			}
		}
	}
	classFile.addAttributes();
	unitResult.record(typeBinding.constantPoolName(), classFile);
}
}
