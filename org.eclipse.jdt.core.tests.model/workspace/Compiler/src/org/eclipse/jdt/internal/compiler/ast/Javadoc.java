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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.AbstractCommentParser;

/**
 * Node representing a structured Javadoc comment
 */
public class Javadoc extends ASTNode {

	public JavadocSingleNameReference[] parameters; // @param
	public TypeReference[] thrownExceptions; // @throws, @exception
	public JavadocReturnStatement returnStatement; // @return
	public Expression[] references; // @see
	public boolean inherited = false;
	// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51600
	// Store param references for tag with invalid syntax
	public JavadocSingleNameReference[] invalidParameters; // @param

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
		int paramTagsSize = this.parameters == null ? 0 : this.parameters.length;
		for (int i = 0; i < paramTagsSize; i++) {
			JavadocSingleNameReference param = this.parameters[i];
			classScope.problemReporter().javadocUnexpectedTag(param.tagSourceStart, param.tagSourceEnd);
		}

		// @return tags
		if (this.returnStatement != null) {
			classScope.problemReporter().javadocUnexpectedTag(this.returnStatement.sourceStart, this.returnStatement.sourceEnd);
		}

		// @throws/@exception tags
		int throwsTagsLength = this.thrownExceptions == null ? 0 : this.thrownExceptions.length;
		for (int i = 0; i < throwsTagsLength; i++) {
			TypeReference typeRef = this.thrownExceptions[i];
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
		int seeTagsLength = this.references == null ? 0 : this.references.length;
		for (int i = 0; i < seeTagsLength; i++) {
			resolveReference(this.references[i], classScope);
		}
	}
	
	/*
	 * Resolve method javadoc while a method scope
	 */
	public void resolve(MethodScope methScope) {
		
		// get method declaration
		AbstractMethodDeclaration methDecl = methScope.referenceMethod();
		boolean overriding = methDecl == null ? false : (methDecl.binding.modifiers & (AccImplementing+AccOverriding)) != 0;

		// @see tags
		int seeTagsLength = this.references == null ? 0 : this.references.length;
		boolean superRef = false;
		for (int i = 0; i < seeTagsLength; i++) {
			
			// Resolve reference
			resolveReference(this.references[i], methScope);
			
			// see whether we can have a super reference
			try {
				if (methDecl != null && (methDecl.isConstructor() || overriding) && !superRef) {
					if (this.references[i] instanceof JavadocMessageSend) {
						JavadocMessageSend messageSend = (JavadocMessageSend) this.references[i];
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
					else if (this.references[i] instanceof JavadocAllocationExpression) {
						JavadocAllocationExpression allocationExpr = (JavadocAllocationExpression) this.references[i];
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
				// Something wrong happen, forget super ref...
			}
		}
		
		// Store if a reference exists to an overriden method/constructor or the method is in a local type,
		boolean reportMissing = methDecl == null || !((overriding && this.inherited) || superRef || (methDecl.binding.declaringClass != null && methDecl.binding.declaringClass.isLocalType()));

		// @param tags
		resolveParamTags(methScope, reportMissing);

		// @return tags
		if (this.returnStatement == null) {
			if (reportMissing && methDecl != null) {
				if (methDecl.isMethod()) {
					MethodDeclaration meth = (MethodDeclaration) methDecl;
					if (meth.binding.returnType != VoidBinding) {
						// method with return should have @return tag
						methScope.problemReporter().javadocMissingReturnTag(meth.returnType.sourceStart, meth.returnType.sourceEnd, methDecl.binding.modifiers);
					}
				}
			}
		} else {
			this.returnStatement.resolve(methScope);
		}

		// @throws/@exception tags
		resolveThrowsTags(methScope, reportMissing);

		// Resolve param tags with invalid syntax
		int length = this.invalidParameters == null ? 0 : this.invalidParameters.length;
		for (int i = 0; i < length; i++) {
			this.invalidParameters[i].resolve(methScope, false);
		}
	}
	
	private void resolveReference(Expression reference, Scope scope) {

		// Perform resolve
		switch (scope.kind) {
			case Scope.METHOD_SCOPE:
				reference.resolveType((MethodScope)scope);
			break;
			case Scope.CLASS_SCOPE:
				reference.resolveType((ClassScope)scope);
			break;
		}

		// Verify field references
		boolean verifyValues = scope.environment().options.sourceLevel >= ClassFileConstants.JDK1_5;
		if (reference instanceof JavadocFieldReference) {
			JavadocFieldReference fieldRef = (JavadocFieldReference) reference;
			
			// Verify if this is a method reference
			// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=51911
			if (fieldRef.methodBinding != null) {
				// cannot refer to method for @value tag
				if (fieldRef.tagValue == AbstractCommentParser.TAG_VALUE_VALUE) {
					scope.problemReporter().javadocInvalidValueReference(fieldRef.sourceStart, fieldRef.sourceEnd);
				}
				else if (fieldRef.receiverType != null) {
					fieldRef.superAccess = scope.enclosingSourceType().isCompatibleWith(fieldRef.receiverType);
					fieldRef.methodBinding = scope.findMethod((ReferenceBinding)fieldRef.receiverType, fieldRef.token, new TypeBinding[0], fieldRef);
				}
			}

			// Verify whether field ref should be static or not (for @value tags)
			else if (verifyValues && fieldRef.binding != null && fieldRef.binding.isValidBinding()) {
				if (fieldRef.tagValue == AbstractCommentParser.TAG_VALUE_VALUE && !fieldRef.binding.isStatic()) {
					scope.problemReporter().javadocInvalidValueReference(fieldRef.sourceStart, fieldRef.sourceEnd);
				}
			}
		}

		// If not 1.5 level, verification is finished
		if (!verifyValues)  return;

		// Verify that message reference are not used for @value tags
		else if (reference instanceof JavadocMessageSend) {
			JavadocMessageSend msgSend = (JavadocMessageSend) reference;
			if (msgSend.tagValue == AbstractCommentParser.TAG_VALUE_VALUE) { // cannot refer to method for @value tag
				scope.problemReporter().javadocInvalidValueReference(msgSend.sourceStart, msgSend.sourceEnd);
			}
		}

		// Verify that constructorreference are not used for @value tags
		else if (reference instanceof JavadocAllocationExpression) {
			JavadocAllocationExpression alloc = (JavadocAllocationExpression) reference;
			if (alloc.tagValue == AbstractCommentParser.TAG_VALUE_VALUE) { // cannot refer to method for @value tag
				scope.problemReporter().javadocInvalidValueReference(alloc.sourceStart, alloc.sourceEnd);
			}
		}
	}
	
	/*
	 * Resolve @param tags while method scope
	 */
	private void resolveParamTags(MethodScope methScope, boolean reportMissing) {
		AbstractMethodDeclaration md = methScope.referenceMethod();
		int paramTagsSize = this.parameters == null ? 0 : this.parameters.length;

		// If no referenced method (field initializer for example) then report a problem for each param tag
		if (md == null) {
			for (int i = 0; i < paramTagsSize; i++) {
				JavadocSingleNameReference param = this.parameters[i];
				methScope.problemReporter().javadocUnexpectedTag(param.tagSourceStart, param.tagSourceEnd);
			}
			return;
		}
		
		// If no param tags then report a problem for each method argument
		int argumentsSize = md.arguments == null ? 0 : md.arguments.length;
		if (paramTagsSize == 0) {
			if (reportMissing) {
				for (int i = 0; i < argumentsSize; i++) {
					Argument arg = md.arguments[i];
					methScope.problemReporter().javadocMissingParamTag(arg, md.binding.modifiers);
				}
			}
		} else {
			LocalVariableBinding[] bindings = new LocalVariableBinding[paramTagsSize];
			int maxBindings = 0;

			// Scan all @param tags
			for (int i = 0; i < paramTagsSize; i++) {
				JavadocSingleNameReference param = this.parameters[i];
				param.resolve(methScope);
				if (param.binding != null && param.binding.isValidBinding()) {
					// Verify duplicated tags
					boolean found = false;
					for (int j = 0; j < maxBindings && !found; j++) {
						if (bindings[j] == param.binding) {
							methScope.problemReporter().javadocDuplicatedParamTag(param, md.binding.modifiers);
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
						methScope.problemReporter().javadocMissingParamTag(arg, md.binding.modifiers);
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
		int throwsTagsLength = this.thrownExceptions == null ? 0 : this.thrownExceptions.length;

		// If no referenced method (field initializer for example) then report a problem for each throws tag
		if (md == null) {
			for (int i = 0; i < throwsTagsLength; i++) {
				TypeReference typeRef = this.thrownExceptions[i];
				int start = typeRef.sourceStart;
				int end = typeRef.sourceEnd;
				if (typeRef instanceof JavadocQualifiedTypeReference) {
					start = ((JavadocQualifiedTypeReference) typeRef).tagSourceStart;
					end = ((JavadocQualifiedTypeReference) typeRef).tagSourceEnd;
				} else if (typeRef instanceof JavadocSingleTypeReference) {
					start = ((JavadocSingleTypeReference) typeRef).tagSourceStart;
					end = ((JavadocSingleTypeReference) typeRef).tagSourceEnd;
				}
				methScope.problemReporter().javadocUnexpectedTag(start, end);
			}
			return;
		}

		// If no throws tags then report a problem for each method thrown exception
		int boundExceptionLength = (md.binding == null || md.binding.thrownExceptions == null) ? 0 : md.binding.thrownExceptions.length;
		int thrownExceptionLength = md.thrownExceptions == null ? 0 : md.thrownExceptions.length;
		if (throwsTagsLength == 0) {
			if (reportMissing) {
				for (int i = 0; i < boundExceptionLength; i++) {
					ReferenceBinding exceptionBinding = md.binding.thrownExceptions[i];
					if (exceptionBinding != null && exceptionBinding.isValidBinding()) { // flag only valid class name
						int j=i;
						while (j<thrownExceptionLength && exceptionBinding != md.thrownExceptions[j].resolvedType) j++;
						if (j<thrownExceptionLength) {
							methScope.problemReporter().javadocMissingThrowsTag(md.thrownExceptions[j], md.binding.modifiers);
						}
					}
				}
			}
		} else {
			int maxRef = 0;
			TypeReference[] typeReferences = new TypeReference[throwsTagsLength];

			// Scan all @throws tags
			for (int i = 0; i < throwsTagsLength; i++) {
				TypeReference typeRef = this.thrownExceptions[i];
				typeRef.resolve(methScope);
				TypeBinding typeBinding = typeRef.resolvedType;

				if (typeBinding != null && typeBinding.isValidBinding() && typeBinding.isClass()) {
					typeReferences[maxRef++] = typeRef;
				}
			}

			// Look for undocumented thrown exception
			for (int i = 0; i < boundExceptionLength; i++) {
				ReferenceBinding exceptionBinding = md.binding.thrownExceptions[i];
				boolean found = false;
				for (int j = 0; j < maxRef && !found; j++) {
					if (typeReferences[j] != null) {
						TypeBinding typeBinding = typeReferences[j].resolvedType;
						if (exceptionBinding == typeBinding) {
							found = true;
							typeReferences[j] = null;
						}
					}
				}
				if (!found && reportMissing) {
					if (exceptionBinding != null && exceptionBinding.isValidBinding()) { // flag only valid class name
						int k=i;
						while (k<thrownExceptionLength && exceptionBinding != md.thrownExceptions[k].resolvedType) k++;
						if (k<thrownExceptionLength) {
							methScope.problemReporter().javadocMissingThrowsTag(md.thrownExceptions[k], md.binding.modifiers);
						}
					}
				}
			}

			// Verify additional @throws tags
			for (int i = 0; i < maxRef; i++) {
				TypeReference typeRef = typeReferences[i];
				if (typeRef != null) {
					boolean compatible = false;
					// thrown exceptions subclasses are accepted
					for (int j = 0; j<thrownExceptionLength && !compatible; j++) {
						TypeBinding exceptionBinding = md.thrownExceptions[j].resolvedType;
						if (exceptionBinding != null) {
							compatible = typeRef.resolvedType.isCompatibleWith(exceptionBinding);
						}
					}
			
					//  If not compatible only complain on unchecked exception
					if (!compatible &&
						 !typeRef.resolvedType.isCompatibleWith(methScope.getJavaLangRuntimeException()) &&
						 !typeRef.resolvedType.isCompatibleWith(methScope.getJavaLangError())) {
						methScope.problemReporter().javadocInvalidThrowsClassName(typeRef, md.binding.modifiers);
					}
				}
			}
		}
	}
	
	/*
	 * Search node with a given staring position in javadoc objects arrays.
	 */
	public ASTNode getNodeStartingAt(int start) {
		// parameters array
		if (this.parameters != null) {
			for (int i=0; i<this.parameters.length; i++) {
				JavadocSingleNameReference param = this.parameters[i];
				if (param.sourceStart==start) {
					return param;
				}
			}
		}
		// array of invalid syntax tags parameters
		if (this.invalidParameters != null) {
			for (int i=0; i<this.invalidParameters.length; i++) {
				JavadocSingleNameReference param = this.invalidParameters[i];
				if (param.sourceStart==start) {
					return param;
				}
			}
		}
		// thrown exception array
		if (this.thrownExceptions != null) {
			for (int i=0; i<this.thrownExceptions.length; i++) {
				TypeReference typeRef = this.thrownExceptions[i];
				if (typeRef.sourceStart==start) {
					return typeRef;
				}
			}
		}
		// references array
		if (this.references != null) {
			for (int i=0; i<this.references.length; i++) {
				org.eclipse.jdt.internal.compiler.ast.Expression expression = this.references[i];
				if (expression.sourceStart==start) {
					return expression;
				} else if (expression instanceof JavadocAllocationExpression) {
					JavadocAllocationExpression allocationExpr = (JavadocAllocationExpression) this.references[i];
					// if binding is valid then look at arguments
					if (allocationExpr.binding != null && allocationExpr.binding.isValidBinding()) {
						if (allocationExpr.arguments != null) {
							for (int j=0; j<allocationExpr.arguments.length; j++) {
								if (allocationExpr.arguments[j].sourceStart == start) {
									return allocationExpr.arguments[j];
								}
							}
						}
					}
				} else if (expression instanceof JavadocMessageSend) {
					JavadocMessageSend messageSend = (JavadocMessageSend) this.references[i];
					// if binding is valid then look at arguments
					if (messageSend.binding != null && messageSend.binding.isValidBinding()) {
						if (messageSend.arguments != null) {
							for (int j=0; j<messageSend.arguments.length; j++) {
								if (messageSend.arguments[j].sourceStart == start) {
									return messageSend.arguments[j];
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
}
