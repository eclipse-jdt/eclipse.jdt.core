package org.eclipse.jdt.internal.compiler;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.problem.ProblemHandler;

/*
 * Handler policy is responsible to answer the 2 following
 * questions:
 * 1. should the handler stop on first problem which appears
 *	to be a real error (i.e. not a warning),
 * 2. should it proceed once it has gathered all problems
 *
 * The intent is that one can supply its own policy to implement 
 * some interactive error handling strategy where some UI would 
 * display problems and ask user if he wants to proceed or not.
 */

public interface IProblem {
	final int Error = 1; // when bit is set: problem is error, if not it is a warning
/**
 * Answer back the original arguments recorded into the problem.
 */
String[] getArguments();
/**
 * 
 * @return int
 */
int getID();
/**
 * Answer a localized, human-readable message string which describes the problem.
 */
String getMessage();
/**
 * Answer the file name in which the problem was found.
 */
char[] getOriginatingFileName();
/**
 * Answer the severity of the problem.
 */
int getSeverity();
/**
 * Answer the end position of the problem (inclusive), or -1 if unknown.
 */
int getSourceEnd();
/**
 * Answer the line number in source where the problem begins.
 */
int getSourceLineNumber();
/**
 * Answer the start position of the problem (inclusive), or -1 if unknown.
 */
int getSourceStart();
/*
 * Helper method: checks the severity to see if the Error bit is set.
 */
boolean isError();
/*
 * Helper method: checks the severity to see if the Error bit is not set.
 */
boolean isWarning();
/**
 * Set the end position of the problem (inclusive), or -1 if unknown.
 *
 * Used for shifting problem positions.
 */
void setSourceEnd(int sourceEnd);
/**
 * Set the line number in source where the problem begins.
 */
void setSourceLineNumber(int lineNumber);
/**
 * Set the start position of the problem (inclusive), or -1 if unknown.
 *
 * Used for shifting problem positions.
 */
void setSourceStart(int sourceStart);
}
