package org.eclipse.jdt.internal.core.search.indexing;

import org.eclipse.jdt.internal.core.search.processing.IJob;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
public abstract class IndexRequest implements IJob {
	

	
	protected boolean isCancelled = false;

	/*
	 * @see IJob#cancel()
	 */
	public void cancel() {
		this.isCancelled = true;
	}

}
