package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.*;

import java.util.Vector;

public class ConvertedCompilationResult {
	PackageElement fPackageElement;
	Vector fDependencies;
	IProblemDetail[] fProblems;
	TypeStructureEntry[] fTypes;

ConvertedCompilationResult(
	PackageElement packageElement,
	Vector dependencies,
	IProblemDetail[] problems,
	TypeStructureEntry[] types) {

	fPackageElement = packageElement;
	fDependencies = dependencies;
	fProblems = problems;
	fTypes = types;
}
	Vector getDependencies() {
		return fDependencies;
	}
	PackageElement getPackageElement() {
		return fPackageElement;
	}
	IProblemDetail[] getProblems() {
		return fProblems;
	}
	TypeStructureEntry[] getTypes() {
		return fTypes;
	}
	public String toString() {
		return (fProblems.length == 0 ? "" : "*") + "ConvertedCompilationResult(" + fPackageElement + ")";
	}
}
