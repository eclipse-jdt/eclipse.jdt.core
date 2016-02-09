/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.core.nd.db.ChunkCache;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IndexException;

/**
 * Network Database for storing semantic information.
 *
 * @since 3.12
 */
public class Nd {
	private static final int CANCELLATION_CHECK_INTERVAL = 500;
	private static final int BLOCKED_WRITE_LOCK_OUTPUT_INTERVAL = 30000;
	private static final int LONG_WRITE_LOCK_REPORT_THRESHOLD = 1000;
	private static final int LONG_READ_LOCK_WAIT_REPORT_THRESHOLD = 1000;
	static boolean sDEBUG_LOCKS= false;

	private final int currentVersion;
	private final int maxVersion;
	private final int minVersion;

	// Read-write lock rules. Readers don't conflict with other readers,
	// Writers conflict with readers, and everyone conflicts with writers.
	private final Object lockMutex = new Object();
	private long lastWriteAccess = 0;
	private long lastReadAccess = 0;
	private ReentrantReadWriteLock lock;

	public static int version(int major, int minor) {
		return (major << 16) + minor;
	}

	/**
	 * Returns the version that shall be used when creating new databases.
	 */
	public int getDefaultVersion() {
		return this.currentVersion;
	}

	public boolean isSupportedVersion(int vers) {
		return vers >= this.minVersion && vers <= this.maxVersion;
	}

	public int getMinSupportedVersion() {
		return this.minVersion;
	}

	public int getMaxSupportedVersion() {
		return this.maxVersion;
	}

	public static String versionString(int version) {
		final int major= version >> 16;
		final int minor= version & 0xffff;
		return "" + major + '.' + minor; //$NON-NLS-1$
	}

	public static class ChangeEvent {
		public Set<IIndexFileLocation> fClearedFiles= new HashSet<>();
		public Set<IIndexFileLocation> fFilesWritten= new HashSet<>();
		private boolean fCleared;
		private boolean fReloaded;
		private boolean fNewFiles;

//		private void setCleared() {
//			this.fCleared= true;
//			this.fReloaded= false;
//			this.fNewFiles= false;
//
//			this.fClearedFiles.clear();
//			this.fFilesWritten.clear();
//		}

		public boolean isCleared() {
			return this.fCleared;
		}

		public void setReloaded() {
			this.fReloaded= true;
		}

		public boolean isReloaded() {
			return this.fReloaded;
		}

		public void setHasNewFiles() {
			this.fNewFiles = true;
		}

		public boolean hasNewFiles() {
			return this.fNewFiles;
		}

		public boolean isTrivial() {
			return !this.fCleared && !this.fReloaded && !this.fNewFiles && this.fClearedFiles.isEmpty() &&
					this.fFilesWritten.isEmpty();
		}
	}

	// Local caches
	protected Database db;
	private File fPath;
	private final HashMap<Object, Object> fResultCache= new HashMap<>();
	protected ChangeEvent fEvent= new ChangeEvent();
	private final NdNodeTypeRegistry<NdNode> fNodeTypeRegistry;
	private HashMap<Long, Throwable> pendingDeletions = new HashMap<>();

	private IReader fReader = new IReader() {
		@Override
		public void close() {
			releaseReadLock();
		}
	};

	/**
	 * This long is incremented every time a change is written to the database. Can be used to determine if the database
	 * has changed.
	 */
	private long fWriteNumber;

	public Nd(File dbPath, NdNodeTypeRegistry<NdNode> nodeTypes, int minVersion, int maxVersion,
			int currentVersion) throws IndexException {
		this(dbPath, ChunkCache.getSharedInstance(), nodeTypes, minVersion, maxVersion, currentVersion);
	}

	public Nd(File dbPath, ChunkCache chunkCache, NdNodeTypeRegistry<NdNode> nodeTypes, int minVersion,
			int maxVersion, int currentVersion) throws IndexException {
		this.lock = new ReentrantReadWriteLock();
		this.currentVersion = currentVersion;
		this.maxVersion = maxVersion;
		this.minVersion = minVersion;
		this.fNodeTypeRegistry = nodeTypes;
		loadDatabase(dbPath, chunkCache);
		// if (sDEBUG_LOCKS) {
		// this.fLockDebugging = new HashMap<>();
		// System.out.println("Debugging PDOM Locks"); //$NON-NLS-1$
		// }
	}

	public long getWriteNumber() {
		return this.fWriteNumber;
	}

	public void scheduleDeletion(long addressOfNodeToDelete) {
		// Sometimes an object can be scheduled for deletion twice, if it is created and then discarded shortly
		// afterward during indexing. This may indicate an inefficiency in the indexer but is not necessarily
		// a bug.
		if (this.pendingDeletions.containsKey(addressOfNodeToDelete)) {
			Package.log("PDOM object queued for deletion twice", new RuntimeException()); //$NON-NLS-1$
			Package.log("Earlier deletion stack was this:", this.pendingDeletions.get(addressOfNodeToDelete)); //$NON-NLS-1$
			return;
		}
		this.pendingDeletions.put(addressOfNodeToDelete, new RuntimeException());
	}

	/**
	 * Synchronously processes all pending deletions
	 */
	public void processDeletions() {
		while (!this.pendingDeletions.isEmpty()) {
			long next = this.pendingDeletions.keySet().iterator().next();

			deleteIfUnreferenced(next);

			this.pendingDeletions.remove(next);
		}
	}

	/**
	 * Returns whether this PDOM can never be written to. Writable subclasses should return false.
	 */
	protected boolean isPermanentlyReadOnly() {
		return false;
	}

	private void loadDatabase(File dbPath, ChunkCache cache) throws IndexException {
		this.fPath= dbPath;
		final boolean lockDB = this.db == null;

		clearCaches();
		this.db = new Database(this.fPath, cache, getDefaultVersion(), isPermanentlyReadOnly());

		this.db.setLocked(true);
		if (!isSupportedVersion()) {
			Package.log("Index database is uses an unsupported version " + this.db.getVersion() //$NON-NLS-1$
				+ " Deleting and recreating.", null); //$NON-NLS-1$
			this.db.close();
			this.fPath.delete();
			this.db = new Database(this.fPath, cache, getDefaultVersion(), isPermanentlyReadOnly());
			this.db.setLocked(true);
		}
		this.fWriteNumber = this.db.getLong(Database.WRITE_NUMBER_OFFSET);
		synchronized (this.lockMutex) {
			syncLockedState();
		}
	}

	void syncLockedState() {
		int writeLocks = this.lock.getWriteHoldCount();
		int readLocks = this.lock.getReadLockCount();

		this.db.setLocked(writeLocks != 0 || readLocks != 0);
	}

	public Database getDB() {
		return this.db;
	}

	public IReader acquireReadLock() {
		this.lock.readLock().lock();
		synchronized (this.lockMutex) {
			syncLockedState();
		}
		return this.fReader;
	}

	public void releaseReadLock() {
		synchronized (this.lockMutex) {
			this.lock.readLock().unlock();
			syncLockedState();
			this.lastReadAccess = System.currentTimeMillis();
		}

		// TODO(sprigogin): It would be more efficient to replace the global result cache with
		// separate caches for each AST.
		clearResultCache();
	}

	/**
	 * Acquire a write lock on this PDOM. Blocks until any existing read/write locks are released.
	 * @throws OperationCanceledException
	 * @throws IllegalStateException if this PDOM is not writable
	 */
	public void acquireWriteLock(IProgressMonitor monitor) {
		assert !isPermanentlyReadOnly();
		this.lock.writeLock().lock();
		this.db.setExclusiveLock();
	}

	final public void releaseWriteLock() {
		synchronized (this.lockMutex) {
			int writeHoldCount = this.lock.getWriteHoldCount();
			assert writeHoldCount >= 1;
			if (writeHoldCount == 1) {
				processDeletions();
				this.db.putLong(Database.WRITE_NUMBER_OFFSET, ++this.fWriteNumber);
				clearResultCache();
				this.db.giveUpExclusiveLock(true);
				this.lastWriteAccess = System.currentTimeMillis();
			}
			this.lock.writeLock().unlock();
			syncLockedState();
		}
	}

	public boolean hasWaitingReaders() {
		return this.lock.hasQueuedThreads();
	}

	public long getLastWriteAccess() {
		return this.lastWriteAccess;
	}

	public long getLastReadAccess() {
		return this.lastReadAccess;
	}

	public boolean isSupportedVersion() throws IndexException {
		final int version = this.db.getVersion();
		return version >= this.minVersion && version <= this.maxVersion;
	}

	public void close() throws IndexException {
		this.db.close();
		clearCaches();
	}

	private void clearCaches() {
//		fileIndex= null;
//		tagIndex = null;
//		indexOfDefectiveFiles= null;
//		indexOfFiledWithUnresolvedIncludes= null;
//		fLinkageIDCache.clear();
		clearResultCache();
	}

	public void clearResultCache() {
		synchronized (this.fResultCache) {
			this.fResultCache.clear();
		}
	}

	public Object getCachedResult(Object key) {
		synchronized (this.fResultCache) {
			return this.fResultCache.get(key);
		}
	}

	public void putCachedResult(Object key, Object result) {
		putCachedResult(key, result, true);
	}

	public Object putCachedResult(Object key, Object result, boolean replace) {
		synchronized (this.fResultCache) {
			Object old= this.fResultCache.put(key, result);
			if (old != null && !replace) {
				this.fResultCache.put(key, old);
				return old;
			}
			return result;
		}
	}

	public void removeCachedResult(Object key) {
		synchronized (this.fResultCache) {
			this.fResultCache.remove(key);
		}
	}

    public NdNode getNode(long address, short nodeType) throws IndexException {
    	return this.fNodeTypeRegistry.createNode(this, address, nodeType);
    }

    public <T extends NdNode> ITypeFactory<T> getTypeFactory(short nodeType) {
    	return this.fNodeTypeRegistry.getTypeFactory(nodeType);
    }

	/**
	 * Returns the type ID for the given class
	 */
	public short getNodeType(Class<? extends NdNode> toQuery) {
		return this.fNodeTypeRegistry.getTypeForClass(toQuery);
	}

	private void deleteIfUnreferenced(long address) {
		if (address == 0) {
			return;
		}
		short nodeType = NdNode.NODE_TYPE.get(this, address);

		// Look up the type
		ITypeFactory<? extends NdNode> factory1 = getTypeFactory(nodeType);

		if (factory1.isReadyForDeletion(this, address)) {
			// Call its destructor
			factory1.destruct(this, address);

			// Free up its memory
			getDB().free(address);
		}
	}

	public void delete(long address) {
		if (address == 0) {
			return;
		}
		short nodeType = NdNode.NODE_TYPE.get(this, address);

		// Look up the type
		ITypeFactory<? extends NdNode> factory1 = getTypeFactory(nodeType);

		// Call its destructor
		factory1.destruct(this, address);

		// Free up its memory
		getDB().free(address);
	}
}
