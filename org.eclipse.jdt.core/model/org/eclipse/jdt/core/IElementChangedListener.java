package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;

/**
 * An element changed listener receives notification of changes to Java elements
 * maintained by the Java model.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IElementChangedListener {
	
/**
 * Notifies that one or more attributes of one or more Java elements have changed.
 * The specific details of the change are described by the given event.
 *
 * @param event the change event
 */
public void elementChanged(ElementChangedEvent event);
}
