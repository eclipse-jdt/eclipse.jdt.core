package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.internal.compiler.IProblem;
import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.core.Util;

/**
 * @see IProblemDetail
 */
public class ProblemDetailImpl implements IProblemDetail, IProblem {
	protected SourceEntry fSourceEntry;
	protected String fMessage;
	protected int fStartPos, fEndPos, fLineNumber;
	protected int fSeverity;

	/**
	 * The ID of the problem returned by the compiler.
	 * @see com.ibm.compiler.java.problem.ProblemIrritants
	 */
	protected int fID;

	/**
	 * Severity flag indicating a syntax error (also covers namespace errors such as duplicates).
	 */
	protected static final int S_SYNTAX_ERROR = 2;
/**
 * Creates a problem detail.
 */
public ProblemDetailImpl(String msg, int id, int severity, SourceEntry sourceEntry, int startPos, int endPos, int lineNumber) {
	fMessage = msg;
	fID = id;
	fSeverity = severity;
	fSourceEntry = sourceEntry;
	fStartPos = startPos;
	fEndPos = endPos;
	fLineNumber = lineNumber;
}
/**
 * Creates a problem detail.
 */
public ProblemDetailImpl(String msg, SourceEntry sourceEntry) {
	this(msg, 0, S_ERROR, sourceEntry, -1, -1, -1);
}
public boolean equals(Object o) {
	if (this == o) return true;
	if (!(this instanceof ProblemDetailImpl)) return false;
	return equals((ProblemDetailImpl) o, false);
}
public boolean equals(ProblemDetailImpl pb, boolean ignorePositions) {
	return
		fMessage.equals(pb.fMessage)
			&& Util.equalOrNull(fSourceEntry, pb.fSourceEntry)
			&& fSeverity == pb.fSeverity
			&& (ignorePositions || 
				(fStartPos == pb.fStartPos && fEndPos == pb.fEndPos));
}
/**
 * @see IProblem
 */
public String[] getArguments() {
	return null; // not kept
}
/**
 * Returns the end pos.
 */
public int getEndPos() {
	return fEndPos;
}
/**
 * @see IProblemDetail
 */
public int getID() {
	return fID;
}
/**
 * @see IProblemDetail
 */
public int getKind() {
	return IProblemDetail.K_COMPILATION_PROBLEM;
}
/**
 * @see IProblemDetail
 */
public int getLineNumber() {
	return fLineNumber;
}
/**
 * @see IProblemDetail
 */
public String getMessage() {
	return fMessage;
}
/**
 * getOriginatingFileName method comment.
 */
public char[] getOriginatingFileName() {
	return fSourceEntry.getPathWithZipEntryName().toCharArray();
}
/**
 * Returns the path of the source entry.
 */
IPath getPath() {
	return fSourceEntry == null ? null : fSourceEntry.getPath();
}
/**
 * @see IProblemDetail
 */
public int getSeverity() {
	return fSeverity;
}
/**
 * @see IProblem
 */
public int getSourceEnd() {
	return fEndPos;
}
/**
 * Returns the source entry
 */
SourceEntry getSourceEntry() {
	return fSourceEntry;
}
/**
 * @see ICompilationProblem
 */
public ISourceFragment getSourceFragment() {
	if (fSourceEntry == null) {
		return null;
	}
	return new SourceFragmentImpl(fStartPos, fEndPos, fSourceEntry);
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
	return fStartPos;
}
/**
 * Returns the start pos.
 */
public int getStartPos() {
	return fStartPos;
}
public int hashCode() {
	return fMessage.hashCode() * 17 + (fSourceEntry == null ? 0 : fSourceEntry.hashCode());
}
/**
 * @see IProblem
 */
public boolean isError() {
	return (fSeverity & S_ERROR) != 0;
}
/**
 * @see IProblem
 */
public boolean isWarning() {
	return (fSeverity & S_ERROR) == 0;
}
/**
 * @see IProblem
 */
public void setSourceEnd(int sourceEnd) {
	fEndPos = sourceEnd;
}
/**
 * Internal - Set the source entry.
 */
public void setSourceEntry(SourceEntry sourceEntry) {
	fSourceEntry = sourceEntry;
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
	fStartPos = sourceStart;
}
/**
 * Returns a readable representation of the class.  This method is for debugging
 * purposes only. Non-NLS.
 */
public String toString() {
	return "ProblemDetail(" + getMessage() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
}
}
