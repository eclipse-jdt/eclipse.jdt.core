package org.eclipse.jdt.internal.core.index;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

public interface IQueryResult {
	String getPath();
	String getProperty(String propertyName);
	java.util.Enumeration getPropertyNames();
	String propertiesToString();
}
