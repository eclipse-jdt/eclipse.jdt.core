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
package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;

/*
 * Compiler error handler, responsible to determine whether
 * a problem is actually a warning or an error; also will
 * decide whether the compilation task can be processed further or not.
 *
 * Behavior : will request its current policy if need to stop on
 *	first error, and if should proceed (persist) with problems.
 */

public class ProblemHandler {

	public final static String[] NoArgument = new String[0];
	
	final public IErrorHandlingPolicy policy;
	public final IProblemFactory problemFactory;
	public final CompilerOptions options;
/*
 * Problem handler can be supplied with a policy to specify
 * its behavior in error handling. Also see static methods for
 * built-in policies.
 *
 */
public ProblemHandler(IErrorHandlingPolicy policy, CompilerOptions options, IProblemFactory problemFactory) {
	this.policy = policy;
	this.problemFactory = problemFactory;
	this.options = options;
}
/*
 * Given the current configuration, answers which category the problem
 * falls into:
 *		Error | Warning | Ignore
 */
public int computeSeverity(int problemId){
	
	return ProblemSeverities.Error; // by default all problems are errors
}
public CategorizedProblem createProblem(
	char[] fileName, 
	int problemId, 
	String[] problemArguments, 
	String[] messageArguments,
	int severity, 
	int problemStartPosition, 
	int problemEndPosition, 
	int lineNumber,
	int columnNumber) {

	return this.problemFactory.createProblem(
		fileName, 
		problemId, 
		problemArguments, 
		messageArguments,
		severity, 
		problemStartPosition, 
		problemEndPosition, 
		lineNumber,
		columnNumber); 
}
public void handle(
	int problemId, 
	String[] problemArguments, 
	String[] messageArguments,
	int severity, 
	int problemStartPosition, 
	int problemEndPosition, 
	ReferenceContext referenceContext, 
	CompilationResult unitResult) {

	if (severity == ProblemSeverities.Ignore)
		return;

	// if no reference context, we need to abort from the current compilation process
	if (referenceContext == null) {
		if ((severity & ProblemSeverities.Error) != 0) { // non reportable error is fatal
			CategorizedProblem problem = this.createProblem(null, problemId, problemArguments, messageArguments, severity, 0, 0, 0, 0);			
			throw new AbortCompilation(null, problem);
		} else {
			return; // ignore non reportable warning
		}
	}

	int lineNumber = problemStartPosition >= 0
			? searchLineNumber(unitResult.getLineSeparatorPositions(), problemStartPosition)
			: 0;
	int columnNumber = problemStartPosition >= 0
			? searchColumnNumber(unitResult.getLineSeparatorPositions(), lineNumber, problemStartPosition)
			: 0;
	CategorizedProblem problem = 
		this.createProblem(
			unitResult.getFileName(), 
			problemId, 
			problemArguments, 
			messageArguments,
			severity, 
			problemStartPosition, 
			problemEndPosition,
			lineNumber,
			columnNumber);

	if (problem == null) return; // problem couldn't be created, ignore
	
	switch (severity & ProblemSeverities.Error) {
		case ProblemSeverities.Error :
			this.record(problem, unitResult, referenceContext);
			if ((severity & ProblemSeverities.Fatal) != 0) {
				referenceContext.tagAsHavingErrors();
				// should abort ?
				int abortLevel;
				if ((abortLevel = 	this.policy.stopOnFirstError() ? ProblemSeverities.AbortCompilation : severity & ProblemSeverities.Abort) != 0) {
					referenceContext.abort(abortLevel, problem);
				}
			}
			break;
		case ProblemSeverities.Warning :
			this.record(problem, unitResult, referenceContext);
			break;
	}
}
/**
 * Standard problem handling API, the actual severity (warning/error/ignore) is deducted
 * from the problem ID and the current compiler options.
 */
public void handle(
	int problemId, 
	String[] problemArguments, 
	String[] messageArguments,
	int problemStartPosition, 
	int problemEndPosition, 
	ReferenceContext referenceContext, 
	CompilationResult unitResult) {

	this.handle(
		problemId,
		problemArguments,
		messageArguments,
		this.computeSeverity(problemId), // severity inferred using the ID
		problemStartPosition,
		problemEndPosition,
		referenceContext,
		unitResult);
}
public void record(CategorizedProblem problem, CompilationResult unitResult, ReferenceContext referenceContext) {
	unitResult.record(problem, referenceContext);
}
/**
 * Search the line number corresponding to a specific position
 */
public static final int searchLineNumber(int[] startLineIndexes, int position) {
	if (startLineIndexes == null)
		return 1;
	int length = startLineIndexes.length;
	if (length == 0)
		return 1;
	int g = 0, d = length - 1;
	int m = 0, start;
	while (g <= d) {
		m = (g + d) /2;
		if (position < (start = startLineIndexes[m])) {
			d = m-1;
		} else if (position > start) {
			g = m+1;
		} else {
			return m + 1;
		}
	}
	if (position < startLineIndexes[m]) {
		return m+1;
	}
	return m+2;
}
public static final int searchColumnNumber(int[] startLineIndexes, int lineNumber, int position) {
	switch(lineNumber) {
		case 1 :
			return position + 1;
		case 2:
			return position - startLineIndexes[0];
		default:
			int line = lineNumber - 2;
    		int length = startLineIndexes.length;
    		if (line >= length) {
    			return position - startLineIndexes[length - 1];
    		}
    		return position - startLineIndexes[line];
	}
}
}
