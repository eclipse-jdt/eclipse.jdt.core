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

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class CompactConstructorDeclaration extends ConstructorDeclaration {

	public boolean isImplicit;
	
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
		return true; //temporary workaround for grammar part - to be addressed in flow analysis
//		return false; // enable this once flow analysis done
	}
}