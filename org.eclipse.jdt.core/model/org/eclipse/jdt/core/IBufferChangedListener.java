package org.eclipse.jdt.core;

public interface IBufferChangedListener {

	/** 
	 * Notifies that the given event has occurred.
	 *
	 * @param event the change event
	 */
	public void bufferChanged(BufferChangedEvent event);
}
