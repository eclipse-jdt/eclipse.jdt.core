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

import java.text.NumberFormat;
import java.util.Enumeration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;

/**
 * The buffer manager manages the set of open buffers.
 * It implements an LRU cache of buffers.
 */
public class BufferManager {

	protected static BufferManager DEFAULT_BUFFER_MANAGER;
	protected static boolean VERBOSE;

	/**
	 * LRU cache of buffers, keyed by their owning model handles.
	 */
	private final BufferCache<IOpenable> openBuffers = new BufferCache<>(60);

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
	cacheBuffer(buffer.getOwner(), buffer);
	closeBuffers();
}

/**
 * Publishes a source buffer after the lifecycle coordinator has checked that no
 * entry exists, revalidating after eviction queries. For an equal owner, retain
 * the actual registered listener as the key so retirement can remove that same
 * instance. Preserve other owner keys.
 */
IBuffer cacheBuffer(Openable listener, IOpenable owner, IBuffer buffer, BufferCache.PublicationCheck check) throws JavaModelException {
	synchronized (this.openBuffers) {
		IBuffer result = this.openBuffers.putIfAbsent(listener.equals(owner) ? listener : owner, buffer, check);
		if (result == buffer) {
			// Preparation edits occurred before this buffer had a current registration.
			JavaModelManager.getJavaModelManager().getElementsOutOfSynchWithBuffers().add(listener);
		}
		return result;
	}
}

/** Adds a buffer without invoking close callbacks for evicted entries. */
private void cacheBuffer(IOpenable owner, IBuffer buffer) {
	if (VERBOSE) {
		String ownerName = ((Openable)owner).toStringWithAncestors();
		System.out.println("Adding buffer for " + ownerName); //$NON-NLS-1$
	}
	synchronized (this.openBuffers) {
		this.openBuffers.put(owner, buffer);
	}
	if (VERBOSE) {
		System.out.println("-> Buffer cache filling ratio = " + NumberFormat.getInstance().format(this.openBuffers.fillingRatio()) + "%"); //$NON-NLS-1$//$NON-NLS-2$
	}
}

/** Completes eviction outside the lifecycle transaction that published a buffer. */
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

/** Only the actual listener receives the notifications needed to invalidate its AST cache. */
boolean isBufferListener(CompilationUnit unit) {
	synchronized (this.openBuffers) {
		return this.openBuffers.peek(unit) != null && this.openBuffers.getKey(unit) == unit;
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
 * The caller must close the returned buffer outside the buffer manager lock.
 */
IBuffer removeBuffer(IOpenable owner) {
	synchronized (this.openBuffers) {
		return this.openBuffers.remove(owner);
	}
}

record DetachedBuffer(IBuffer buffer, Openable listener) {}

/** Captures the cache registration and detaches its consistency state before provider close. */
DetachedBuffer detachBuffer(Openable owner) {
	synchronized (this.openBuffers) {
		JavaModelManager.getJavaModelManager().getElementsOutOfSynchWithBuffers().remove(owner);
		IOpenable key = this.openBuffers.getKey(owner);
		IBuffer buffer = this.openBuffers.remove(owner);
		return buffer == null ? null : new DetachedBuffer(buffer, key instanceof Openable listener ? listener : owner);
	}
}

/** Applies edits only to the current registration, and allows close cleanup after eviction. */
boolean bufferChanged(Openable listener, IBuffer buffer, boolean closed) {
	IOpenable owner = buffer.getOwner();
	synchronized (this.openBuffers) {
		IBuffer current = this.openBuffers.peek(owner);
		if (current != buffer && (!closed || current != null)) {
			return false;
		}
		var outOfSync = JavaModelManager.getJavaModelManager().getElementsOutOfSynchWithBuffers();
		if (closed) {
			outOfSync.remove(listener);
			if (current == buffer) {
				this.openBuffers.remove(owner);
			}
		} else {
			outOfSync.add(listener);
		}
		return true;
	}
}

/**
 * Removes a buffer from the table of open buffers.
 */
protected void removeBuffer(IBuffer buffer) {
	IOpenable bufferOwner = buffer.getOwner();
	if (VERBOSE) {
		String owner = ((Openable)bufferOwner).toStringWithAncestors();
		System.out.println("Removing buffer for " + owner); //$NON-NLS-1$
	}
	synchronized (this.openBuffers) {
		if (this.openBuffers.peek(bufferOwner) == buffer) {
			this.openBuffers.remove(bufferOwner);
		}
	}
	// close buffers that were removed from the cache (should be only one)
	this.openBuffers.closeBuffers();
	if (VERBOSE) {
		System.out.println("-> Buffer cache filling ratio = " + NumberFormat.getInstance().format(this.openBuffers.fillingRatio()) + "%"); //$NON-NLS-1$//$NON-NLS-2$
	}
}
}
