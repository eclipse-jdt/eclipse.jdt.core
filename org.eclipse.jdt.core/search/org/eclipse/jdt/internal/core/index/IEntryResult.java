package org.eclipse.jdt.internal.core.index;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

public interface IEntryResult {
public int[] getFileReferences();
public char[] getWord();
}
