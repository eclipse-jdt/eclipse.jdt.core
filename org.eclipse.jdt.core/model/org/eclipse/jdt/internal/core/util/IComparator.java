package org.eclipse.jdt.internal.core.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * An interface for comparing two objects according to a partial order.
 */
public interface IComparator {
	/**
	 * Returns an integer < 0 if o1 is less than o2, > 0 if o1 is greater
	 * than o2, and 0 if o1 equals o2.
	 */
	int compare(Object o1, Object o2);
}
