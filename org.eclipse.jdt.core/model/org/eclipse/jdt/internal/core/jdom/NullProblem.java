package org.eclipse.jdt.internal.core.jdom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

/**
 * An implementation of IProblem which keeps track of the info given to it, but does
 * nothing more.  The need for this class is dependent upon the compilers ability to 
 * accept null <code>IProblemFactory</code>s and null <code>IProblem</code>s.
 */
public class NullProblem implements IProblem {
	/**
	 * The filename of the compilation unit which was being processed when this problem occured.
	 */
	char [] fFileName;

	/**
	 * The problem identification number of this problem.
	 */
	int fProblemId;

	/**
	 * Specific arguments relating to this problem.
	 */
	String[] fArguements;

	/**
	 * The severity of this problem
	 */
	int fSeverity;

	/**
	 * The source position corresponding to the beginning of the source element which caused
	 * this problem.
	 */
	int fStartPosition;

	/**
	 * The source position corresponding to the end of the source element which caused
	 * this problem.
	 */	
	int fEndPosition;

	/**
	 * The line number in the compilation unit which caused the problem
	 */
	int fLineNumber;
/**
 * Creates a NullProblem
 */
public NullProblem(char[] originatingFileName, int problemId, String[] arguments, int severity, int startPosition, int endPosition, int lineNumber) {
	fFileName = originatingFileName;
	fProblemId = problemId;
	fArguements = arguments;
	fSeverity = severity;
	fStartPosition = startPosition;
	fEndPosition = endPosition;
	fLineNumber = lineNumber;
}
/**
 * @see IProblem
 */
public String[] getArguments() {
	return fArguements;
}
/**
 * @see IProblem
 */
public int getID() {
	return fProblemId;
}
/**
 * @see IProblem
 */
public String getMessage() {
	return Integer.toString(fProblemId);
}
/**
 * @see IProblem
 */
public char[] getOriginatingFileName() {
	return fFileName;
}
/**
 * @see IProblem.
 */
public int getSeverity() {
	return fSeverity;
}
/**
 * @see IProblem
 */
public int getSourceEnd() {
	return fEndPosition;
}
/**
 * @see IProblem
 */
public int getSourceLineNumber() {
	return fLineNumber;
}
/**
 * @see IProblem
 */
public int getSourceStart() {
	return fStartPosition;
}
/**
 * @see IProblem
 */
public boolean isError() {
	return true;
}
/**
 * @see IProblem
 */
public boolean isWarning() {
	return false;
}
/**
 * @see IProblem
 */
public void setSourceEnd(int sourceEnd) {
	fEndPosition = sourceEnd;	
}
/**
 * @see IProblem
 */
public void setSourceLineNumber(int lineNumber) {
	fLineNumber = lineNumber;	
}
/**
 * @see IProblem
 */
public void setSourceStart(int sourceStart) {
	fStartPosition = sourceStart;	
}
}
