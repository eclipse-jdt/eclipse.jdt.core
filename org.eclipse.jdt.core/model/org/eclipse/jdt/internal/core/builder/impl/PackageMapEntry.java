package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.internal.core.builder.IPackage;

import java.util.Hashtable;

/**
 * These objects are used as entries in the State's package map table.
 */
class PackageMapEntry extends StateTables {
	IPackage fPkg;
	IPath[] fFragments;
	/**
	 * Creates a new package map entry.
	 */
	PackageMapEntry(IPackage pkg) {
		fPkg = pkg;
		fFragments = null;
	}
/**
 * Adds a new package fragment to the entry.
 */
void addFragment(IPath fragment) {
	if (fFragments == null) {
		fFragments = new IPath[] {fragment};
	} else {
		for (int i = 0; i < fFragments.length; ++i) {
			if (fFragments[i].equals(fragment)) {
				return;
			}
		}
		IPath[] newFragments = new IPath[fFragments.length + 1];
		System.arraycopy(fFragments, 0, newFragments, 0, fFragments.length);
		newFragments[fFragments.length] = fragment;
		fFragments = newFragments;
	}
}
	/**
	 * Adds a new package fragment to the entry.
	 */
	void addFragments(IPath[] fragments) {
		if (fFragments == null) {
			fFragments = new IPath[fragments.length];
			System.arraycopy(fragments, 0, fFragments, 0, fragments.length);
		}
		else {
			IPath[] newFragments = new IPath[fFragments.length + fragments.length];
			System.arraycopy(fFragments, 0, newFragments, 0, fFragments.length);
			System.arraycopy(fragments, 0, newFragments, fFragments.length, fragments.length);
			fFragments = newFragments;
		}
	}
	/**
	 * Returns an the fragments in the package
	 */
	IPath[] getFragments() {
		return fFragments;
	}
	/**
	 * Returns the package for which this is the entry.
	 */
	IPackage getPackage() {
		return fPkg;
	}
	/**
	 * Returns a String that represents the value of this object.
	 * This method is for debugging purposes only.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("PackageMapEntry("/*nonNLS*/);
		buf.append(fPkg);
		buf.append(")"/*nonNLS*/);
		return buf.toString();
	}
}
