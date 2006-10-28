/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ClassLiteralAccess extends Expression {
	
	public TypeReference type;
	public TypeBinding targetType;
	FieldBinding syntheticField;

	public ClassLiteralAccess(int sourceEnd, TypeReference type) {
		this.type = type;
		type.bits |= IgnoreRawTypeCheck; // no need to worry about raw type usage
		this.sourceStart = type.sourceStart;
		this.sourceEnd = sourceEnd;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// if reachable, request the addition of a synthetic field for caching the class descriptor
		SourceTypeBinding sourceType =
			currentScope.outerMostMethodScope().enclosingSourceType();
		if ((!(sourceType.isInterface()
				// no field generated in interface case (would'nt verify) see 1FHHEZL
				|| sourceType.isBaseType()))
				&& currentScope.compilerOptions().sourceLevel < ClassFileConstants.JDK1_5) {
			syntheticField = sourceType.addSyntheticFieldForClassLiteral(targetType, currentScope);
		}
		return flowInfo;
	}

	/**
	 * MessageSendDotClass code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {
		int pc = codeStream.position;

		// in interface case, no caching occurs, since cannot make a cache field for interface
		if (valueRequired) {
			codeStream.generateClassLiteralAccessForType(type.resolvedType, syntheticField);
			codeStream.generateImplicitConversion(this.implicitConversion);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		return type.print(0, output).append(".class"); //$NON-NLS-1$
	}

	public TypeBinding resolveType(BlockScope scope) {

		constant = Constant.NotAConstant;
		if ((targetType = type.resolveType(scope, true /* check bounds*/)) == null)
			return null;

		if (targetType.isArrayType()
			&& ((ArrayBinding) targetType).leafComponentType == TypeBinding.VOID) {
			scope.problemReporter().cannotAllocateVoidArray(this);
			return null;
		} else if (targetType.isTypeVariable()) {
			scope.problemReporter().illegalClassLiteralForTypeVariable((TypeVariableBinding)targetType, this);
		}
		ReferenceBinding classType = scope.getJavaLangClass();
		if (classType.isGenericType()) {
		    // Integer.class --> Class<Integer>, perform boxing of base types (int.class --> Class<Integer>)
			TypeBinding boxedType = null;
			if (targetType.id == T_void) {
				boxedType = scope.environment().getType(JAVA_LANG_VOID);
				if (boxedType == null) {
					boxedType = new ProblemReferenceBinding(JAVA_LANG_VOID, null, ProblemReasons.NotFound);
				}
			} else {
				boxedType = scope.boxing(targetType);
			}
		    this.resolvedType = scope.environment().createParameterizedType(classType, new TypeBinding[]{ boxedType }, null/*not a member*/);
		} else {
		    this.resolvedType = classType;
		}
		return this.resolvedType;
	}

	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			type.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}
