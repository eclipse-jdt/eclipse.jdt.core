package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;

/**
 * A listener which gets notified when a particular type hierarchy object
 * changes.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface ITypeHierarchyChangedListener {
/**
 * Notifies that the given type hierarchy has changed in some way and should
 * be refreshed at some point to make it consistent with the current state of
 * the Java model.
 */
void typeHierarchyChanged(ITypeHierarchy typeHierarchy);
}
