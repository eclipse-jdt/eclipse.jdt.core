package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.internal.core.builder.ISourceFragment;
import org.eclipse.jdt.internal.core.Util;

public class SourceFragmentImpl implements ISourceFragment {
	private int fStartPos, fEndPos;
	private IPath fPath;
	private String fZipEntryName;
	public SourceFragmentImpl (int start, int end, IPath path) {
		fStartPos = start;
		fEndPos = end;
		fPath = path;
	}
	public SourceFragmentImpl (int start, int end, SourceEntry sourceEntry) {
		fStartPos = start;
		fEndPos = end;
		fPath = sourceEntry.getPath();
		fZipEntryName = sourceEntry.getZipEntryName();
	}
	public SourceFragmentImpl (SourceEntry sourceEntry) {
		this(-1, -1, sourceEntry);
	}
	/**
	 * Compares this object against the specified object.
	 *	Returns true if the objects are the same.
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SourceFragmentImpl)) return false;

		SourceFragmentImpl frag = (SourceFragmentImpl) o;
		if (this.fStartPos != frag.fStartPos ||	this.fEndPos != frag.fEndPos)
			return false;
		if (!Util.equalOrNull(this.fPath, frag.fPath))
			return false;
		return Util.equalOrNull(this.fZipEntryName, frag.fZipEntryName);
	}
public IPath getPath() {
	return fPath;
}
/**
 * getZipEntryName method comment.
 */
public String getZipEntryName() {
	return fZipEntryName;
}
	/**
	 * Returns a consistent hashcode for this source fragment
	 */
	public int hashCode() {
		return fStartPos + fEndPos + fPath.hashCode() + fZipEntryName.hashCode();
	}
	/**
	 * Returns a string representation of the receiver.
	 */
	public String toString() {
		return 
			"SourceFragmentImpl("/*nonNLS*/ + fPath 
				+ (fZipEntryName == null ? ""/*nonNLS*/ : " : "/*nonNLS*/ + fZipEntryName) + ")"/*nonNLS*/;
	}
}
