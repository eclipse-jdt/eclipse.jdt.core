package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.builder.IPackage;
import java.util.*;
import org.eclipse.jdt.internal.core.util.LookupTable;

/**
 * A PathMap is essentially a reverse-indexed package map.  It maps
 * from IPaths to a IPackage[], the packages contributed by that path.
 * In most cases a path contributes only one package, but if the path
 * happens to be a zip, it can contribute many package fragments.
 */
class PathMap extends StateTables {
	/**
	 * Make it a dictionary so the implementation can be either
	 * a LookupTable or a Hashtable.
	 */
	Dictionary fTable;

	/**
	 * Creates a new path map given a package map.  The package map is
	 * from IPackage -> IPath[].  This creates a path map that maps
	 * from IPath -> IPackage[]
	 */
	PathMap(PackageMap pkgMap) {
		fTable = packageMapToPathMap(pkgMap);
	}

	/**
	 * Returns the paths which are the keys in the path map.
	 */
	IPath[] getPaths() {
		IPath[] paths = new IPath[fTable.size()];
		int count = 0;
		for (Enumeration e = fTable.keys(); e.hasMoreElements();) {
			paths[count++] = (IPath) e.nextElement();
		}
		return paths;
	}

	/**
	 * Returns whether the path map contains the given path as a key.
	 */
	boolean hasPath(IPath path) {
		return fTable.get(path) != null;
	}

	/**
	 * Returns the package handle for the given package resource.  
	 * Throws an internal error if the path was inappropriate.
	 */
	IPackage packageHandleFromPath(IPath path) {
		IPackage[] pkgs = (IPackage[]) fTable.get(path);
		if (pkgs == null) {
			throw new Error("Attempt to access packages for non-existent path:" + path);
		}
		if (pkgs.length != 1) {
			throw new Error("Didn't get exactly one package for " + path);
		}
		return pkgs[0];
	}

	/**
	 * Returns the package handles for the given package resource.  
	 */
	IPackage[] packageHandlesFromPath(IPath path) {
		IPackage[] pkgs = (IPackage[]) fTable.get(path);
		return pkgs != null ? pkgs : new IPackage[0];
	}

	/**
	 * Creates and returns new path map given a package map.  The package map is
	 * from IPackage -> IPath[].  The new path map is from IPath -> IPackage[]
	 */
	protected Dictionary packageMapToPathMap(PackageMap pkgMap) {
		LookupTable table = new LookupTable();

		/* first generate a path map using vectors */
		for (Enumeration e = pkgMap.getAllPackages(); e.hasMoreElements();) {
			IPackage pkg = (IPackage) e.nextElement();
			IPath[] paths = pkgMap.getFragments(pkg);

			/* add entries in the path map for each fragment */
			for (int i = 0; i < paths.length; i++) {
				Vector v = (Vector) table.get(paths[i]);
				if (v == null) {
					/* most common case is one path per package */
					v = new Vector(1);
					table.put(paths[i], v);
				}
				v.addElement(pkg);
			}
		}

		/* convert vectors to arrays */
		for (Enumeration e = table.keys(); e.hasMoreElements();) {
			IPath path = (IPath) e.nextElement();
			Vector v = (Vector) table.get(path);
			IPackage[] pkgs = new IPackage[v.size()];
			v.copyInto(pkgs);
			table.put(path, pkgs);
		}
		return table;
	}

	/**
	 * For debugging only.
	 */
	public String toString() {
		ArrayList list = new ArrayList();
		for (Enumeration e = fTable.keys(); e.hasMoreElements();) {
			IPath path = (IPath) e.nextElement();
			list.add(path);
		}
		Collections.sort(list, StateImpl.getPathComparator());
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString() + ":\n");
		for (int i = 0; i < list.size(); ++i) {
			IPath path = (IPath) list.get(i);
			sb.append("  " + path + ": ");
			IPackage[] pkgs = packageHandlesFromPath(path);
			if (pkgs.length == 1) {
				sb.append(pkgs[0].getName());
			} else {
				Arrays.sort(pkgs, StateImpl.getPackageComparator());
				sb.append("(" + pkgs.length + " packages)");
				for (int j = 0; j < pkgs.length; ++j) {
					sb.append("\n    ");
					sb.append(pkgs[j].getName());
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

}
