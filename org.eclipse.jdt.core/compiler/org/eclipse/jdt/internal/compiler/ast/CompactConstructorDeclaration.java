/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class CompactConstructorDeclaration extends ConstructorDeclaration {

	public boolean isImplicit;
	public RecordDeclaration recordDeclaration;
	
	public CompactConstructorDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}
	@Override
	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		if (this.isImplicit && this.constructorCall == null) {
			this.constructorCall = SuperReference.implicitSuperConstructorCall();
			this.constructorCall.sourceStart = this.sourceStart;
			this.constructorCall.sourceEnd = this.sourceEnd;
			return;
		}
		parser.parse(this, unit, false);
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public boolean visit(ExplicitConstructorCall explicitConstructorCall, BlockScope skope) {
				if (explicitConstructorCall.accessMode != ExplicitConstructorCall.ImplicitSuper)
					skope.problemReporter().recordCompactConstructorHasExplicitConstructorCall(explicitConstructorCall);
				return false;
			}
			@Override
			public boolean visit(MethodDeclaration methodDeclaration, ClassScope skope) {
				return false;
			}
			@Override
			public boolean visit(LambdaExpression lambda, BlockScope skope) {
				return false;
			}
			@Override
			public boolean visit(ReturnStatement returnStatement, BlockScope skope) {
				parser.problemReporter().recordCompactConstructorHasReturnStatement(returnStatement);
				return false;
			}
		};
		if (!this.isImplicit)
			unit.traverse(visitor, unit.scope);
	}
	@Override
	protected boolean generateFieldAssignment(FieldBinding field, int i) {
		// TODO : Add Code for missing field assignments
		/* JLS 14 8.10.5 Compact Record Constructor Declarations
		 * In addition, at the end of the body of the compact constructor, all the fields
		 * corresponding to the record components of R that are definitely unassigned
		 * (16 (Definite Assignment)) are implicitly initialized to the value of the
		 * corresponding formal parameter. These fields are implicitly initialized in the
		 * order that they are declared in the record component list.
		 */
		/*
		FieldReference lhs = new FieldReference(field.name, 0);
		lhs.receiver = ThisReference.implicitThis();
		lhs.actualReceiverType = this.recordDeclaration.binding;
		lhs.binding = field;
		lhs.resolvedType = field.type;
		//TODO: set definitelyAssigned to true;
		//TODO: Check anything to be done for null analysis.
		SingleNameReference rhs = new SingleNameReference(field.name, 0);
		rhs.actualReceiverType = this.recordDeclaration.binding;
 
		rhs.resolvedType = field.type;
		Assignment assignment = new Assignment(lhs, rhs, 0);
		Statement[] stmts = this.statements;
		if (this.statements == null) {
			this.statements = new Statement[] { assignment };
		} else {
			int len = this.statements.length;
			System.arraycopy(
					this.statements,
					0,
					stmts = new Statement[len + 1],
					0,
					len);
			stmts[len] = assignment;
			this.statements = stmts;
		}*/
		return true; // TODO: Enable the code above during flow analysis. 
	}
}