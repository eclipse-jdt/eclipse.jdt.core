package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.*;

import java.io.*;

import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;

public class OrPattern extends SearchPattern {

	public SearchPattern leftPattern;
	public SearchPattern rightPattern;
public OrPattern(SearchPattern leftPattern, SearchPattern rightPattern) {
	super(-1, false); // values ignored for a OrPattern
		
	this.leftPattern = leftPattern;
	this.rightPattern = rightPattern;

	this.needsResolve = leftPattern.needsResolve || rightPattern.needsResolve;
}
/**
 * see SearchPattern.decodedIndexEntry
 */
protected void decodeIndexEntry(IEntryResult entry) {

	// will never be directly invoked on a composite pattern
}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope)  throws IOException {
	// will never be directly invoked on a composite pattern
}
/**
 * see SearchPattern.findMatches
 */
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	IIndexSearchRequestor orCombiner;
	if (detailLevel == IInfoConstants.NameInfo) {
		orCombiner = new OrNameCombiner(requestor);
	} else {
		orCombiner = new OrPathCombiner(requestor);
	}
	leftPattern.findIndexMatches(input, orCombiner, detailLevel, progressMonitor, scope);
	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();
	rightPattern.findIndexMatches(input, orCombiner, detailLevel, progressMonitor, scope);
}
/**
 * see SearchPattern.indexEntryPrefix
 */
public char[] indexEntryPrefix() {

	// will never be directly invoked on a composite pattern
	return null;
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return leftPattern.matchContainer()
			| rightPattern.matchContainer();
}
/**
 * see SearchPattern.matchIndexEntry
 */
protected boolean matchIndexEntry() {

	return leftPattern.matchIndexEntry()
			|| rightPattern.matchIndexEntry();
}
/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	int leftLevel = this.leftPattern.matchLevel(reference, true);
	if (leftLevel == ACCURATE_MATCH || leftLevel == INACCURATE_MATCH) {
		this.leftPattern.matchReportReference(reference, element, accuracy, locator);
	} else {
		this.rightPattern.matchReportReference(reference, element, accuracy, locator);
	}
}
public String toString(){
	return this.leftPattern.toString() + "\n| "/*nonNLS*/ + this.rightPattern.toString();
}

/**
 * see SearchPattern.initializeFromLookupEnvironment
 */
public boolean initializeFromLookupEnvironment(LookupEnvironment env) {

	// need to perform both operand initialization due to side-effects.
	boolean leftInit = this.leftPattern.initializeFromLookupEnvironment(env);
	boolean rightInit = this.rightPattern.initializeFromLookupEnvironment(env);
	return leftInit || rightInit;
}

/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	switch (this.leftPattern.matchLevel(node, resolve)) {
		case IMPOSSIBLE_MATCH:
			return this.rightPattern.matchLevel(node, resolve);
		case POSSIBLE_MATCH:
			return POSSIBLE_MATCH;
		case INACCURATE_MATCH:
			int rightLevel = this.rightPattern.matchLevel(node, resolve);
			if (rightLevel != IMPOSSIBLE_MATCH) {
				return rightLevel;
			} else {
				return INACCURATE_MATCH;
			}
		case ACCURATE_MATCH:
			return ACCURATE_MATCH;
		default:
			return IMPOSSIBLE_MATCH;
	}
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	switch (this.leftPattern.matchLevel(binding)) {
		case IMPOSSIBLE_MATCH:
			return this.rightPattern.matchLevel(binding);
		case POSSIBLE_MATCH:
			return POSSIBLE_MATCH;
		case INACCURATE_MATCH:
			int rightLevel = this.rightPattern.matchLevel(binding);
			if (rightLevel != IMPOSSIBLE_MATCH) {
				return rightLevel;
			} else {
				return INACCURATE_MATCH;
			}
		case ACCURATE_MATCH:
			return ACCURATE_MATCH;
		default:
			return IMPOSSIBLE_MATCH;
	}
}
}
