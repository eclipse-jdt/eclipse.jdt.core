package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.impl.*;

public class Problem implements IProblem, ProblemSeverities, ProblemIrritants {
	private char[] fileName;
	private int id;
	private int startPosition, endPosition,line;
	private int severity;
	private String[] arguments; 
	private String message;
public Problem(
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
/**
 * Answer back the original arguments recorded into the problem.
 * @return java.lang.String[]
 */
public String[] getArguments() {
	return arguments;
}
/**
 * Answer the type of problem.
 * @see com.ibm.compiler.java.problem.ProblemIrritants
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
 * Answer the severity of the problem.
 * @return int
 */
public int getSeverity() {
	return severity;
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

	String s = "Pb("/*nonNLS*/ + (id & IgnoreCategoriesMask) + ") "/*nonNLS*/;
	if (message != null) {
		s += message;
	} else {
		if (arguments != null)
			for (int i = 0; i < arguments.length; i++)
				s += " "/*nonNLS*/ + arguments[i];
	}
	return s;
}
}
