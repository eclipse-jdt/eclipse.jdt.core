package org.eclipse.jdt.internal.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;

/**
 * An interface useful for debugging.  
 * Implementors know how to dump their internal state in a human-readable way
 * to a Dumper.
 *
 * @see Dumper
 */
public interface IDumpable {
	/**
	 * Dumps the internal state of this object in a human-readable way.
	 */
	public void dump(Dumper dumper);
}
