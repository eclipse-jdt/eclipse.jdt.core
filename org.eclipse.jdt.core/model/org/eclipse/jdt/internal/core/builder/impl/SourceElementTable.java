package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.core.util.LookupTable;

import java.util.Enumeration;

/**
 * The source element table contains all elements of the workspace that are
 * visible to the image builder.  It is implemented as nested hashtables.  The
 * first hashtable is keyed by non state-specific package handle.  The second
 * table is keyed by name of source file name (e.g., "Object.class", or "Foo.java"),
 * and has SourceEntry objects as values.
 */
class SourceElementTable extends StateTables{
	LookupTable fPackageTable = new LookupTable(11);
	/**
	 * Returns true if the package is in the table, false otherwise.
	 */
	boolean containsPackage(IPackage pkg) {
		return fPackageTable.containsKey(pkg);
	}
	/**
	 * Creates a copy of the table.
	 */
	SourceElementTable copy() {
		try {
			SourceElementTable copy = (SourceElementTable) super.clone();
			copy.fPackageTable = new LookupTable(fPackageTable.size() * 2 + 1);
			for (Enumeration e = fPackageTable.keys(); e.hasMoreElements();) {
				IPackage pkg = (IPackage) e.nextElement();
				LookupTable pkgTable = (LookupTable) fPackageTable.get(pkg);
				copy.fPackageTable.put(pkg, pkgTable.clone());
			}
			return copy;
		}
		catch (CloneNotSupportedException e) {
			// Should not happen.
			throw new Error();
		}
	}
	/**
	 * Returns the table for a package.  Returns null if no such table exists.
	 */
	LookupTable getPackageTable(IPackage pkg) {
		return (LookupTable) fPackageTable.get(pkg);
	}
/**
 * Returns the source entries in the given package.  
 * Returns null if no entries exist for that package.
 */
SourceEntry[] getSourceEntries(IPackage pkg) {
	LookupTable pkgTable = getPackageTable(pkg);
	if (pkgTable == null) {
		return null;
	}
	int i = 0;
	SourceEntry[] results = new SourceEntry[pkgTable.size()];
	for (Enumeration e = pkgTable.elements(); e.hasMoreElements();) {
		results[i++] = (SourceEntry) e.nextElement();
	}
	return results;
}
/**
 * Returns the source entry for a package and file name.  Returns null if
 * no entry exists.
 */
SourceEntry getSourceEntry(IPackage pkg, String fileName) {
	/* make sure package is not state specific */
	Assert.isTrue(!pkg.isStateSpecific());
	LookupTable pkgTable = getPackageTable(pkg);
	if (pkgTable != null) {
		return (SourceEntry) pkgTable.get(fileName);
	}
	return null;
}
	/**
	 * Returns the number of packages in the table.  
	 */
	int numPackages() {
		return fPackageTable.size();
	}
	/**
	 * Adds the table for a package to the table.
	 */
	void putPackageTable(IPackage pkg, LookupTable pkgTable) {
		fPackageTable.put(pkg, pkgTable);
	}
	/**
	 * Adds one source entry in the source element table
	 */
	public void putSourceEntry(IPackage pkg, SourceEntry sourceEntry) {
		LookupTable pkgTable = getPackageTable(pkg);
		if (pkgTable == null){
			putPackageTable(pkg, pkgTable = new LookupTable());
		}
		pkgTable.put(sourceEntry.getFileName(), sourceEntry);
	}
	/**
	 * Removes the source entries for a package.
	 */
	void removePackage(IPackage pkg) {
		fPackageTable.remove(pkg);
	}
/**
 * Removes the source entry for a source element.  Returns the
 * removed element or null if it didn't exist
 */
SourceEntry removeSourceEntry(IPackage pkg, String fileName) {
	LookupTable pkgTable = getPackageTable(pkg);
	if (pkgTable != null) {
		return (SourceEntry) pkgTable.remove(fileName);
	}
	return null;
}
	/**
	 * Returns a String that represents the value of this object.
	 * @return a string representation of the receiver
	 */
	public String toString() {
		return "SourceElementTable("/*nonNLS*/ + fPackageTable + ")"/*nonNLS*/;
	}
}
