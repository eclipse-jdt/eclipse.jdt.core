package org.eclipse.jdt.internal.core.index;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.index.impl.*;
import java.io.*;

public class IndexFactory {

	public static IIndex newIndex(File indexDirectory) throws IOException {
		return new Index(indexDirectory);
	}
	public static IIndex newIndex(File indexDirectory, String indexName) throws IOException {
		return new Index(indexDirectory, indexName);
	}
	public static IIndex newIndex(String indexName) throws IOException {
		return new Index(indexName);
	}
	public static IIndex newIndex(String indexName, String toString) throws IOException {
		return new Index(indexName, toString);
	}
}
