package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.Util;
import java.io.*;
import java.util.Enumeration;

/**
 * The BufferManager implements an LRU cache of buffers.
 *
 * @see IBufferManager
 */
public class BufferManager implements IBufferManager {

	/**
	 * LRU cache of buffers. The key and value for an entry
	 * in the table is the identical buffer.
	 */
	protected OverflowingLRUCache fOpenBuffers = new BufferCache(20);
	protected static BufferManager fgDefaultBufferManager;
/**
 * Creates a new buffer manager.
 */
public BufferManager() {
}
/**
 * Adds a buffer to the table of open buffers.
 */
protected void addBuffer(IBuffer buffer) {
	fOpenBuffers.put(buffer.getOwner(), buffer);
}
/**
 * @see IBufferManager
 */
public IBuffer getBuffer(IOpenable owner) {
	return (IBuffer)fOpenBuffers.get(owner);
}
/**
 * Returns the default buffer manager.
 * TBD: There shouldn't be a global buffer manager.
 * It should be a registered manager with the workspace.
 */
public synchronized static IBufferManager getDefaultBufferManager() {
	if (fgDefaultBufferManager == null) {
		fgDefaultBufferManager = new BufferManager();
	}
	return fgDefaultBufferManager;
}
/**
 * The <code>Enumeration</code> answered is thread safe.
 *
 * @see OverflowLRUCache
 * @see IBufferManager
 */
public Enumeration getOpenBuffers() {
	synchronized (fOpenBuffers) {
		fOpenBuffers.shrink();
		return fOpenBuffers.elements();
	}
}
/**
 * @see IBufferManager
 */
public IBuffer openBuffer(char[] contents, IProgressMonitor progress, IOpenable owner, boolean readOnly) throws IllegalArgumentException {
	if (contents == null || owner == null) {
		throw new IllegalArgumentException();
	}
	Buffer buffer = new Buffer(this, contents, owner, readOnly);
	addBuffer(buffer);
	return buffer;
}
/**
 * @see IBufferManager
 */
public IBuffer openBuffer(IFile file, IProgressMonitor progress, IOpenable owner, boolean readOnly) throws JavaModelException {
	if (file == null || owner == null) {
		throw new IllegalArgumentException();
	}
	char[] contents = Util.getResourceContentsAsCharArray(file);
	Buffer buffer = new Buffer(this, file, contents, owner, readOnly);
	addBuffer(buffer);
	return buffer;
}
/**
 * Removes a buffer from the table of open buffers.
 */
protected void removeBuffer(IBuffer buffer) {
	fOpenBuffers.remove(buffer.getOwner());
}
/**
 * Returns the given String as a byte array. This is centralized here in case
 * we need to do special conversion.
 */
public static byte[] stringToBytes(String s) {

	return s.getBytes();
	
}
}
