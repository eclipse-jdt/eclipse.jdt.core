package org.eclipse.jdt.internal.compiler.lookup;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public class ProblemMethodBinding extends MethodBinding {
	private int problemId;
	public MethodBinding closestMatch;
public ProblemMethodBinding(char[] selector, TypeBinding[] args, int problemId) {
	this.selector = selector;
	this.parameters = (args == null || args.length == 0) ? NoParameters : args;
	this.problemId = problemId;
}
public ProblemMethodBinding(char[] selector, TypeBinding[] args, ReferenceBinding declaringClass, int problemId) {
	this.selector = selector;
	this.parameters = (args == null || args.length == 0) ? NoParameters : args;
	this.declaringClass = declaringClass;
	this.problemId = problemId;
}
public ProblemMethodBinding(MethodBinding closestMatch, char[] selector, TypeBinding[] args, int problemId) {
	this(selector, args, problemId);
	this.closestMatch = closestMatch;
}
/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/

public final int problemId() {
	return problemId;
}
}
