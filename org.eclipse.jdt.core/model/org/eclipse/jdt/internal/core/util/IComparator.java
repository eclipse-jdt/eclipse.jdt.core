package org.eclipse.jdt.internal.core.util;

public interface IComparator {
	/**
	 * Returns an integer < 0 if o1 is less than o2, > 0 if o1 is greater
	 * than o2, and 0 if o1 equals o2.
	 */
	int compare(Object o1, Object o2);
}
