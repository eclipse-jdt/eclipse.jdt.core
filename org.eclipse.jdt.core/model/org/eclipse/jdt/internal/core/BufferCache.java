/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.LRUCache;

/**
 * An LRU cache of <code>IBuffers</code>.
 */
public class BufferCache<K> extends OverflowingLRUCache<K, IBuffer> {

	private final ThreadLocal<List<IBuffer>> buffersToClose = new ThreadLocal<>();

	/**
	 * Constructs a new buffer cache of the given size.
	 */
	public BufferCache(int size) {
		super(size);
	}
	/**
	 * Constructs a new buffer cache of the given size.
	 */
	public BufferCache(int size, int overflow) {
		super(size, overflow);
	}

	@FunctionalInterface
	interface PublicationCheck {
		boolean canPublish() throws JavaModelException;
	}

	/** Validate after eviction queries, which may reenter the source buffer lifecycle. */
	IBuffer putIfAbsent(K key, IBuffer buffer, PublicationCheck check) throws JavaModelException {
		if (this.overflow > 0) {
			shrink();
		}
		int space = spaceFor(buffer);
		if (peek(key) == null) {
			makeSpace(space);
		}
		try {
			if (!check.canPublish()) {
				return null;
			}
			IBuffer existing = get(key);
			if (existing != null) {
				return existing;
			}
			// No provider callbacks between validation and insertion, nor replacement of a reentrant winner.
			privateAdd(key, buffer, space);
			return buffer;
		} finally {
			// makeSpace included a prospective insertion, which validation may have rejected.
			this.overflow = Math.max(0, this.currentSpace - this.spaceLimit);
		}
	}

	@Override
	protected boolean close(LRUCacheEntry<K, IBuffer> entry) {
		IBuffer buffer= entry.value;

		// prevent buffer that have unsaved changes or working copy buffer to be removed
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=39311
		if (!((Openable)buffer.getOwner()).canBufferBeRemovedFromCache(buffer)) {
			return false;
		} else {
			List<IBuffer> buffers = this.buffersToClose.get();
			if (buffers == null) {
				buffers = new ArrayList<>();
				this.buffersToClose.set(buffers);
			}
			buffers.add(buffer);
			return true;
		}
	}

	void closeBuffers() {
		List<IBuffer> buffers = this.buffersToClose.get();
		if (buffers == null)
			return;
		this.buffersToClose.remove();
		for (IBuffer buffer : buffers) {
			buffer.close();
		}
	}

	@Override
	protected LRUCache<K, IBuffer> newInstance(int size, int newOverflow) {
		return new BufferCache<>(size, newOverflow);
	}
}
