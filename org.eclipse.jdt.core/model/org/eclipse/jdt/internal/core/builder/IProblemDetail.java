package org.eclipse.jdt.internal.core.builder;

public interface IProblemDetail {

	/** 
	 * Constant returned by getKind() indicating that the problem was
	 * encountered during compilation of the body of a method, constructor,
	 * field initializer or other initializer.
	 */
	static final int K_COMPILATION_PROBLEM = 1;

	/** 
	 * Constant returned by getKind() indicating that this object
	 * represents a problem with the principle structure.
	 */
	static final int K_PRINCIPLE_STRUCTURE_PROBLEM = 2;

	/**
	 * An integer bit mask indicating that the problem is an error.
	 * If this bit is set, the problem is an error, otherwise it is a warning.
	 */
	static final int S_ERROR = 1;

	/**
	 * Returns the character offset of the end of the problem in its compilation unit,
	 * or -1 if the end offset is not known.
	 */
	int getEndPos();
	/**
	 * Returns a constant indicating the type of problem returned by the compiler.
	 * This is one of the constants defined in com.ibm.compiler.java.problem.ProblemIrritants.
	 */
	int getID();
	/**
	 * Returns a constant indicating the kind of problem, either K_COMPILATION_PROBLEM or 
	 * K_PRINCIPLE_STRUCTURE_PROBLEM.
	 */
	int getKind();
	/**
	 * Returns the line number of the problem in its compilation unit,
	 * or -1 if the line number is not known.
	 */
	int getLineNumber();
	/**
	 * Returns a human-readable string describing the problem instance.
	 * The message is specific to the actual culprit object.
	 * The string is in the language specified by the current locale of the image builder.
	 */
	String getMessage();
	/**
	 * Returns an integer bit mask indicating the severity of the problem.
	 *
	 * @see S_ERROR
	 */
	int getSeverity();
	/**
	 * Returns a source fragment indicating the position in the source where the problem
	 * occurs.  The element ID of the source fragment refers to the element having the
	 * problem.
	 * The relevant string can be retrieved using:
	 * <code>
	 *    ISourceFragment fragment = problemDetail.getSourceFragment();
	 *    int start = getStartPos();
	 *    int end = getEndPos();
	 *    if(end>=start) {// handle problems with binaries and unknown source positions
	 *      String source = workspace.getElementContentString(fragment.getElementID());
	 *      String sub = source.substring(start, end + 1);
	 *      ...
	 * </code>
	 * If the positions within the source are unknown, the source fragment will 
	 * contain the most relevant element identifier but the start and end positions will be
	 * (0, -1). The same is true when the problem is with a binary type.
	 * Typically, positions are known only for problems of kind 
	 * <code>K_COMPILATION_PROBLEM</code>.
	 * <p>
	 * When this source fragment arises outside the context of a workspace
	 * (e.g, the Java source analyzer) where element identifiers make no sense,
	 * the element identifier is null.
	 */
	ISourceFragment getSourceFragment();
	/**
	 * Returns the character offset of the start of the problem in its compilation unit,
	 * or -1 if the start offset is not known.
	 */
	int getStartPos();
}
