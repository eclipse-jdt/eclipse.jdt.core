package org.eclipse.jdt.internal.core.index.impl;

public interface ILRUCacheable {
	/**
	 * Returns the space the receiver consumes in an LRU Cache.  The default space
	 * value is 1.
	 *
	 * @return int Amount of cache space taken by the receiver
	 */
	public int getCacheFootprint();
}
