package org.eclipse.jdt.internal.core.builder;

public class BuildEvent {
	protected ISourceFragment fSourceFragment;
	protected int fNewErrorCount;
	protected int fFixedErrorCount;
	protected int fNewWarningCount;
	protected int fFixedWarningCount;
	/**
	 * Internal - Constructs a new build event object.
	 */
	public BuildEvent(
		ISourceFragment sourceFragment, 
		int newErrorCount, 
		int fixedErrorCount, 
		int newWarningCount, 
		int fixedWarningCount) {
			
		fSourceFragment = sourceFragment;
		fNewErrorCount = newErrorCount;
		fFixedErrorCount = fixedErrorCount;
		fNewWarningCount = newWarningCount;
		fFixedWarningCount = fixedWarningCount;
	}
	/**
	 * Returns the number of errors fixed since
	 * the beginning of the build.
	 */
	public int getFixedErrorCount() {
		return fFixedErrorCount;
	}
	/**
	 * Returns the number of warnings fixed since
	 * the beginning of the build.
	 */
	public int getFixedWarningCount() {
		return fFixedWarningCount;
	}
	/**
	 * Returns the number of new errors found since
	 * the beginning of the build.
	 */
	public int getNewErrorCount() {
		return fNewErrorCount;
	}
	/**
	 * Returns the number of new warnings found since
	 * the beginning of the build.
	 */
	public int getNewWarningCount() {
		return fNewWarningCount;
	}
	/**
	 * Returns the source fragment of the most recently
	 * (re)compiled element.  May be null if the notification
	 * is not due to an element being (re)compiled, for example
	 * when an element is removed.
	 */
	public ISourceFragment getSourceFragment() {
		return fSourceFragment;
	}
}
