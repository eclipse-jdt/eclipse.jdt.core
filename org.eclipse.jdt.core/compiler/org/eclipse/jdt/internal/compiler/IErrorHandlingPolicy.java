package org.eclipse.jdt.internal.compiler;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.problem.ProblemHandler;

/*
 * Handler policy is responsible to answer the 2 following
 * questions:
 * 1. should the handler stop on first problem which appears
 *	to be a real error (i.e. not a warning),
 * 2. should it proceed once it has gathered all problems
 *
 * The intent is that one can supply its own policy to implement 
 * some interactive error handling strategy where some UI would 
 * display problems and ask user if he wants to proceed or not.
 */

public interface IErrorHandlingPolicy {
	boolean proceedOnErrors();
	boolean stopOnFirstError();
}
