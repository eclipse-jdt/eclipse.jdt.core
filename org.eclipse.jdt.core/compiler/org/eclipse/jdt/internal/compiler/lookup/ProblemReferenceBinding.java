package org.eclipse.jdt.internal.compiler.lookup;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class ProblemReferenceBinding extends ReferenceBinding {
	public Binding original;
	private int problemId;
// NOTE: must only answer the subset of the name related to the problem

public ProblemReferenceBinding(char[][] compoundName, int problemId) {
	this(compoundName, null, problemId);
}
public ProblemReferenceBinding(char[] name, int problemId) {
	this(new char[][] {name}, null, problemId);
}

public ProblemReferenceBinding(char[][] compoundName, Binding original, int problemId) {
	this.compoundName = compoundName;
	this.original = original;
	this.problemId = problemId;
}
public ProblemReferenceBinding(char[] name, Binding original, int problemId) {
	this(new char[][] {name}, original, problemId);
}
/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/

public final int problemId() {
	return problemId;
}

public char[] readableName() /*java.lang.Object*/ {
	if(original != null)
		return original.readableName();
	
	return CharOperation.concatWith(compoundName, '.');
}
}
