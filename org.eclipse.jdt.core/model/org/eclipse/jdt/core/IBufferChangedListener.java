package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*; 

/**
 * A listener which gets notified when the contents of a specific buffer
 * have changed, or when the buffer is closed.
 * When a buffer is closed, the listener is notified <em>after</em> the buffer has been closed.
 * A listener is not notified when a buffer is saved.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IBufferChangedListener {

	/** 
	 * Notifies that the given event has occurred.
	 *
	 * @param event the change event
	 */
	public void bufferChanged(BufferChangedEvent event);
}
