/*******************************************************************************
* Copyright (c) 2024 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.jdt.internal.javac;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

public class JavacCompilationResult extends CompilationResult {
	private Set<String[]> javacQualifiedReferences = new TreeSet<>((a, b) -> Arrays.compare(a, b));
	private Set<String> javacSimpleNameReferences = new TreeSet<>();
	private Set<String> javacRootReferences = new TreeSet<>();
	private boolean isMigrated = false;

	public JavacCompilationResult(ICompilationUnit compilationUnit) {
		this(compilationUnit, 0, 0, Integer.MAX_VALUE);
	}

	public JavacCompilationResult(ICompilationUnit compilationUnit, int unitIndex, int totalUnitsKnown,
			int maxProblemPerUnit) {
		super(compilationUnit, unitIndex, totalUnitsKnown, maxProblemPerUnit);
	}

	public boolean addQualifiedReference(String[] qualifiedReference) {
		return this.javacQualifiedReferences.add(qualifiedReference);
	}

	public boolean addSimpleNameReference(String simpleNameReference) {
		return this.javacSimpleNameReferences.add(simpleNameReference);
	}

	public boolean addRootReference(String rootReference) {
		return this.javacRootReferences.add(rootReference);
	}

	public void migrateReferenceInfo() {
		if (isMigrated) {
			return;
		}

		this.simpleNameReferences = this.javacSimpleNameReferences.stream().map(String::toCharArray).toArray(char[][]::new);
		this.rootReferences = this.javacRootReferences.stream().map(String::toCharArray).toArray(char[][]::new);
		this.qualifiedReferences = this.javacQualifiedReferences.stream().map(qualifiedNames -> {
			// convert String[] to char[][]
			return Stream.of(qualifiedNames).map(String::toCharArray).toArray(char[][]::new);
		}).toArray(char[][][]::new);

		this.javacSimpleNameReferences.clear();
		this.javacRootReferences.clear();
		this.javacQualifiedReferences.clear();
		this.isMigrated = true;
	}
}
