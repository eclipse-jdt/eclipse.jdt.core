package org.eclipse.jdt.internal.core.search.indexing;

import org.eclipse.jdt.internal.core.search.processing.IJob;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
public abstract class IndexRequest implements IJob {
	
	/* The time the resource which is about to be indexed was last modified */
	public long timeStamp = -1;

}
