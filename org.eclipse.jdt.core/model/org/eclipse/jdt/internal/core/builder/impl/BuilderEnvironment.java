package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.core.builder.*;
import java.io.IOException;
import java.util.*;

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.core.Util;

/**
 * This is the main interface between the image builder and the compiler.
 */
public class BuilderEnvironment implements INameEnvironment {
	protected AbstractImageBuilder fBuilder;
	protected IDevelopmentContext fDevelopmentContext;
	protected StateImpl fState;
	protected Hashtable fPackages;
	protected IPackage fDefaultPackage = null;
	protected BuildNotifier fNotifier = null;
public BuilderEnvironment(AbstractImageBuilder builder) {
	fBuilder = builder;
	fState = (StateImpl) fBuilder.getNewState();
	fDevelopmentContext = fState.getDevelopmentContext();
}
protected void checkCancel() {
	if (fNotifier != null) {
		fNotifier.checkCancelWithinCompiler();
	}
}
/**
 * Notifies interested listeners that a compilation unit is
 * going to be compiled.
 */
protected void compiling(CompilerCompilationUnit unit) {
	fBuilder.compiling(unit);
}
	/**
	 * Create a table keyed by package name, including all package prefixes.
	 * If it's a real package, it maps to the handle, otherwise to null.
	 */
	void createPackageTable() {
		Hashtable table = new Hashtable();
		if (fDefaultPackage != null) {
			table.put(IPackageFragment.DEFAULT_PACKAGE_NAME, fDefaultPackage);
		}
		PackageMap packageMap = fState.getPackageMap();
		for (Enumeration e = packageMap.getAllPackages(); e.hasMoreElements();) {
			IPackage pkg = (IPackage)e.nextElement();
			if (!pkg.isUnnamed()) {
				String name = pkg.getName();
				table.put(name, pkg);
				int i = -1;
				while ((i = name.indexOf('.', i + 1)) != -1) {
					String prefix = name.substring(0, i);
					if (!table.containsKey(prefix)) { // don't overwrite if there's a real package with the same prefix
						table.put(prefix, prefix);
					}
				}
			}
		}
		fPackages = table;
	}
/**
 * @see IBuilderEnvironment
 */
protected NameEnvironmentAnswer find(String packageName, String simpleTypeName) {
	checkCancel();
	IPackage pkg = getPackageHandle(packageName);
	if (pkg == null) {
		return null;
	}
	IType type = pkg.getClassHandle(simpleTypeName);
	SourceEntry sEntry = null;
	TypeStructureEntry tsEntry = fState.getTypeStructureEntry(type, false);
	boolean isInvalid;
	if (tsEntry != null) {
		sEntry = tsEntry.getSourceEntry();
		isInvalid = fBuilder.isInvalid(sEntry);
		if (!isInvalid) {
			/* don't want to invoke a lazy build here */
			IBinaryType binaryType = fState.getBinaryTypeOrNull(tsEntry);
			if (binaryType != null) {
				return new NameEnvironmentAnswer(binaryType);
			}
		}
	} else {
		sEntry = fState.getSourceEntry(type);
		if (sEntry == null) {
			return null;
		}
		isInvalid = fBuilder.isInvalid(sEntry);
	}
	if (sEntry != null && sEntry.isSource()) {
		// only accept if really a compilation unit, not from binary
		// If the type was unknown (as opposed to invalid), check to see if there are problems.
		// If there was no principal structure and there are problems,
		// then the compilation unit has tried to be compiled and it has failed,
		// so don't try again.
		// If the type was known, but needs to be recompiled because its principal structure was missing,
		// then recompile it regardless of whether it has problems.
		if (!isInvalid && (tsEntry == null && fState.getProblemReporter().hasProblems(sEntry))) {
			return null;
		}
		CompilerCompilationUnit unit = new CompilerCompilationUnit(fState, sEntry, fNotifier);
		compiling(unit);
		return new NameEnvironmentAnswer(unit);
	}
	return null;
}
/**
 * @see INameEnvironment
 */
public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
	int last = compoundTypeName.length - 1;
	char[][] pkgName = new char[last][];
	System.arraycopy(compoundTypeName, 0, pkgName, 0, last);
	return findType(compoundTypeName[last], pkgName);
}
/**
 * @see INameEnvironment
 */
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	return find(Util.toString(packageName), Util.toString(typeName));
}
	/**
	 * Return the default package or null.
	 */
	public IPackage getDefaultPackage() {
		return fDefaultPackage;
	}
	/**
	 * Internal - Returns the package handle corresponding to the given package name.
	 */
	IPackage getPackageHandle(String packageName) {
		
		if (fPackages == null) {
			createPackageTable();
		}
		Object o = fPackages.get(packageName);
		if (o == null || !(o instanceof IPackage)) { // value is a string if it's only a package prefix
			return null;
		}
		return (IPackage) o;
	}
/**
 * @see INameEnvironment
 */
public boolean isPackage(char[][] parentPackageName, char[] packageName) {
	checkCancel();
	if (fPackages == null) {
		createPackageTable();
	}
	String fullName = Util.toString(parentPackageName, packageName);
	return fPackages.containsKey(fullName);
}
	/**
	 * Set the default package or null.  The compiler knows about at most one default package.
	 */
	public void setDefaultPackage(IPackage pkg) {
		if (!Util.equalOrNull(fDefaultPackage, pkg)) {
			fPackages = null;  // clear the table; it's reinitialized lazily
		}
		if (pkg.isStateSpecific()) {
			fDefaultPackage = (IPackage)pkg.nonStateSpecific();
		} else {
			fDefaultPackage = pkg;
		}
	}
	/**
	 * Set the notifier.
	 */
	public void setNotifier(BuildNotifier notifier) {
		fNotifier = notifier;
	}
}
