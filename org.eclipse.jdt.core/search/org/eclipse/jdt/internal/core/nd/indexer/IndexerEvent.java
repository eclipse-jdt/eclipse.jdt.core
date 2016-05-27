package org.eclipse.jdt.internal.core.nd.indexer;

import org.eclipse.jdt.core.IJavaElementDelta;

public class IndexerEvent {
	final IJavaElementDelta delta;

	private IndexerEvent(IJavaElementDelta delta) {
		this.delta = delta;
	}

	public static IndexerEvent createChange(IJavaElementDelta delta) {
		return new IndexerEvent(delta);
	}

	public IJavaElementDelta getDelta() {
		return this.delta;
	}
}
