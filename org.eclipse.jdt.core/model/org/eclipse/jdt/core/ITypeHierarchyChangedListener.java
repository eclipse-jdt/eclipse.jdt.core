package org.eclipse.jdt.core;

public interface ITypeHierarchyChangedListener {
	/**
	 * Notifies that the given type hierarchy has changed in some way and should
	 * be refreshed at some point to make it consistent with the current state of
	 * the Java model.
	 */
	void typeHierarchyChanged(ITypeHierarchy typeHierarchy);
}
