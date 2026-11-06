/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;

/**
 * The buffer manager manages the set of open buffers.
 * It implements an LRU cache of buffers.
 * <p>
 * Individual cache operations synchronize on {@link #openBuffers}. The monitor
 * of the {@code BufferManager} instance is a separate lock used by
 * {@link CompilationUnit#openBuffer} and {@link BecomeWorkingCopyOperation} to
 * coordinate buffer publication with primary working-copy registration and
 * detachment of an ordinary buffer. In that transition, buffer factories and
 * explicit closes of detached or rejected buffers run outside the manager
 * monitor, since they can invoke client code and acquire other model locks.
 * </p>
 */
public class BufferManager {

	protected static BufferManager DEFAULT_BUFFER_MANAGER;
	protected static boolean VERBOSE;

	/**
	 * LRU cache mapping openable owners to their buffers.
	 */
	private final BufferCache<IOpenable> openBuffers = new BufferCache<>(60);

	// Guarded by this manager's monitor. Retirement remains visible until close
	// finishes and every opener that claimed the physical buffer has released it.
	private final Map<IBuffer, CandidateState> candidates = new IdentityHashMap<>();

	private static final class CandidateState {
		final Object initializationLock = new Object();
		// Equal handles share an actual listener; unequal working-copy owners must
		// each receive changes, even if their providers share one physical buffer.
		final Map<Openable, Openable> listeners = new HashMap<>();
		int claims;
		volatile boolean retiring;
		boolean closeStarted;
		boolean closeFinished;

		CandidateState(Openable listener) {
			this.listeners.put(listener, listener);
		}
	}

	/** Owns one opener's claim, unless the cache already owns the same physical buffer. */
	final class BufferCandidate implements AutoCloseable {
		final IBuffer buffer;
		private final Openable listener;
		private final CandidateState state;
		private IOpenable owner;
		private boolean ownerCaptured;
		private boolean released;

		private BufferCandidate(CompilationUnit unit, IBuffer buffer, CandidateState state) {
			this.buffer = buffer;
			this.state = state;
			this.listener = state == null ? unit : state.listeners.computeIfAbsent(unit, key -> key);
		}

		boolean isCached() {
			return this.state == null;
		}

		Object initializationLock() {
			return this.state == null ? this : this.state.initializationLock;
		}

		Openable listener() {
			return this.listener;
		}

		/** Called outside the manager monitor; cleanup never repeats this provider callback. */
		IOpenable captureOwner() {
			if (!this.ownerCaptured) {
				this.ownerCaptured = true;
				this.owner = this.buffer.getOwner();
			}
			return this.owner;
		}

		boolean isRetiring() {
			return this.state != null && this.state.retiring;
		}

		void checkOpen() throws JavaModelException {
			// Avoid querying a buffer already being closed, and recheck after a
			// provider query that may itself reenter retirement.
			if (isRetiring() || this.buffer.isClosed() || isRetiring()) {
				throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS));
			}
		}

		@Override
		public void close() {
			synchronized (BufferManager.this) {
				if (this.released) {
					return;
				}
				this.released = true;
			}
			if (this.state != null) {
				releaseCandidate(this);
			}
		}
	}

	/** Acquiring a cached buffer never acquires ownership of its eventual cleanup. */
	synchronized BufferCandidate acquireBuffer(CompilationUnit unit, IBuffer buffer) {
		if (getBuffer(unit) == buffer) {
			return new BufferCandidate(unit, buffer, null);
		}
		CandidateState state = this.candidates.computeIfAbsent(buffer, _ -> new CandidateState(unit));
		state.claims++;
		return new BufferCandidate(unit, buffer, state);
	}

	private void releaseCandidate(BufferCandidate candidate) {
		synchronized (this) {
			CandidateState state = candidate.state;
			state.claims--;
			if (!state.retiring) {
				if (state.claims != 0) {
					return;
				}
				if (isCached(candidate)) {
					this.candidates.remove(candidate.buffer, state);
					return;
				}
				// Publication checks this reservation under the same monitor. Closing
				// outside the monitor therefore cannot race publication of this buffer.
				state.retiring = true;
			}
		}
		// A retiring opener may have registered its listener after retirement's
		// first removal. Every release retries removal; only the first cleanup closes.
		closeRetired(candidate.buffer, candidate.state);
	}

	private boolean isCached(BufferCandidate candidate) {
		synchronized (this.openBuffers) {
			if (candidate.owner != null) {
				return this.openBuffers.peek(candidate.owner) == candidate.buffer;
			}
			// A failing owner query must not make cleanup close an already-cached
			// shared buffer. Inspect identities without another provider callback.
			Enumeration<IBuffer> buffers = this.openBuffers.elements();
			while (buffers.hasMoreElements()) {
				if (buffers.nextElement() == candidate.buffer) {
					return true;
				}
			}
			return false;
		}
	}

	/** Owns the close of a detached buffer, outside the registration transaction. */
	final class BufferRetirement implements AutoCloseable {
		private final IBuffer buffer;
		private final CandidateState state;
		private boolean released;

		private BufferRetirement(IBuffer buffer, CandidateState state) {
			this.buffer = buffer;
			this.state = state;
		}

		@Override
		public void close() {
			synchronized (BufferManager.this) {
				if (this.released) {
					return;
				}
				this.released = true;
			}
			closeRetired(this.buffer, this.state);
		}
	}

	/** Detaches and reserves retirement before another opener can publish this physical buffer. */
	synchronized BufferRetirement retireBuffer(Openable owner) {
		IBuffer buffer;
		IOpenable key;
		synchronized (this.openBuffers) {
			key = this.openBuffers.getKey(owner);
			buffer = this.openBuffers.remove(owner);
		}
		if (buffer == null) {
			return null;
		}
		Openable listener = key instanceof Openable openable ? openable : owner;
		CandidateState state = this.candidates.computeIfAbsent(buffer, _ -> new CandidateState(listener));
		// An unequal owner's in-flight claim may have created the state after this
		// buffer was cached. Also detach the actual listener owned by the cache entry.
		state.listeners.putIfAbsent(listener, listener);
		state.retiring = true;
		return new BufferRetirement(buffer, state);
	}

	private void closeRetired(IBuffer buffer, CandidateState state) {
		boolean close;
		List<Openable> listeners;
		synchronized (this) {
			close = !state.closeStarted;
			state.closeStarted = true;
			listeners = new ArrayList<>(state.listeners.values());
		}
		try {
			Throwable failure = null;
			for (Openable listener : listeners) {
				try {
					buffer.removeBufferChangedListener(listener);
				} catch (RuntimeException | Error e) {
					if (failure == null) failure = e;
					else if (failure != e) failure.addSuppressed(e);
				}
			}
			if (close) {
				try {
					buffer.close();
				} catch (RuntimeException | Error e) {
					if (failure == null) failure = e;
					else if (failure != e) failure.addSuppressed(e);
				}
			}
			if (failure instanceof RuntimeException e) throw e;
			if (failure instanceof Error e) throw e;
		} finally {
			synchronized (this) {
				if (close) {
					state.closeFinished = true;
				}
				if (state.closeFinished && state.claims == 0) {
					this.candidates.remove(buffer, state);
				}
			}
		}
	}

	/**
	 * @deprecated
	 */
	protected org.eclipse.jdt.core.IBufferFactory defaultBufferFactory = new org.eclipse.jdt.core.IBufferFactory() {
	    /**
	     * @deprecated
	     */
		@Override
		public IBuffer createBuffer(IOpenable owner) {
			return BufferManager.createBuffer(owner);
		}
	};

/**
 * Adds a buffer to the table of open buffers.
 */
protected void addBuffer(IBuffer buffer) {
	if (VERBOSE) {
		String owner = ((Openable)buffer.getOwner()).toStringWithAncestors();
		System.out.println("Adding buffer for " + owner); //$NON-NLS-1$
	}
	synchronized (this.openBuffers) {
		this.openBuffers.put(buffer.getOwner(), buffer);
	}
	// close buffers that were removed from the cache if space was needed
	this.openBuffers.closeBuffers();
	if (VERBOSE) {
		System.out.println("-> Buffer cache filling ratio = " + NumberFormat.getInstance().format(this.openBuffers.fillingRatio()) + "%"); //$NON-NLS-1$//$NON-NLS-2$
	}
}

/** Publishes only after revalidating callbacks, retaining the actual listener for equal owners. */
IBuffer cacheBuffer(Openable listener, IOpenable owner, IBuffer buffer, BufferCache.PublicationCheck check) throws JavaModelException {
	synchronized (this.openBuffers) {
		return this.openBuffers.putIfAbsent(listener.equals(owner) ? listener : owner, buffer, check);
	}
}

/** Completes eviction outside the source-publication transaction. */
void closeBuffers() {
	this.openBuffers.closeBuffers();
}
public static IBuffer createBuffer(IOpenable owner) {
	JavaElement element = (JavaElement) owner;
	IResource resource = element.resource();
	return
		new Buffer(
			resource instanceof IFile ? (IFile)resource : null,
			owner,
			element.isReadOnly());
}
public static IBuffer createNullBuffer(IOpenable owner) {
	JavaElement element = (JavaElement) owner;
	IResource resource = element.resource();
	return
		new NullBuffer(
			resource instanceof IFile ? (IFile)resource : null,
			owner,
			element.isReadOnly());
}
/**
 * Returns the open buffer associated with the given owner,
 * or <code>null</code> if the owner does not have an open
 * buffer associated with it.
 */
public IBuffer getBuffer(IOpenable owner) {
	synchronized (this.openBuffers) {
		return this.openBuffers.get(owner);
	}
}
/**
 * Returns the default buffer manager.
 */
public synchronized static BufferManager getDefaultBufferManager() {
	if (DEFAULT_BUFFER_MANAGER == null) {
		DEFAULT_BUFFER_MANAGER = new BufferManager();
	}
	return DEFAULT_BUFFER_MANAGER;
}
/**
 * Returns the default buffer factory.
 * @deprecated
 */
public org.eclipse.jdt.core.IBufferFactory getDefaultBufferFactory() {
	return this.defaultBufferFactory;
}
/**
 * Returns an enumeration of all open buffers.
 * <p>
 * The <code>Enumeration</code> answered is thread safe.
 *
 * @see OverflowingLRUCache
 * @return Enumeration of IBuffer
 */
public Enumeration<IBuffer> getOpenBuffers() {
	Enumeration<IBuffer> result;
	synchronized (this.openBuffers) {
		this.openBuffers.shrink();
		result = this.openBuffers.elements();
	}
	// close buffers that were removed from the cache if space was needed
	this.openBuffers.closeBuffers();
	return result;
}

/**
 * Removes the owner's buffer without closing it or notifying its listeners.
 * This method may be called while holding this manager's monitor to detach an
 * ordinary buffer atomically with primary working-copy registration. The caller
 * must close the returned buffer after releasing that monitor, because closing
 * can invoke buffer listeners. It is the close, not this removal, that must run
 * outside the manager monitor.
 */
IBuffer removeBuffer(IOpenable owner) {
	synchronized (this.openBuffers) {
		return this.openBuffers.remove(owner);
	}
}

/**
 * Removes a buffer from the table of open buffers, provided it is still the
 * cached buffer for its owner.
 */
protected void removeBuffer(IBuffer buffer) {
	if (VERBOSE) {
		String owner = ((Openable)buffer.getOwner()).toStringWithAncestors();
		System.out.println("Removing buffer for " + owner); //$NON-NLS-1$
	}
	synchronized (this.openBuffers) {
		// A delayed close event must not remove a replacement for the same owner.
		// A replacement remains cached until its own close, detachment or cache eviction.
		if (this.openBuffers.peek(buffer.getOwner()) == buffer) {
			this.openBuffers.remove(buffer.getOwner());
		}
	}
	// close buffers that were removed from the cache (should be only one)
	this.openBuffers.closeBuffers();
	if (VERBOSE) {
		System.out.println("-> Buffer cache filling ratio = " + NumberFormat.getInstance().format(this.openBuffers.fillingRatio()) + "%"); //$NON-NLS-1$//$NON-NLS-2$
	}
}
}
