package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.builder.IPackage;
import java.util.*;

/**
 * The state's package map.
 */
public class PackageMap extends StateTables {
	Hashtable fTable;
	/**
	 * Creates a new package map
	 */
	PackageMap() {
		fTable = new Hashtable(23);
	}
	/**
	 * Returns true if the package exists in the state, otherwise
	 * returns false
	 */
	boolean containsPackage(IPackage pkg) {
		return fTable.containsKey(pkg);
	}
	/**
	 * Creates a copy of the package map.
	 */
	PackageMap copy() {
		try {
			PackageMap copy = (PackageMap) super.clone();
			copy.fTable = (Hashtable) fTable.clone();
			return copy;
		}
		catch (CloneNotSupportedException e) {
			// Should not happen.
			throw new Error();
		}
	}
	/**
	 * Returns an enumeration of all packages in the state.  The enumeration
	 * is of non state-specific package handles.
	 */
	public Enumeration getAllPackages() {
		return fTable.keys();
	}
	/**
	 * Returns all packages in the state.  The result is an array
	 * of non state-specific package handles.
	 */
	public IPackage[] getAllPackagesAsArray() {
		IPackage[] pkgs = new IPackage[fTable.size()];
		int i = 0;
		for (Enumeration e = fTable.keys(); e.hasMoreElements();) {
			pkgs[i++] = (IPackage) e.nextElement();
		}
		return pkgs;
	}
	/**
	 * Returns the package map entry for a given package.
	 * Returns null if the package is not present.
	 */
	PackageMapEntry getEntry(IPackage pkg) {
		return (PackageMapEntry)fTable.get(pkg);
	}
	/**
	 * Returns the package fragments for a given package.  The returned
	 * fragments are sorted according to the class path.
	 * Returns null if the package is not present.
	 */
	IPath[] getFragments(IPackage pkg) {
		PackageMapEntry entry = (PackageMapEntry)fTable.get(pkg);
		return entry == null ? null : entry.getFragments();
	}
	/**
	 * Adds a fragment entry for the given package.
	 * Mutable operation, should only be used when recreating package map.
	 */
	void putFragment(IPackage pkg, IPath frag) {
		PackageMapEntry entry = (PackageMapEntry)fTable.get(pkg);
		if (entry == null) {
			entry = new PackageMapEntry(pkg);
			fTable.put(pkg, entry);
		}
		entry.addFragment(frag);
	}
	/**
	 * Adds an array of fragments for the given package.  Assumes that the
	 * given array of fragments are in classpath order.
	 */
	void putFragments(IPackage pkg, IPath[] frags) {
		PackageMapEntry entry = (PackageMapEntry)fTable.get(pkg);
		if (entry == null) {
			entry = new PackageMapEntry(pkg);
			fTable.put(pkg, entry);
		}
		entry.addFragments(frags);
	}
	/**
	 * Returns the number of packages in the state.  
	 */
	int size() {
		return fTable.size();
	}
	/**
	 * For debugging only.
	 */
	public String toString() {
		IPackage[] pkgs = getAllPackagesAsArray();
		Arrays.sort(pkgs, StateImpl.getPackageComparator());
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString() + ":\n"); //$NON-NLS-1$
		for (int i = 0; i < pkgs.length; ++i) {
			sb.append("  " + pkgs[i].getName() + ": "); //$NON-NLS-2$ //$NON-NLS-1$
			IPath[] fragments = getFragments(pkgs[i]);
			for (int j = 0; j < fragments.length; ++j) {
				if (j != 0) sb.append(", "); //$NON-NLS-1$
				sb.append(fragments[j]);
			}
			sb.append("\n"); //$NON-NLS-1$
		}
		return sb.toString();
	}
}
