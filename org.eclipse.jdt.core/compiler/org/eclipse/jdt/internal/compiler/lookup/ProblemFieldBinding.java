package org.eclipse.jdt.internal.compiler.lookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.util.*;

public class ProblemFieldBinding extends FieldBinding {
	private int problemId;
	// NOTE: must only answer the subset of the name related to the problem

	public ProblemFieldBinding(char[][] compoundName, int problemId) {
		this(CharOperation.concatWith(compoundName, '.'), problemId);
	}

	public ProblemFieldBinding(char[] name, int problemId) {
		this.name = name;
		this.problemId = problemId;
	}

	/* API
	* Answer the problem id associated with the receiver.
	* NoError if the receiver is a valid binding.
	*/

	public final int problemId() {
		return problemId;
	}

}
