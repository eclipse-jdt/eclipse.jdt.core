package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.core.Util;

import java.util.Hashtable;

public class ImageContextImpl implements IImageContext {
	private IPackage[] fPackages;
	private IDevelopmentContext fDevContext;
	/**
	 * Creates a new ImageContext.
	 */
	public ImageContextImpl(IDevelopmentContext context, IPackage[] packages) {
		fDevContext = context;
		fPackages = packages;
	}
	/**
	 * Returns true if the image context contains the given package, false otherwise.
	 */
	public boolean containsPackage(IPackage pkg) {
		for (int i = 0, len = fPackages.length; i < len; ++i) {
			if (fPackages[i].equals(pkg)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if the given image context is equal to this one.  Returns
	 * false otherwise.
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ImageContextImpl)) return false;

		ImageContextImpl ctx = (ImageContextImpl)o;

		if (!ctx.fDevContext.equals(fDevContext)) return false;
		int pkgCount = fPackages.length;
		if (ctx.fPackages.length != pkgCount) return false;

		/* place packages of this in hashtable to avoid n squared search */
		Hashtable pkgTable = new Hashtable(pkgCount * 2 + 1);
		for (int i = 0; i < pkgCount; i++) {
			pkgTable.put(fPackages[i], fPackages[i]);
		}

		/* check packages of other context for inclusion in this context */
		IPackage[] otherPkgs = ctx.getPackages();
		for (int i = 0; i < pkgCount; i++) {
			if (!pkgTable.contains(otherPkgs[i])) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Returns the development context for this image context.
	 */
	public IDevelopmentContext getDevelopmentContext() {
		return fDevContext;
	}
	/**
	 * Returns the packages in this image context.  The packages will always
	 * be non state-specific.
	 */
	public IPackage[] getPackages() {
		return fPackages;
	}
	/**
	 * Returns true if all packages in this image context appear in the given one.
	 */
	public boolean isSubsetOf(ImageContextImpl other) {
		if (this == other) return true;
		if (Util.equalArraysOrNull(fPackages, other.fPackages)) return true;
		for (int i = 0; i < fPackages.length; ++i) {
			if (!other.containsPackage(fPackages[i])) return false;
		}
		return true;
	}
	/**
	 * Return a string describing the image context.  This is for debugging
	 * purposes only.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ImageContext with packages: \n"); //$NON-NLS-1$
		for (int i = 0; i < fPackages.length; i++)
			buf.append("	" + fPackages[i].getName() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}
}
