package org.eclipse.jdt.internal.core.index;

public interface IQueryResult {
	String getPath();
	String getProperty(String propertyName);
	java.util.Enumeration getPropertyNames();
	String propertiesToString();
}
