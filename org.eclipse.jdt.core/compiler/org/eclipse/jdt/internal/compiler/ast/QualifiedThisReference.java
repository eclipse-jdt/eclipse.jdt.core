/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class QualifiedThisReference extends ThisReference {
	
	public TypeReference qualification;
	ReferenceBinding currentCompatibleType;
	
	public QualifiedThisReference(TypeReference name, int sourceStart, int sourceEnd) {
		super(sourceStart, sourceEnd);
		qualification = name;
		this.sourceStart = name.sourceStart;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		return flowInfo;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo,
		boolean valueRequired) {

		return flowInfo;
	}

	/**
	 * Code generation for QualifiedThisReference
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
		if (valueRequired) {
			if ((bits & DepthMASK) != 0) {
				Object[] emulationPath =
					currentScope.getEmulationPath(this.currentCompatibleType, true /*only exact match*/, false/*consider enclosing arg*/);
				codeStream.generateOuterAccess(emulationPath, this, this.currentCompatibleType, currentScope);
			} else {
				// nothing particular after all
				codeStream.aload_0();
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public TypeBinding resolveType(BlockScope scope) {

		constant = NotAConstant;
		this.resolvedType = qualification.resolveType(scope);
		if (this.resolvedType == null) return null;

		// the qualification MUST exactly match some enclosing type name
		// Its possible to qualify 'this' by the name of the current class
		int depth = 0;
		this.currentCompatibleType = scope.referenceType().binding;
		while (this.currentCompatibleType != null
			&& this.currentCompatibleType != this.resolvedType) {
			depth++;
			this.currentCompatibleType = this.currentCompatibleType.isStatic() ? null : this.currentCompatibleType.enclosingType();
		}
		bits &= ~DepthMASK; // flush previous depth if any			
		bits |= (depth & 0xFF) << DepthSHIFT; // encoded depth into 8 bits

		if (this.currentCompatibleType == null) {
			scope.problemReporter().noSuchEnclosingInstance(this.resolvedType, this, false);
			return this.resolvedType;
		}

		// Ensure one cannot write code like: B() { super(B.this); }
		if (depth == 0) {
			checkAccess(scope.methodScope());
		} // if depth>0, path emulation will diagnose bad scenarii
		return this.resolvedType;
	}

	public String toStringExpression() {

		return qualification.toString(0) + ".this"; //$NON-NLS-1$
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			qualification.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}