package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.*;

/**
 * This identifies an element of a package in the workspace,
 * before building but after fragment assembly.
 * It consists of two parts: a package handle and a file name (represented as a source entry).
 */
public class PackageElement {
	IPackage fPackage;
	String fFileName;
	boolean fIsSource;
	PackageElement(IPackage pkg, String fileName, boolean isSource) {
		fPackage = pkg;
		fFileName = fileName;
		fIsSource = isSource;
	}

	PackageElement(IPackage pkg, SourceEntry entry) {
		fPackage = pkg;
		fFileName = entry.getFileName();
		fIsSource = entry.isSource();
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PackageElement))
			return false;
		PackageElement e = (PackageElement) o;

		/* If filenames are equal, so is isSource flag.
		 */
		return this.fFileName.equals(e.fFileName) && fPackage.equals(e.fPackage);
	}

	/**
	 * Returns the 'file name'.
	 */
	public String getFileName() {
		return fFileName;
	}

	/**
	 * Returns the package containing the package element.
	 */
	public IPackage getPackage() {
		return fPackage;
	}

	public int hashCode() {
		return fPackage.hashCode() * 17 + fFileName.hashCode();
	}

	/**
	 * Returns true if the source entry comes from a class file, otherwise
	 * returns false.
	 */
	public boolean isBinary() {
		return !fIsSource;
	}

	/**
	 * Returns true if the source entry comes from a compilation unit, otherwise
	 * returns false.
	 */
	public boolean isSource() {
		return fIsSource;
	}

	public String toString() {
		return fPackage.isUnnamed() ? fFileName : fPackage.getName() + '/' + fFileName;
	}

}
