package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;

/**
 * The <code>ISourceFragment</code> represents a fragment of a source.
 * It is used for navigating from a DC object back to the source.
 *
 * For Type position info, use the indexes.  For IProblemDetail
 * position info, use IProblemDetail.getStartPos() / getEndPos()
 *
 * @see IMember#getSourceFragment
 */
public interface ISourceFragment 
{

	/**
	 * Compare two Objects for equality.  Returns true iff they represent the same
	 * source fragment (same start positions, end positions, element identifiers, and
	 * zip entry names).
	 */
	boolean equals(Object obj);
	/**
	 * Returns the path of the fragment.
	 */
	IPath getPath();
	/**
	 * If this object represents a source fragment within a ZIP file, 
	 * this method returns the name of the corresponding ZIP entry.
	 * If this represents a package fragment within a ZIP file, 
	 * this returns the name of the package in the ZIP file, although
	 * there may not be a corresponding entry (i.e. there may only be entries
	 * for the class files within the package).
	 * Otherwise, this returns null.
	 */
	String getZipEntryName();
}
