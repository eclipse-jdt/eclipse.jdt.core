package org.eclipse.jdt.core;

public interface IElementChangedListener {

	/**
	 * Notifies that one or more attributes of one or more Java elements have changed.
	 * The specific details of the change are described by the given event.
	 *
	 * @param event the change event
	 */
	public void elementChanged(ElementChangedEvent event);
}
