/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.util.Util;

public class DefaultProblem implements ProblemSeverities, IProblem {
	
	private static final String LINE_DELIMITER = System.getProperty("line.separator"); //$NON-NLS-1$
		
	private char[] fileName;
	private int id;
	private int startPosition, endPosition, line;
	private int severity;
	private String[] arguments;
	private String message;
	
	public DefaultProblem(
		char[] originatingFileName,
		String message,
		int id,
		String[] stringArguments,
		int severity,
		int startPosition,
		int endPosition,
		int line) {

		this.fileName = originatingFileName;
		this.message = message;
		this.id = id;
		this.arguments = stringArguments;
		this.severity = severity;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.line = line;
	}
	public String errorReportSource(char[] unitSource) {
		//extra from the source the innacurate     token
		//and "highlight" it using some underneath ^^^^^
		//put some context around too.

		//this code assumes that the font used in the console is fixed size

		//sanity .....
		if ((startPosition > endPosition)
			|| ((startPosition < 0) && (endPosition < 0)))
			return Util.bind("problem.noSourceInformation"); //$NON-NLS-1$

		StringBuffer errorBuffer = new StringBuffer(" "); //$NON-NLS-1$
		errorBuffer.append(Util.bind("problem.atLine", String.valueOf(line))); //$NON-NLS-1$
		errorBuffer.append(LINE_DELIMITER).append("\t"); //$NON-NLS-1$
		
		char c;
		final char SPACE = '\u0020';
		final char MARK = '^';
		final char TAB = '\t';
		//the next code tries to underline the token.....
		//it assumes (for a good display) that token source does not
		//contain any \r \n. This is false on statements ! 
		//(the code still works but the display is not optimal !)

		// expand to line limits
		int length = unitSource.length, begin, end;
		for (begin = startPosition >= length ? length - 1 : startPosition; begin > 0; begin--) {
			if ((c = unitSource[begin - 1]) == '\n' || c == '\r') break;
		}
		for (end = endPosition >= length ? length - 1 : endPosition ; end+1 < length; end++) {
			if ((c = unitSource[end + 1]) == '\r' || c == '\n') break;
		}
		
		// trim left and right spaces/tabs
		while ((c = unitSource[begin]) == ' ' || c == '\t') begin++;
		//while ((c = unitSource[end]) == ' ' || c == '\t') end--; TODO (philippe) should also trim right, but all tests are to be updated
		
		// copy source
		errorBuffer.append(unitSource, begin, end-begin+1);
		errorBuffer.append(LINE_DELIMITER).append("\t"); //$NON-NLS-1$

		// compute underline
		for (int i = begin; i <startPosition; i++) {
			errorBuffer.append((unitSource[i] == TAB) ? TAB : SPACE);
		}
		for (int i = startPosition; i <= (endPosition >= length ? length - 1 : endPosition); i++) {
			errorBuffer.append(MARK);
		}
		return errorBuffer.toString();
	}

	/**
	 * Answer back the original arguments recorded into the problem.
	 * @return java.lang.String[]
	 */
	public String[] getArguments() {

		return arguments;
	}

	/**
	 * Answer the type of problem.
	 * @see org.eclipse.jdt.core.compiler.IProblem#getID()
	 * @return int
	 */
	public int getID() {

		return id;
	}

	/**
	 * Answer a localized, human-readable message string which describes the problem.
	 * @return java.lang.String
	 */
	public String getMessage() {

		return message;
	}

	/**
	 * Answer the file name in which the problem was found.
	 * @return char[]
	 */
	public char[] getOriginatingFileName() {

		return fileName;
	}

	/**
	 * Answer the end position of the problem (inclusive), or -1 if unknown.
	 * @return int
	 */
	public int getSourceEnd() {

		return endPosition;
	}

	/**
	 * Answer the line number in source where the problem begins.
	 * @return int
	 */
	public int getSourceLineNumber() {

		return line;
	}

	/**
	 * Answer the start position of the problem (inclusive), or -1 if unknown.
	 * @return int
	 */
	public int getSourceStart() {

		return startPosition;
	}

	/*
	 * Helper method: checks the severity to see if the Error bit is set.
	 * @return boolean
	 */
	public boolean isError() {

		return (severity & ProblemSeverities.Error) != 0;
	}

	/*
	 * Helper method: checks the severity to see if the Error bit is not set.
	 * @return boolean
	 */
	public boolean isWarning() {

		return (severity & ProblemSeverities.Error) == 0;
	}

	/**
	 * Set the end position of the problem (inclusive), or -1 if unknown.
	 *
	 * Used for shifting problem positions.
	 * @param sourceEnd the new value of the sourceEnd of the receiver
	 */
	public void setSourceEnd(int sourceEnd) {

		endPosition = sourceEnd;
	}

	/**
	 * Set the line number in source where the problem begins.
	 * @param lineNumber the new value of the line number of the receiver
	 */
	public void setSourceLineNumber(int lineNumber) {

		line = lineNumber;
	}

	/**
	 * Set the start position of the problem (inclusive), or -1 if unknown.
	 *
	 * Used for shifting problem positions.
	 * @param sourceStart the new value of the source start position of the receiver
	 */
	public void setSourceStart(int sourceStart) {

		startPosition = sourceStart;
	}

	public String toString() {

		String s = "Pb(" + (id & IgnoreCategoriesMask) + ") "; //$NON-NLS-1$ //$NON-NLS-2$
		if (message != null) {
			s += message;
		} else {
			if (arguments != null)
				for (int i = 0; i < arguments.length; i++)
					s += " " + arguments[i]; //$NON-NLS-1$
		}
		return s;
	}
}
