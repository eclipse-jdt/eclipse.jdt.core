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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.env.IGenericType;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;

public class MethodDeclaration extends AbstractMethodDeclaration {
	
	public TypeReference returnType;
	public TypeParameter[] typeParameters;
	
	/**
	 * MethodDeclaration constructor comment.
	 */
	public MethodDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}

	public void analyseCode(
		ClassScope classScope,
		InitializationFlowContext initializationContext,
		FlowInfo flowInfo) {

		// starting of the code analysis for methods
		if (ignoreFurtherInvestigation)
			return;
		try {
			if (binding == null)
				return;
				
			if (this.binding.isPrivate() && !this.binding.isPrivateUsed()) {
				if (!classScope.referenceCompilationUnit().compilationResult.hasSyntaxError()) {
					scope.problemReporter().unusedPrivateMethod(this);
				}
			}
				
			// skip enum implicit methods
			if (binding.declaringClass.isEnum() && (this.selector == TypeConstants.VALUES || this.selector == TypeConstants.VALUEOF))
				return;

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

			// tag parameters as being set
			if (this.arguments != null) {
				for (int i = 0, count = this.arguments.length; i < count; i++) {
					flowInfo.markAsDefinitelyAssigned(this.arguments[i].binding);
				}
			}
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

	public boolean isMethod() {

		return true;
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {

		//fill up the method body with statement
		if (ignoreFurtherInvestigation)
			return;
		parser.parse(this, unit);
	}

	public StringBuffer printReturnType(int indent, StringBuffer output) {

		if (returnType == null) return output;
		return returnType.printExpression(0, output).append(' ');
	}

	public void resolveStatements() {

		// ========= abort on fatal error =============
		if (this.returnType != null && this.binding != null) {
			this.returnType.resolvedType = this.binding.returnType;
			// record the return type binding
		}
		// check if method with constructor name
		if (CharOperation.equals(scope.enclosingSourceType().sourceName, selector)) {
			scope.problemReporter().methodWithConstructorName(this);
		}
		
		// check @Override annotation
		if (this.binding != null) {
			int bindingModifiers = this.binding.modifiers;
			if ((this.binding.tagBits & TagBits.AnnotationOverride) != 0 
					&& (bindingModifiers & AccOverriding) == 0) {
				// claims to override, and doesn't actually do so
				scope.problemReporter().methodMustOverride(this);
			} else	if ((this.binding.tagBits & TagBits.AnnotationOverride) == 0 
						&& (bindingModifiers & AccStatic) == 0
						&& (bindingModifiers & AccOverriding) != 0
						&& scope.environment().options.sourceLevel >= JDK1_5) {
				// actually overrides, but did not claim to do so
				scope.problemReporter().missingOverrideAnnotation(this);
			}
		}
				
		// by grammatical construction, interface methods are always abstract
		switch (scope.referenceType().kind()) {
			case IGenericType.ENUM_DECL :
				if (this.selector == TypeConstants.VALUES) break;
				if (this.selector == TypeConstants.VALUEOF) break;
			case IGenericType.CLASS_DECL :
				// if a method has an semicolon body and is not declared as abstract==>error
				// native methods may have a semicolon body 
				if ((this.modifiers & AccSemicolonBody) != 0) {
					if ((this.modifiers & AccNative) == 0)
						if ((this.modifiers & AccAbstract) == 0)
							scope.problemReporter().methodNeedBody(this);
				} else {
					// the method HAS a body --> abstract native modifiers are forbiden
					if (((this.modifiers & AccNative) != 0) || ((this.modifiers & AccAbstract) != 0))
						scope.problemReporter().methodNeedingNoBody(this);
				}
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
			if (this.typeParameters != null) {
				int typeParametersLength = this.typeParameters.length;
				for (int i = 0; i < typeParametersLength; i++) {
					this.typeParameters[i].traverse(visitor, scope);
				}
			}			
			if (returnType != null)
				returnType.traverse(visitor, scope);
			if (arguments != null) {
				int argumentLength = arguments.length;
				for (int i = 0; i < argumentLength; i++)
					arguments[i].traverse(visitor, scope);
			}
			if (thrownExceptions != null) {
				int thrownExceptionsLength = thrownExceptions.length;
				for (int i = 0; i < thrownExceptionsLength; i++)
					thrownExceptions[i].traverse(visitor, scope);
			}
			if (statements != null) {
				int statementsLength = statements.length;
				for (int i = 0; i < statementsLength; i++)
					statements[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, classScope);
	}
	public TypeParameter[] typeParameters() {
	    return this.typeParameters;
	}		
}
