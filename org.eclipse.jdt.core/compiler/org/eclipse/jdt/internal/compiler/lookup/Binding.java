package org.eclipse.jdt.internal.compiler.lookup;

public abstract class Binding
	implements BindingIds, CompilerModifiers, ProblemReasons {
	/* API
	* Answer the receiver's binding type from Binding.BindingID.
	*
	* Note: Do NOT expect this to be used very often... only in switch statements with
	* more than 2 possible choices.
	*/

	public abstract int bindingType();
	/* API
	* Answer true if the receiver is not a problem binding
	*/

	public final boolean isValidBinding() {
		return problemId() == NoError;
	}

	/* API
	* Answer the problem id associated with the receiver.
	* NoError if the receiver is a valid binding.
	*/

	public int problemId() {
		return NoError;
	}

	/* Answer a printable representation of the receiver.
	*/

	public abstract char[] readableName();
}
