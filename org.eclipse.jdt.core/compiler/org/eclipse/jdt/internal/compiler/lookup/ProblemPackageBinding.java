package org.eclipse.jdt.internal.compiler.lookup;

public class ProblemPackageBinding extends PackageBinding {
	private int problemId;
// NOTE: must only answer the subset of the name related to the problem

ProblemPackageBinding(char[][] compoundName, int problemId) {
	this.compoundName = compoundName;
	this.problemId = problemId;
}
ProblemPackageBinding(char[] name, int problemId) {
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
