package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.index.*;
import java.util.*;

/**
 * The properties of a document are stored into a hashtable.
 * @see IDocument
 */

public abstract class PropertyDocument implements IDocument {
	protected Hashtable properties;
	public PropertyDocument() {
		properties= new Hashtable(5);
	}
	/**
	 * @see IDocument#getProperty
	 */
	public String getProperty(String property) {
		return (String) properties.get(property);
	}
	/**
	 * @see IDocument#getPropertyNames
	 */

	public Enumeration getPropertyNames() {
		return properties.keys();
	}
	/**
	 * @see IDocument#setProperty
	 */

	public void setProperty(String property, String value) {
		properties.put(property, value);
	}
}
