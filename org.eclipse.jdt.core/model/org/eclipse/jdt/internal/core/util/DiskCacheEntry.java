package org.eclipse.jdt.internal.core.util;

public class DiskCacheEntry {
	String fKey;
	int fSize;
	String fFileName;
	long fLastAccess;
	byte[] fExtraInfo;
	/**
	 * Create an entry.
	 */
	DiskCacheEntry(String key, int size, String fileName, long lastAccess, byte[] extraInfo) {
		fKey = key;
		fSize = size;
		fFileName = fileName;
		fLastAccess = lastAccess;
		fExtraInfo = extraInfo;
	}
	/**
	 * Returns the extra info associated with this entry,
	 * or null if there is no extra info.
	 */
	public byte[] getExtraInfo() {
		return fExtraInfo;
	}
	/**
	 * Returns the file name of the entry.
	 * This name is assigned by the cache and is, strictly speaking,
	 * part of its internal representation.  It is included in the API
	 * since a client may want to expose the file name for an element.
	 */
	public String getFileName() {
		return fFileName;
	}
	/**
	 * Returns the key for this entry.
	 * The key was used to look up this entry.
	 */
	public String getKey() {
		return fKey;
	}
	/**
	 * Returns the time that this entry was last accessed,
	 * in milliseconds since Jan 1, 1970.
	 */
	public long getLastAccess() {
		return fLastAccess;
	}
	/**
	 * Returns the size of the entry in bytes.
	 */
	public int getSize() {
		return fSize;
	}
}
