/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Node representing a structured Javadoc annotation comment
 */
public class Annotation extends AstNode {

	public AnnotationSingleNameReference[] parameters; // @param
	public TypeReference[] thrownExceptions; // @throws, @exception
	public AnnotationReturnStatement returnStatement; // @return
	public Expression[] references; // @see
	public Annotation(int sourceStart, int sourceEnd) {
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}
	
	/*
	 * @see org.eclipse.jdt.internal.compiler.ast.AstNode#print(int, java.lang.StringBuffer)
	 */
	public StringBuffer print(int indent, StringBuffer output) {
		printIndent(indent, output).append("/**\n"); //$NON-NLS-1$
		if (this.parameters != null) {
			for (int i = 0, length = this.parameters.length; i < length; i++) {
				printIndent(indent + 1, output).append(" * @param "); //$NON-NLS-1$		
				this.parameters[i].print(indent, output).append('\n');
			}
		}
		if (this.returnStatement != null) {
			printIndent(indent + 1, output).append(" * @return\n"); //$NON-NLS-1$		
		}
		if (this.thrownExceptions != null) {
			for (int i = 0, length = this.thrownExceptions.length; i < length; i++) {
				printIndent(indent + 1, output).append(" * @throws "); //$NON-NLS-1$		
				this.thrownExceptions[i].print(indent, output).append('\n');
			}
		}
		if (this.references != null) {
			for (int i = 0, length = this.references.length; i < length; i++) {
				printIndent(indent + 1, output).append(" * @see"); //$NON-NLS-1$		
				this.references[i].print(indent, output).append('\n');
			}
		}
		printIndent(indent, output).append(" */\n"); //$NON-NLS-1$
		return output;
	}

	/*
	 * Resolve type annotation while a class scope
	 */
	public void resolve(ClassScope classScope) {

		// @param tags
		int paramTagsSize = parameters == null ? 0 : parameters.length;
		for (int i = 0; i < paramTagsSize; i++) {
			AnnotationSingleNameReference param = parameters[i];
			classScope.problemReporter().annotationUnexpectedTag(param.tagSourceStart, param.tagSourceEnd);
		}

		// @return tags
		if (this.returnStatement != null) {
			classScope.problemReporter().annotationUnexpectedTag(this.returnStatement.sourceStart, this.returnStatement.sourceEnd);
		}

		// @throws/@exception tags
		int throwsTagsNbre = thrownExceptions == null ? 0 : thrownExceptions.length;
		for (int i = 0; i < throwsTagsNbre; i++) {
			TypeReference typeRef = thrownExceptions[i];
			int start, end;
			if (typeRef instanceof AnnotationSingleTypeReference) {
				AnnotationSingleTypeReference singleRef = (AnnotationSingleTypeReference) typeRef;
				start = singleRef.tagSourceStart;
				end = singleRef.tagSourceEnd;
			} else if (typeRef instanceof AnnotationQualifiedTypeReference) {
				AnnotationQualifiedTypeReference qualifiedRef = (AnnotationQualifiedTypeReference) typeRef;
				start = qualifiedRef.tagSourceStart;
				end = qualifiedRef.tagSourceEnd;
			} else {
				start = typeRef.sourceStart;
				end = typeRef.sourceEnd;
			}
			classScope.problemReporter().annotationUnexpectedTag(start, end);
		}

		// @see tags
		int seeTagsNbre = references == null ? 0 : references.length;
		for (int i = 0; i < seeTagsNbre; i++) {
			references[i].resolveType(classScope);
		}
	}
	
	/*
	 * Resolve method annotation while a method scope
	 */
	public void resolve(MethodScope methScope) {

		// @param tags
		resolveParamTags(methScope);

		// @return tags
		if (this.returnStatement == null) {
			AbstractMethodDeclaration md = methScope.referenceMethod();
			if (!md.isConstructor() && !md.isClinit()) {
				MethodDeclaration meth = (MethodDeclaration) md;
				if (meth.binding.returnType != VoidBinding) {
					// method with return should have @return tag
					//int end = md.sourceStart + md.selector.length - 1;
					methScope.problemReporter().annotationInvalidReturnTag(meth.returnType.sourceStart, meth.returnType.sourceEnd, true);
				}
			}
		} else {
			this.returnStatement.resolve(methScope);
		}

		// @throws/@exception tags
		resolveThrowsTags(methScope);

		// @see tags
		int seeTagsNbre = references == null ? 0 : references.length;
		for (int i = 0; i < seeTagsNbre; i++) {
			references[i].resolveType(methScope);
		}
	}
	
	/*
	 * Resolve @param tags while method scope
	 */
	private void resolveParamTags(MethodScope methScope) {
		AbstractMethodDeclaration md = methScope.referenceMethod();
		int paramTagsSize = parameters == null ? 0 : parameters.length;
		int argumentsSize = md.arguments == null ? 0 : md.arguments.length;

		// If no param tags then report a problem for each method argument
		if (paramTagsSize == 0) {
			for (int i = 0; i < argumentsSize; i++) {
				Argument arg = md.arguments[i];
				methScope.problemReporter().annotationMissingParamTag(arg);
			}
		} else {
			LocalVariableBinding[] bindings = new LocalVariableBinding[paramTagsSize];
			int maxBindings = 0;

			// Scan all @param tags
			for (int i = 0; i < paramTagsSize; i++) {
				AnnotationSingleNameReference param = parameters[i];
				param.resolve(methScope);
				if (param.binding != null) {
					// Verify duplicated tags
					boolean found = false;
					for (int j = 0; j < maxBindings && !found; j++) {
						if (bindings[j] == param.binding) {
							methScope.problemReporter().annotationInvalidParamName(param, true);
							found = true;
						}
					}
					if (!found) {
						bindings[maxBindings++] = (LocalVariableBinding) param.binding;
					}
				}
			}

			// Look for undocumented arguments
			for (int i = 0; i < argumentsSize; i++) {
				Argument arg = md.arguments[i];
				boolean found = false;
				for (int j = 0; j < maxBindings && !found; j++) {
					LocalVariableBinding binding = bindings[j];
					if (arg.binding == binding) {
						found = true;
					}
				}
				if (!found) {
					methScope.problemReporter().annotationMissingParamTag(arg);
				}
			}
		}
	}

	/*
	 * Resolve @throws/@exception tags while method scope
	 */
	private void resolveThrowsTags(MethodScope methScope) {
		AbstractMethodDeclaration md = methScope.referenceMethod();
		int throwsTagsNbre = thrownExceptions == null ? 0 : thrownExceptions.length;
		int thrownExceptionSize = md.thrownExceptions == null ? 0 : md.thrownExceptions.length;

		// If no throws tags then report a problem for each method thrown exception
		if (throwsTagsNbre == 0) {
			for (int i = 0; i < thrownExceptionSize; i++) {
				TypeReference typeRef = md.thrownExceptions[i];
				if (typeRef.resolvedType != null) { // flag only valid class name
					methScope.problemReporter().annotationMissingThrowsTag(typeRef);
				}
			}
		} else {
			int maxRef = 0;
			TypeReference[] typeReferences = new TypeReference[throwsTagsNbre];

			// Scan all @throws tags
			for (int i = 0; i < throwsTagsNbre; i++) {
				TypeReference typeRef = thrownExceptions[i];
				typeRef.resolve(methScope);
				TypeBinding typeBinding = typeRef.resolvedType;

				if (typeBinding.isValidBinding() && typeBinding.isClass()) {
					// Verify duplicated tags
					boolean found = false;
					for (int j = 0; j < maxRef && !found; j++) {
						if (typeReferences[j].resolvedType == typeBinding) {
							methScope.problemReporter().annotationInvalidThrowsClassName(typeRef, true);
							found = true;
						}
					}
					if (!found) {
						typeReferences[maxRef++] = typeRef;
					}
				}
			}

			// Look for undocumented thrown exception
			for (int i = 0; i < thrownExceptionSize; i++) {
				TypeReference exception = md.thrownExceptions[i];
				boolean found = false;
				for (int j = 0; j < maxRef && !found; j++) {
					if (typeReferences[j] != null) {
						TypeBinding typeBinding = typeReferences[j].resolvedType;
						if (exception.resolvedType == typeBinding) {
							found = true;
							typeReferences[j] = null;
						}
					}
				}
				if (!found) {
					if (exception.resolvedType != null) { // flag only valid class name
						methScope.problemReporter().annotationMissingThrowsTag(exception);
					}
				}
			}

			// Verify that additional @throws tags are unchecked exception
			for (int i = 0; i < maxRef; i++) {
				TypeReference typeRef = typeReferences[i];
				if (typeRef != null) {
					if (!typeRef.resolvedType.isCompatibleWith(methScope.getJavaLangRuntimeException())
							&& !typeRef.resolvedType.isCompatibleWith(methScope.getJavaLangError())) {
						methScope.problemReporter().annotationInvalidThrowsClassName(typeRef, false);
					}
				}
			}
		}
	}
}