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
package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;

/*
 * Special unchecked exception type used 
 * to abort from the compilation process
 *
 * should only be thrown from within problem handlers.
 */
public class AbortCompilation extends RuntimeException {

	public CompilationResult compilationResult;
	public Throwable exception;
	public IProblem problem;
	
	/* special fields used to abort silently (e.g. when cancelling build process) */
	public boolean isSilent;
	public RuntimeException silentException;

	private static final long serialVersionUID = -2047226595083244852L; // backward compatible
	
	public AbortCompilation() {
		// empty
	}

	public AbortCompilation(CompilationResult compilationResult, IProblem problem) {
		this();
		this.compilationResult = compilationResult;
		this.problem = problem;
	}

	public AbortCompilation(CompilationResult compilationResult, Throwable exception) {
		this();
		this.compilationResult = compilationResult;
		this.exception = exception;
	}

	public AbortCompilation(boolean isSilent, RuntimeException silentException) {
		this();
		this.isSilent = isSilent;
		this.silentException = silentException;
	}
	
	public void updateContext(InvocationSite invocationSite, CompilationResult unitResult) {
		if (this.problem == null) return;
		if (this.problem.getSourceStart() != 0 || this.problem.getSourceEnd() != 0) return;
		this.problem.setSourceStart(invocationSite.sourceStart());
		this.problem.setSourceEnd(invocationSite.sourceEnd());
		this.problem.setSourceLineNumber(ProblemHandler.searchLineNumber(unitResult.lineSeparatorPositions, invocationSite.sourceStart()));
		this.compilationResult = unitResult;
	}

	public void updateContext(ASTNode astNode, CompilationResult unitResult) {
		if (this.problem == null) return;
		if (this.problem.getSourceStart() != 0 || this.problem.getSourceEnd() != 0) return;
		this.problem.setSourceStart(astNode.sourceStart());
		this.problem.setSourceEnd(astNode.sourceEnd());
		this.problem.setSourceLineNumber(ProblemHandler.searchLineNumber(unitResult.lineSeparatorPositions, astNode.sourceStart()));
		this.compilationResult = unitResult;
	}
}
