package org.eclipse.jdt.internal.compiler.lookup;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public class ProblemReferenceBinding extends ReferenceBinding {
	private int problemId;
// NOTE: must only answer the subset of the name related to the problem

public ProblemReferenceBinding(char[][] compoundName, int problemId) {
	this.compoundName = compoundName;
	this.problemId = problemId;
}
public ProblemReferenceBinding(char[] name, int problemId) {
	this(new char[][] {name}, problemId);
}
/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/

public final int problemId() {
	return problemId;
}
}
