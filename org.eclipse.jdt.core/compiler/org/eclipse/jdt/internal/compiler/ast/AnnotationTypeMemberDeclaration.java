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
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;

public class AnnotationTypeMemberDeclaration extends AbstractMethodDeclaration {
	
	public TypeReference returnType;
	public Expression memberValue;
	public int extendedDimensions;

	/**
	 * MethodDeclaration constructor comment.
	 */
	public AnnotationTypeMemberDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}

	public void analyseCode(
		ClassScope classScope,
		InitializationFlowContext initializationContext,
		FlowInfo flowInfo) {

		// starting of the code analysis for methods
		if (ignoreFurtherInvestigation) {
			return;
		}
		if (this.extendedDimensions != 0) {
			scope.problemReporter().illegalExtendedDimensions(this);		
		}
		try {
			if (binding == null) {
				return;
			}
				
			if (this.binding.isPrivate() && !this.binding.isPrivateUsed()) {
				if (!classScope.referenceCompilationUnit().compilationResult.hasSyntaxError()) {
					scope.problemReporter().unusedPrivateMethod(this);
				}
			}
				
			// may be in a non necessary <clinit> for innerclass with static final constant fields
			if (binding.isAbstract() || binding.isNative())
				return;

			ExceptionHandlingFlowContext methodContext =
				new ExceptionHandlingFlowContext(
					initializationContext,
					this,
					binding.thrownExceptions,
					scope,
					FlowInfo.DEAD_END);

			// propagate to statements
			if (statements != null) {
				boolean didAlreadyComplain = false;
				for (int i = 0, count = statements.length; i < count; i++) {
					Statement stat = statements[i];
					if (!stat.complainIfUnreachable(flowInfo, scope, didAlreadyComplain)) {
						flowInfo = stat.analyseCode(scope, methodContext, flowInfo);
					} else {
						didAlreadyComplain = true;
					}
				}
			}
			// check for missing returning path
			TypeBinding returnTypeBinding = binding.returnType;
			if ((returnTypeBinding == VoidBinding) || isAbstract()) {
				this.needFreeReturn = flowInfo.isReachable();
			} else {
				if (flowInfo != FlowInfo.DEAD_END) { 
					scope.problemReporter().shouldReturn(returnTypeBinding, this);
				}
			}
			// check unreachable catch blocks
			methodContext.complainIfUnusedExceptionHandlers(this);
		} catch (AbortMethod e) {
			this.ignoreFurtherInvestigation = true;
		}
	}


	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		// nothing to do
		// annotation type member declaration don't have any body
	}
	
	public StringBuffer printReturnType(int indent, StringBuffer output) {

		if (returnType == null) return output;
		return returnType.printExpression(0, output).append(' ');
	}

	public void resolve(ClassScope upperScope) {
		if (this.binding == null) {
			this.ignoreFurtherInvestigation = true;
		}

		try {
			resolveStatements();
			resolveJavadoc();
		} catch (AbortMethod e) {	// ========= abort on fatal error =============
			this.ignoreFurtherInvestigation = true;
		} 
	}
	
	public void resolveStatements() {

		// ========= abort on fatal error =============
		if (this.returnType != null && this.binding != null) {
			this.returnType.resolvedType = this.binding.returnType;
			// record the return type binding
		}
		// check if method with constructor name
		if (CharOperation.equals(scope.enclosingSourceType().sourceName, selector)) {
			scope.problemReporter().annotationTypeMemberDeclarationWithConstructorName(this);
		}
		super.resolveStatements(); 
	}

	public void traverse(
		ASTVisitor visitor,
		ClassScope classScope) {

		if (visitor.visit(this, classScope)) {
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, scope);
			}
			if (returnType != null) {
				returnType.traverse(visitor, scope);
			}
			if (memberValue != null) {
				memberValue.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, classScope);
	}
}
