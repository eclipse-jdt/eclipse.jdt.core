package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.core.builder.*;

import java.io.IOException;

/**
 * This is the representation of a compilation unit which
 * is passed to the compiler.  It remembers the contents of the unit
 * once read, since the compiler may request it multiple times
 * (it currently asks for it twice: once to get the structure, once
 * when actually compiling methds).  The compiler actually clears out the entries in
 * the array of ICompilationUnit passed to the compiler, so the
 * source is only hung onto while needed.
 */
public class CompilerCompilationUnit implements ICompilationUnit {
	protected StateImpl fState;
	protected SourceEntry fSourceEntry;
	protected BuildNotifier fNotifier;
	protected char[] fContents;
/**
 * Creates a new compilation unit for the given source entry.
 */
public CompilerCompilationUnit(StateImpl state, SourceEntry sourceEntry, BuildNotifier notifier) {
	fState = state;
	fSourceEntry = sourceEntry;
	fNotifier = notifier;
}
/**
 * @see ICompilationUnit
 * See the discussion of remembering contents in the class comment.
 */
public char[] getContents() {
	if (fContents == null) {
		if (fNotifier != null) {
			fNotifier.compiling(this);
		}
		fContents = fState.getElementContentCharArray(fSourceEntry);
	}
	return fContents;
}
/**
 * @see ICompilationUnit
 */
public char[] getFileName() {
	return fSourceEntry.getPathWithZipEntryName().toCharArray();
}
/**
 * @see ICompilationUnit
 */
public char[] getMainTypeName() {
	return fSourceEntry.getName().toCharArray();
}
/**
 * Returns the source entry
 */
public SourceEntry getSourceEntry() {
	return fSourceEntry;
}
	public String toString() {
		// don't use append(char[]) due to JDK1.2 problems
		return new StringBuffer("CompilationUnit(").append(getFileName()).append(")").toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
