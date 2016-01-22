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
package org.eclipse.jdt.internal.core.pdom;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.jdt.internal.core.pdom.db.Database;
import org.eclipse.jdt.internal.core.pdom.db.IndexException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Database for storing semantic information for all java projects.
 * @since 3.12
 */
public class PDOM {
	private static final int CANCELLATION_CHECK_INTERVAL = 500;
	private static final int BLOCKED_WRITE_LOCK_OUTPUT_INTERVAL = 30000;
	private static final int LONG_WRITE_LOCK_REPORT_THRESHOLD = 1000;
	private static final int LONG_READ_LOCK_WAIT_REPORT_THRESHOLD = 1000;
	static boolean sDEBUG_LOCKS= false;

	private final int currentVersion;
	private final int maxVersion;
	private final int minVersion;

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

	public static final int LINKAGES = Database.DATA_AREA;
	public static final int FILE_INDEX = Database.DATA_AREA + 4;
	public static final int INDEX_OF_DEFECTIVE_FILES = Database.DATA_AREA + 8;
	public static final int INDEX_OF_FILES_WITH_UNRESOLVED_INCLUDES = Database.DATA_AREA + 12;
	public static final int PROPERTIES = Database.DATA_AREA + 16;
	public static final int TAG_INDEX = Database.DATA_AREA + 20;
	public static final int END= Database.DATA_AREA + 24;
	static {
		assert END <= Database.CHUNK_SIZE;
	}

	public static class ChangeEvent {
		public Set<IIndexFileLocation> fClearedFiles= new HashSet<>();
		public Set<IIndexFileLocation> fFilesWritten= new HashSet<>();
		private boolean fCleared;
		private boolean fReloaded;
		private boolean fNewFiles;

		private void setCleared() {
			fCleared= true;
			fReloaded= false;
			fNewFiles= false;

			fClearedFiles.clear();
			fFilesWritten.clear();
		}

		public boolean isCleared() {
			return fCleared;
		}

		public void setReloaded() {
			fReloaded= true;
		}

		public boolean isReloaded() {
			return fReloaded;
		}

		public void setHasNewFiles() {
			fNewFiles = true;
		}

		public boolean hasNewFiles() {
			return fNewFiles;
		}

		public boolean isTrivial() {
			return !fCleared && !fReloaded && !fNewFiles && fClearedFiles.isEmpty() &&
					fFilesWritten.isEmpty();
		}
	}

	// Local caches
	protected Database db;
	private File fPath;
	private final HashMap<Object, Object> fResultCache= new HashMap<>();
	protected ChangeEvent fEvent= new ChangeEvent();
	private final PDOMNodeTypeRegistry<PDOMNode> fNodeTypeRegistry;
	private HashSet<Long> pendingDeletions = new HashSet<>();

	public PDOM(File dbPath, PDOMNodeTypeRegistry<PDOMNode> nodeTypes, int minVersion, int maxVersion,
			int currentVersion) throws IndexException {
		this(dbPath, ChunkCache.getSharedInstance(), nodeTypes, minVersion, maxVersion, currentVersion);
	}

	public PDOM(File dbPath, ChunkCache chunkCache, PDOMNodeTypeRegistry<PDOMNode> nodeTypes, int minVersion,
			int maxVersion, int currentVersion) throws IndexException {
		this.currentVersion = currentVersion;
		this.maxVersion = maxVersion;
		this.minVersion = minVersion;
		this.fNodeTypeRegistry = nodeTypes;
		loadDatabase(dbPath, chunkCache);
		if (sDEBUG_LOCKS) {
			this.fLockDebugging = new HashMap<>();
			System.out.println("Debugging PDOM Locks"); //$NON-NLS-1$
		}
	}

	public void scheduleDeletion(long addressOfNodeToDelete) {
		if (this.pendingDeletions.contains(addressOfNodeToDelete)) {
			// TODO(sxenos): Sometimes the same node gets scheduled for deletion more than once, which is why
			// pendingDeletions is a HashSet rather than a queue. We need to understand the circumstances in which
			// this can happen. If it can be prevented, we should prevent it and change this back to a queue. 
			Package.log("PDOM object queued for deletion twice", new RuntimeException()); //$NON-NLS-1$
			return;
		}
		this.pendingDeletions.add(addressOfNodeToDelete);
	}

	/**
	 * Synchronously processes all pending deletions
	 */
	public void processDeletions() {
		while (!this.pendingDeletions.isEmpty()) {
			Iterator<Long> iter = this.pendingDeletions.iterator();
			long next = iter.next();

			delete(next);

			iter.remove();
		}
	}

	/**
	 * Returns whether this PDOM can never be written to. Writable subclasses should return false.
	 */
	protected boolean isPermanentlyReadOnly() {
		return false;
	}

	private void loadDatabase(File dbPath, ChunkCache cache) throws IndexException {
		fPath= dbPath;
		final boolean lockDB= db == null || lockCount != 0;

		clearCaches();
		db = new Database(fPath, cache, getDefaultVersion(), isPermanentlyReadOnly());

		db.setLocked(lockDB);
		if (!isSupportedVersion()) {
			Package.log("Index database is uses an unsupported version " + db.getVersion() 
				+ " Deleting and recreating.", null);
			db.close();
			fPath.delete();
			db = new Database(fPath, cache, getDefaultVersion(), isPermanentlyReadOnly());
			db.setLocked(lockDB);
		}
		db.setLocked(lockCount != 0);
	}

	public Database getDB() {
		return db;
	}

	private long getFirstLinkageRecord() throws IndexException {
		return db.getRecPtr(LINKAGES);
	}

	// Read-write lock rules. Readers don't conflict with other readers,
	// Writers conflict with readers, and everyone conflicts with writers.
	private final Object mutex = new Object();
	private int lockCount;
	private int waitingReaders;
	private long lastWriteAccess= 0;
	private long lastReadAccess= 0;
	private long timeWriteLockAcquired;

	public void acquireReadLock() {
		try {
			long t = sDEBUG_LOCKS ? System.nanoTime() : 0;
			synchronized (mutex) {
				++waitingReaders;
				try {
					while (lockCount < 0)
						mutex.wait();
				} finally {
					--waitingReaders;
				}
				++lockCount;
				db.setLocked(true);
	
				if (sDEBUG_LOCKS) {
					t = (System.nanoTime() - t) / 1000000;
					if (t >= LONG_READ_LOCK_WAIT_REPORT_THRESHOLD) {
						System.out.println("Acquired index read lock after " + t + " ms wait."); //$NON-NLS-1$//$NON-NLS-2$
					}
					incReadLock(fLockDebugging);
				}
			}
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
	}

	public void releaseReadLock() {
		synchronized (mutex) {
			assert lockCount > 0: "No lock to release"; //$NON-NLS-1$
			if (sDEBUG_LOCKS) {
				decReadLock(fLockDebugging);
			}

			lastReadAccess= System.currentTimeMillis();
			if (lockCount > 0)
				--lockCount;
			mutex.notifyAll();
			db.setLocked(lockCount != 0);
		}
		// A lock release probably means that some AST is going away. The result cache has to be
		// cleared since it may contain objects belonging to the AST that is going away. A failure
		// to release an AST object would cause a memory leak since the whole AST would remain
		// pinned to memory.
		// TODO(sprigogin): It would be more efficient to replace the global result cache with
		// separate caches for each AST.
		clearResultCache();
	}

	/**
	 * Acquire a write lock on this PDOM. Blocks until any existing read/write locks are released.
	 * @throws InterruptedException
	 * @throws IllegalStateException if this PDOM is not writable
	 */
	public void acquireWriteLock(IProgressMonitor monitor) {
		try {
			acquireWriteLock(0, monitor);
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
	}

	/**
	 * Acquire a write lock on this PDOM, giving up the specified number of read locks first. Blocks
	 * until any existing read/write locks are released.
	 * @throws InterruptedException
	 * @throws IllegalStateException if this PDOM is not writable
	 */
	public void acquireWriteLock(int giveupReadLocks, IProgressMonitor monitor) throws InterruptedException {
		assert !isPermanentlyReadOnly();
		synchronized (mutex) {
			if (sDEBUG_LOCKS) {
				incWriteLock(giveupReadLocks);
			}

			if (giveupReadLocks > 0) {
				// give up on read locks
				assert lockCount >= giveupReadLocks: "Not enough locks to release"; //$NON-NLS-1$
				if (lockCount < giveupReadLocks) {
					giveupReadLocks= lockCount;
				}
			} else {
				giveupReadLocks= 0;
			}

			// Let the readers go first
			long start= sDEBUG_LOCKS ? System.currentTimeMillis() : 0;
			while (lockCount > giveupReadLocks || waitingReaders > 0) {
				mutex.wait(CANCELLATION_CHECK_INTERVAL);
				if (monitor != null && monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				if (sDEBUG_LOCKS) {
					start = reportBlockedWriteLock(start, giveupReadLocks);
				}
			}
			lockCount= -1;
			if (sDEBUG_LOCKS)
				timeWriteLockAcquired = System.currentTimeMillis();
			db.setExclusiveLock();
		}
	}

	final public void releaseWriteLock() {
		releaseWriteLock(0, true);
	}

	@SuppressWarnings("nls")
	public void releaseWriteLock(int establishReadLocks, boolean flush) {
		// When all locks are released we can clear the result cache.
		if (establishReadLocks == 0) {
			processDeletions();
			clearResultCache();
		}
		try {
			this.db.giveUpExclusiveLock(flush);
		} catch (IndexException e) {
			Package.log(e);
		}
		assert lockCount == -1;
		if (!fEvent.isTrivial())
			lastWriteAccess= System.currentTimeMillis();
		final ChangeEvent event= fEvent;
		fEvent= new ChangeEvent();
		synchronized (mutex) {
			if (sDEBUG_LOCKS) {
				long timeHeld = lastWriteAccess - timeWriteLockAcquired;
				if (timeHeld >= LONG_WRITE_LOCK_REPORT_THRESHOLD) {
					System.out.println("Index write lock held for " + timeHeld + " ms");
				}
				decWriteLock(establishReadLocks);
			}

			if (lockCount < 0)
				lockCount= establishReadLocks;
			mutex.notifyAll();
			db.setLocked(lockCount != 0);
		}
		//fireChange(event);
	}

	public boolean hasWaitingReaders() {
		synchronized (this.mutex) {
			return this.waitingReaders > 0;
		}
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

	// For debugging lock issues
	static class DebugLockInfo {
		int fReadLocks;
		int fWriteLocks;
		List<StackTraceElement[]> fTraces= new ArrayList<>();

		public int addTrace() {
			fTraces.add(Thread.currentThread().getStackTrace());
			return fTraces.size();
		}

		@SuppressWarnings("nls")
		public void write(String threadName) {
			System.out.println("Thread: '" + threadName + "': " + fReadLocks + " readlocks, " + fWriteLocks + " writelocks");
			for (StackTraceElement[] trace : fTraces) {
				System.out.println("  Stacktrace:");
				for (StackTraceElement ste : trace) {
					System.out.println("    " + ste);
				}
			}
		}

		public void inc(DebugLockInfo val) {
			fReadLocks+= val.fReadLocks;
			fWriteLocks+= val.fWriteLocks;
			fTraces.addAll(val.fTraces);
		}
	}

	// For debugging lock issues
	private Map<Thread, DebugLockInfo> fLockDebugging;

	// For debugging lock issues
	private static DebugLockInfo getLockInfo(Map<Thread, DebugLockInfo> lockDebugging) {
		assert sDEBUG_LOCKS;

		Thread key = Thread.currentThread();
		DebugLockInfo result= lockDebugging.get(key);
		if (result == null) {
			result= new DebugLockInfo();
			lockDebugging.put(key, result);
		}
		return result;
	}

	// For debugging lock issues
	static void incReadLock(Map<Thread, DebugLockInfo> lockDebugging) {
		DebugLockInfo info = getLockInfo(lockDebugging);
		info.fReadLocks++;
		if (info.addTrace() > 10) {
			outputReadLocks(lockDebugging);
		}
	}

	// For debugging lock issues
	@SuppressWarnings("nls")
	static void decReadLock(Map<Thread, DebugLockInfo> lockDebugging) throws AssertionError {
		DebugLockInfo info = getLockInfo(lockDebugging);
		if (info.fReadLocks <= 0) {
			outputReadLocks(lockDebugging);
			throw new AssertionError("Superfluous releaseReadLock");
		}
		if (info.fWriteLocks != 0) {
			outputReadLocks(lockDebugging);
			throw new AssertionError("Releasing readlock while holding write lock");
		}
		if (--info.fReadLocks == 0) {
			lockDebugging.remove(Thread.currentThread());
		} else {
			info.addTrace();
		}
	}

	// For debugging lock issues
	@SuppressWarnings("nls")
	private void incWriteLock(int giveupReadLocks) throws AssertionError {
		DebugLockInfo info = getLockInfo(fLockDebugging);
		if (info.fReadLocks != giveupReadLocks) {
			outputReadLocks(fLockDebugging);
			throw new AssertionError("write lock with " + giveupReadLocks + " readlocks, expected " + info.fReadLocks);
		}
		if (info.fWriteLocks != 0)
			throw new AssertionError("Duplicate write lock");
		info.fWriteLocks++;
	}

	// For debugging lock issues
	private void decWriteLock(int establishReadLocks) throws AssertionError {
		DebugLockInfo info = getLockInfo(fLockDebugging);
		if (info.fReadLocks != establishReadLocks)
			throw new AssertionError("release write lock with " + establishReadLocks + " readlocks, expected " + info.fReadLocks); //$NON-NLS-1$ //$NON-NLS-2$
		if (info.fWriteLocks != 1)
			throw new AssertionError("Wrong release write lock"); //$NON-NLS-1$
		info.fWriteLocks= 0;
		if (info.fReadLocks == 0) {
			fLockDebugging.remove(Thread.currentThread());
		}
	}

	// For debugging lock issues
	@SuppressWarnings("nls")
	private long reportBlockedWriteLock(long start, int giveupReadLocks) {
		long now= System.currentTimeMillis();
		if (now >= start + BLOCKED_WRITE_LOCK_OUTPUT_INTERVAL) {
			System.out.println();
			System.out.println("Blocked writeLock");
			System.out.println("  lockcount= " + lockCount + ", giveupReadLocks=" + giveupReadLocks + ", waitingReaders=" + waitingReaders);
			outputReadLocks(fLockDebugging);
			start= now;
		}
		return start;
	}

	// For debugging lock issues
	@SuppressWarnings("nls")
	private static void outputReadLocks(Map<Thread, DebugLockInfo> lockDebugging) {
		System.out.println("---------------------  Lock Debugging -------------------------");
		for (Thread th: lockDebugging.keySet()) {
			DebugLockInfo info = lockDebugging.get(th);
			info.write(th.getName());
		}
		System.out.println("---------------------------------------------------------------");
	}

	// For debugging lock issues
	public void adjustThreadForReadLock(Map<Thread, DebugLockInfo> lockDebugging) {
		for (Thread th : lockDebugging.keySet()) {
			DebugLockInfo val= lockDebugging.get(th);
			if (val.fReadLocks > 0) {
				DebugLockInfo myval= fLockDebugging.get(th);
				if (myval == null) {
					myval= new DebugLockInfo();
					fLockDebugging.put(th, myval);
				}
				myval.inc(val);
				for (int i = 0; i < val.fReadLocks; i++) {
					decReadLock(fLockDebugging);
				}
			}
		}
	}

    /**
     * @param record
     * @param nodeType
     * @return
     */
    public PDOMNode getNode(long record, short nodeType) throws IndexException {
    	return this.fNodeTypeRegistry.createNode(this, record, nodeType);
    }

    public <T extends PDOMNode> ITypeFactory<T> getTypeFactory(short nodeType) {
    	return this.fNodeTypeRegistry.getTypeFactory(nodeType);
    }

	/**
	 * Returns the type ID for the given class
	 */
	public short getNodeType(Class<? extends PDOMNode> toQuery) {
		return this.fNodeTypeRegistry.getTypeForClass(toQuery);
	}

	private void delete(long address) {
		if (address == 0) {
			return;
		}
		short nodeType = PDOMNode.NODE_TYPE.get(this, address);
	
		// Look up the type
		ITypeFactory<? extends PDOMNode> factory1 = getTypeFactory(nodeType);
	
		// Call its destructor
		factory1.destruct(this, address);
	
		// Free up its memory
		getDB().free(address);
	}
}
