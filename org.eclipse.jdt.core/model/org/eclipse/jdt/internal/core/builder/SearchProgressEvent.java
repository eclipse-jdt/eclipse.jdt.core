package org.eclipse.jdt.internal.core.builder;

public class SearchProgressEvent extends java.util.EventObject {
	String name;
	/**
	 * Creates a new <code>SearchProgressEvent</code> with the fully-qualified 
	 * name of the package or type that is now being searched.
	 */
	public SearchProgressEvent(String name) {
		super(name);
		this.name = name;
	}

	/**
	 * Returns the name of the package or type that this
	 * progress event represents.
	 */
	public String getName() {
		return name;
	}

}
