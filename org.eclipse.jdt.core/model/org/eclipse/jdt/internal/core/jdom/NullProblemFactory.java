package org.eclipse.jdt.internal.core.jdom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

import java.util.*;

/**
 * Implements the <code>IProblemFactory</code> interface to provide <code>NullProblem</code>s.
 * The need for this class is dependent upon the compilers ability to accept null 
 * <code>IProblemFactory</code>s and null <code>IProblem</code>s. 
 */
public class NullProblemFactory implements IProblemFactory {
	/**
	 * Returns a new <code>NullProblem</code> initialized to the given info.
	 */
	public IProblem createProblem(
		char[] originatingFileName,
		int problemId,
		String[] arguments,
		int severity,
		int startPosition,
		int endPosition,
		int lineNumber) {
		return new NullProblem(
			originatingFileName,
			problemId,
			arguments,
			severity,
			startPosition,
			endPosition,
			lineNumber);
	}

	/**
	 * Returns the default locale
	 */
	public Locale getLocale() {
		return Locale.getDefault();
	}

	/**
	 * Answers a String representation of the problemId.
	 */
	public String getLocalizedMessage(int problemId, String[] problemArguments) {
		return Integer.toString(problemId);
	}

}
