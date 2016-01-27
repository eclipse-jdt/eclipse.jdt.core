package org.eclipse.jdt.internal.core.nd.indexer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.core.nd.indexer.messages"; //$NON-NLS-1$
	public static String Indexer_updating_index_job_name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
