package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.*;

import java.io.*;

public abstract class MultipleSearchPattern extends AndPattern {

	protected char[] currentTag;
	public boolean foundAmbiguousIndexMatches = false;	
public MultipleSearchPattern(int matchMode, boolean isCaseSensitive) {
	super(matchMode, isCaseSensitive);
}
/**
 * Query a given index for matching entries. 
 */
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {

	char[][] possibleTags = getPossibleTags();
	
	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	/* narrow down a set of entries using prefix criteria */
	for (int i = 0, max = possibleTags.length; i < max; i++){
		currentTag = possibleTags[i];
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
}
protected abstract char[][] getPossibleTags();
}
