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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Node representing a structured Javadoc comment
 */
public class Javadoc extends ASTNode {

	public JavadocSingleNameReference[] parameters; // @param
	public TypeReference[] thrownExceptions; // @throws, @exception
	public JavadocReturnStatement returnStatement; // @return
	public Expression[] references; // @see
	
	public Javadoc(int sourceStart, int sourceEnd) {
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}
	
	/*
	 * @see org.eclipse.jdt.internal.compiler.ast.ASTNode#print(int, java.lang.StringBuffer)
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
	 * Resolve type javadoc while a class scope
	 */
	public void resolve(ClassScope classScope) {

		// @param tags
		int paramTagsSize = parameters == null ? 0 : parameters.length;
		for (int i = 0; i < paramTagsSize; i++) {
			JavadocSingleNameReference param = parameters[i];
			classScope.problemReporter().javadocUnexpectedTag(param.tagSourceStart, param.tagSourceEnd);
		}

		// @return tags
		if (this.returnStatement != null) {
			classScope.problemReporter().javadocUnexpectedTag(this.returnStatement.sourceStart, this.returnStatement.sourceEnd);
		}

		// @throws/@exception tags
		int throwsTagsNbre = thrownExceptions == null ? 0 : thrownExceptions.length;
		for (int i = 0; i < throwsTagsNbre; i++) {
			TypeReference typeRef = thrownExceptions[i];
			int start, end;
			if (typeRef instanceof JavadocSingleTypeReference) {
				JavadocSingleTypeReference singleRef = (JavadocSingleTypeReference) typeRef;
				start = singleRef.tagSourceStart;
				end = singleRef.tagSourceEnd;
			} else if (typeRef instanceof JavadocQualifiedTypeReference) {
				JavadocQualifiedTypeReference qualifiedRef = (JavadocQualifiedTypeReference) typeRef;
				start = qualifiedRef.tagSourceStart;
				end = qualifiedRef.tagSourceEnd;
			} else {
				start = typeRef.sourceStart;
				end = typeRef.sourceEnd;
			}
			classScope.problemReporter().javadocUnexpectedTag(start, end);
		}

		// @see tags
		int seeTagsNbre = references == null ? 0 : references.length;
		for (int i = 0; i < seeTagsNbre; i++) {
			references[i].resolveType(classScope);
		}
	}
	
	/*
	 * Resolve method javadoc while a method scope
	 */
	public void resolve(MethodScope methScope) {
		
		// get method declaration
		AbstractMethodDeclaration methDecl = methScope.referenceMethod();
		boolean override = (methDecl.binding.modifiers & (AccImplementing+AccOverriding)) != 0;

		// @see tags
		int seeTagsNbre = references == null ? 0 : references.length;
		boolean superRef = false;
		for (int i = 0; i < seeTagsNbre; i++) {
			references[i].resolveType(methScope);
			try {
				// see whether we can have a super reference
				if ((methDecl.isConstructor() || override) && !superRef) {
					if (references[i] instanceof JavadocMessageSend) {
						JavadocMessageSend messageSend = (JavadocMessageSend) references[i];
						// if binding is valid then look if we have a reference to an overriden method/constructor
						if (messageSend.binding != null && messageSend.binding.isValidBinding()) {
							if (methDecl.binding.declaringClass.isCompatibleWith(messageSend.receiverType) &&
								CharOperation.equals(messageSend.selector, methDecl.selector) &&
								(messageSend.binding.returnType == methDecl.binding.returnType)) {
								if (messageSend.arguments == null && methDecl.arguments == null) {
									superRef = true;
								}
								else if (messageSend.arguments != null && methDecl.arguments != null) {
									superRef = methDecl.binding.areParametersEqual(messageSend.binding);
								}
							}
						}
					}
					else if (references[i] instanceof JavadocAllocationExpression) {
						JavadocAllocationExpression allocationExpr = (JavadocAllocationExpression) references[i];
						// if binding is valid then look if we have a reference to an overriden method/constructor
						if (allocationExpr.binding != null && allocationExpr.binding.isValidBinding()) {
							if (methDecl.binding.declaringClass.isCompatibleWith(allocationExpr.resolvedType)) {
								if (allocationExpr.arguments == null && methDecl.arguments == null) {
									superRef = true;
								}
								else if (allocationExpr.arguments != null && methDecl.arguments != null) {
									superRef = methDecl.binding.areParametersEqual(allocationExpr.binding);
								}
							}
						}
					}
				}
			}
			catch (Exception e) {
				// Something wrong happen, forgot super ref...
			}
		}
		
		// Store if a reference exists to an overriden method/constructor or the method is in a local type,
		boolean reportMissing = !(superRef || (methDecl.binding.declaringClass != null && methDecl.binding.declaringClass.isLocalType()));

		// @param tags
		resolveParamTags(methScope, reportMissing);

		// @return tags
		if (this.returnStatement == null) {
			if (reportMissing) {
				if (!methDecl.isConstructor() && !methDecl.isClinit()) {
					MethodDeclaration meth = (MethodDeclaration) methDecl;
					if (meth.binding.returnType != VoidBinding) {
						// method with return should have @return tag
						methScope.problemReporter().javadocMissingReturnTag(meth.returnType.sourceStart, meth.returnType.sourceEnd);
					}
				}
			}
		} else {
			this.returnStatement.resolve(methScope);
		}

		// @throws/@exception tags
		resolveThrowsTags(methScope, reportMissing);
	}
	
	/*
	 * Resolve @param tags while method scope
	 */
	private void resolveParamTags(MethodScope methScope, boolean reportMissing) {
		AbstractMethodDeclaration md = methScope.referenceMethod();
		int paramTagsSize = parameters == null ? 0 : parameters.length;
		int argumentsSize = md.arguments == null ? 0 : md.arguments.length;

		// If no param tags then report a problem for each method argument
		if (paramTagsSize == 0) {
			if (reportMissing) {
				for (int i = 0; i < argumentsSize; i++) {
					Argument arg = md.arguments[i];
					methScope.problemReporter().javadocMissingParamTag(arg);
				}
			}
		} else {
			LocalVariableBinding[] bindings = new LocalVariableBinding[paramTagsSize];
			int maxBindings = 0;

			// Scan all @param tags
			for (int i = 0; i < paramTagsSize; i++) {
				JavadocSingleNameReference param = parameters[i];
				param.resolve(methScope);
				if (param.binding != null && param.binding.isValidBinding()) {
					// Verify duplicated tags
					boolean found = false;
					for (int j = 0; j < maxBindings && !found; j++) {
						if (bindings[j] == param.binding) {
							methScope.problemReporter().javadocInvalidParamName(param, true);
							found = true;
						}
					}
					if (!found) {
						bindings[maxBindings++] = (LocalVariableBinding) param.binding;
					}
				}
			}

			// Look for undocumented arguments
			if (reportMissing) {
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
						methScope.problemReporter().javadocMissingParamTag(arg);
					}
				}
			}
		}
	}

	/*
	 * Resolve @throws/@exception tags while method scope
	 */
	private void resolveThrowsTags(MethodScope methScope, boolean reportMissing) {
		AbstractMethodDeclaration md = methScope.referenceMethod();
		int throwsTagsNbre = thrownExceptions == null ? 0 : thrownExceptions.length;
		int thrownExceptionSize = md.thrownExceptions == null ? 0 : md.thrownExceptions.length;

		// If no throws tags then report a problem for each method thrown exception
		if (throwsTagsNbre == 0) {
			if (reportMissing) {
				for (int i = 0; i < thrownExceptionSize; i++) {
					TypeReference typeRef = md.thrownExceptions[i];
					if (typeRef.resolvedType != null && typeRef.resolvedType.isValidBinding()) { // flag only valid class name
						methScope.problemReporter().javadocMissingThrowsTag(typeRef);
					}
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

				if (typeBinding != null && typeBinding.isValidBinding() && typeBinding.isClass()) {
					// Verify duplicated tags
					boolean found = false;
					for (int j = 0; j < maxRef && !found; j++) {
						if (typeReferences[j].resolvedType == typeBinding) {
							methScope.problemReporter().javadocInvalidThrowsClassName(typeRef, true);
							found = true;
						}
					}
					if (!found) {
						typeReferences[maxRef++] = typeRef;
					}
				}
			}

			// Look for undocumented thrown exception
			if (reportMissing) {
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
						if (exception.resolvedType != null && exception.resolvedType.isValidBinding()) { // flag only valid class name
							methScope.problemReporter().javadocMissingThrowsTag(exception);
						}
					}
				}
			}

			// Verify that additional @throws tags are unchecked exception
			for (int i = 0; i < maxRef; i++) {
				TypeReference typeRef = typeReferences[i];
				if (typeRef != null) {
					if (!typeRef.resolvedType.isCompatibleWith(methScope.getJavaLangRuntimeException())
							&& !typeRef.resolvedType.isCompatibleWith(methScope.getJavaLangError())) {
						methScope.problemReporter().javadocInvalidThrowsClassName(typeRef, false);
					}
				}
			}
		}
	}
}
