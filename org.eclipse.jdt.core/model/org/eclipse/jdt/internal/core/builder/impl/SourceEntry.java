package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.core.Util;

import java.io.File;

/**
 * This class encapsulates a reference to an element in the workspace.  This
 * element may be a java source file, a class file, or a zip entry.
 */
public class SourceEntry extends StateTables {
	IPath fPath;
	String fZipEntryPath;
	String fZipEntryFileName;
	/**
	 * Creates a new SourceEntry.
	 */
	public SourceEntry(IPath path, String zipEntryPath, String zipEntryFileName) {
		fPath = path;
		fZipEntryPath = zipEntryPath;
		fZipEntryFileName = zipEntryFileName;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SourceEntry))
			return false;
		SourceEntry entry = (SourceEntry) o;
		if (!this.fPath.equals(entry.fPath))
			return false;
		if (!Util.equalOrNull(fZipEntryPath, entry.fZipEntryPath))
			return false;
		if (!Util.equalOrNull(fZipEntryFileName, entry.fZipEntryFileName))
			return false;
		return true;
	}

	public static SourceEntry fromPathWithZipEntryName(String pathWithZipEntryName) {
		// Convert separators back.  See getPathWithZipEntryName().
		pathWithZipEntryName = pathWithZipEntryName.replace(File.separatorChar, '/');
		int i = pathWithZipEntryName.lastIndexOf('#');
		if (i == -1) {
			return new SourceEntry(new Path(pathWithZipEntryName), null, null);
		} else {
			Path path = new Path(pathWithZipEntryName.substring(0, i));
			String zipEntryName = pathWithZipEntryName.substring(i + 1);
			String zipEntryPath = null, zipEntryFileName = null;
			if (zipEntryName != null) {
				int pos = zipEntryName.lastIndexOf('/');
				if (pos != -1) {
					zipEntryPath = zipEntryName.substring(0, pos);
					zipEntryFileName = zipEntryName.substring(pos + 1);
				} else {
					zipEntryPath = null;
					zipEntryFileName = zipEntryName;
				}
			}
			return new SourceEntry(path, zipEntryPath, zipEntryFileName);
		}
	}

	/**
	 * Returns the 'file name'.
	 */
	public String getFileName() {
		if (fZipEntryFileName != null) {
			return fZipEntryFileName;
		}
		return fPath.lastSegment();
	}

	/**
	 * Returns the 'file name' without the file extension.
	 */
	public String getName() {
		String name = getFileName();
		int lastDot = name.lastIndexOf('.');
		if (lastDot != -1)
			name = name.substring(0, lastDot);
		return name;
	}

	public IPath getPath() {
		return fPath;
	}

	public String getPathWithZipEntryName() {
		String s =
			fZipEntryFileName == null
				? fPath.toString()
				: fPath.toString() + '#' + this.getZipEntryName();
		// Convert separators.
		// See 1FVQGE2: ITPJCORE:ALL - Class file has workbench relative source file attribute
		// and 1FW1LHM: LFCOM:ALL - Source file names and dependency info
		return s.replace('/', File.separatorChar);
	}

	public String getZipEntryName() {
		return fZipEntryPath == null
			? fZipEntryFileName
			: fZipEntryPath + "/" + fZipEntryFileName;
	}

	public int hashCode() {
		int code = fPath.hashCode();
		if (fZipEntryPath != null) {
			code = code * 17 + fZipEntryPath.hashCode();
		}
		if (fZipEntryFileName != null) {
			code = code * 17 + fZipEntryFileName.hashCode();
		}
		return code;
	}

	/**
	 * Returns true if the source entry comes from a binary file, otherwise
	 * returns false.
	 */
	public boolean isBinary() {
		if (fZipEntryFileName != null) {
			return fZipEntryFileName.endsWith(".class");
		}
		String extension = fPath.getFileExtension();
		return extension != null && extension.equalsIgnoreCase("class");
	}

	/**
	 * Returns true if the source entry comes from a compilation unit, otherwise
	 * returns false.
	 */
	public boolean isSource() {
		if (fZipEntryFileName != null) {
			return fZipEntryFileName.endsWith(".java");
		}
		String extension = fPath.getFileExtension();
		return extension != null && extension.equalsIgnoreCase("java");
	}

	/**
	 * Returns a String that represents the value of this object.
	 * @return a string representation of the receiver
	 */
	public String toString() {
		return fZipEntryFileName == null
			? fPath.toString()
			: fPath.toString() + ", entry: " + this.getZipEntryName();
	}

}
