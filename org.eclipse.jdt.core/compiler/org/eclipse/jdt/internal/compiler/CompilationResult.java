package org.eclipse.jdt.internal.compiler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * A compilation result consists of all information returned by the compiler for 
 * a single compiled compilation source unit.  This includes:
 * <ul>
 * <li> the compilation unit that was compiled
 * <li> for each type produced by compiling the compilation unit, its binary and optionally its principal structure
 * <li> any problems (errors or warnings) produced
 * <li> dependency info
 * </ul>
 *
 * The principle structure and binary may be null if the compiler could not produce them.
 * If neither could be produced, there is no corresponding entry for the type.
 *
 * The dependency info includes type references such as supertypes, field types, method
 * parameter and return types, local variable types, types of intermediate expressions, etc.
 * It also includes the namespaces (packages) in which names were looked up.
 * It does <em>not</em> include finer grained dependencies such as information about
 * specific fields and methods which were referenced, but does contain their 
 * declaring types and any other types used to locate such fields or methods.
 */

import org.eclipse.jdt.internal.compiler.env.*;

import java.util.*;

public class CompilationResult {
	public IProblem problems[];
	public int problemCount;
	public ICompilationUnit compilationUnit;

	public char[][] namespaceDependencies;
	public char[][] fileDependencies;
	public int lineSeparatorPositions[];
	public Hashtable compiledTypes = new Hashtable(11);
	public int unitIndex, totalUnitsKnown;
	public boolean hasBeenAccepted = false;
	public char[] fileName;
	public CompilationResult(char[] fileName, int unitIndex, int totalUnitsKnown) {

		this.fileName = fileName;
		this.unitIndex = unitIndex;
		this.totalUnitsKnown = totalUnitsKnown;

	}

	public CompilationResult(
		ICompilationUnit compilationUnit,
		int unitIndex,
		int totalUnitsKnown) {

		this.fileName = compilationUnit.getFileName();
		this.compilationUnit = compilationUnit;
		this.unitIndex = unitIndex;
		this.totalUnitsKnown = totalUnitsKnown;

	}

	public ClassFile[] getClassFiles() {
		Enumeration enum = compiledTypes.elements();
		ClassFile[] classFiles = new ClassFile[compiledTypes.size()];
		int index = 0;
		while (enum.hasMoreElements()) {
			classFiles[index++] = (ClassFile) enum.nextElement();
		}
		return classFiles;
	}

	/**
	 * Answer the initial compilation unit corresponding to the present compilation result
	 */
	public ICompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	/**
	 * Answer the file names of the types on which the compilation unit depends.
	 * For example, if Foo.java refers to class p1.Bar, and p1.Bar is found,
	 * then the type dependency info for Foo.java will include the file name
	 * for p1.Bar.
	 * If a type is looked up in some package but is not found, this does not
	 * introduce a type dependency, but it does introduce a namespace dependency
	 * on that package.
	 * In general, if any of the types listed are deleted from the image, it will 
	 * break the owner of the dependency info.
	 */

	public char[][] getFileDependencies() {
		return fileDependencies;
	}

	/**
	 * Answer the initial file name
	 */
	public char[] getFileName() {
		return fileName;
	}

	/**
	 * Answer the names of the packages on which the compilation result depends.
	 * That is, in order to compile the compilation unit, the compiler needed
	 * to look up names in these packages.  Such dependencies usually arise
	 * from import statements and qualified type references.
	 * The names are qualified package names separated by periods.
	 * For example, {{{java.lang}, {java.io}}}.
	 * The default package is indicated by the char[0].
	 */

	public char[][] getNamespaceDependencies() {
		return namespaceDependencies;
	}

	/**
	 * Answer the problems (errors and warnings) encountered during compilation.
	 *
	 * This is not a compiler internal API - it has side-effects !
	 * It is intended to be used only once all problems have been detected,
	 * and makes sure the problems slot as the exact size of the number of
	 * problems.
	 */
	public IProblem[] getProblems() {

		// Re-adjust the size of the problems if necessary.
		if (problems != null) {
			if (problemCount != problems.length) {
				System.arraycopy(
					problems,
					0,
					(problems = new IProblem[problemCount]),
					0,
					problemCount);
			}

			// Sort problems per source positions.
			quicksort(problems, 0, problems.length - 1);
		}
		return problems;
	}

	public boolean hasErrors() {
		if (problems != null)
			for (int i = 0; i < problemCount; i++) {
				if (problems[i].isError())
					return true;
			}
		return false;
	}

	public boolean hasProblems() {
		return problemCount != 0;
	}

	public boolean hasWarnings() {
		if (problems != null)
			for (int i = 0; i < problemCount; i++) {
				if (problems[i].isWarning())
					return true;
			}
		return false;
	}

	private static void quicksort(IProblem arr[], int left, int right) {
		int i, last, pos;

		if (left >= right) {
			/* do nothing if array contains fewer than two */
			return;
			/* two elements */
		}

		swap(arr, left, (left + right) / 2);
		last = left;
		pos = arr[left].getSourceStart();

		for (i = left + 1; i <= right; i++) {
			if (arr[i].getSourceStart() < pos) {
				swap(arr, ++last, i);
			}
		}

		swap(arr, left, last);
		quicksort(arr, left, last - 1);
		quicksort(arr, last + 1, right);
	}

	/**
	 * For now, remember the compiled type using its compound name.
	 */
	public void record(char[] typeName, ClassFile classFile) {
		compiledTypes.put(typeName, classFile);
	}

	public void record(IProblem newProblem) {
		if (problemCount == 0) {
			problems = new IProblem[5];
		} else {
			if (problemCount == problems.length)
				System.arraycopy(
					problems,
					0,
					(problems = new IProblem[problemCount * 2]),
					0,
					problemCount);
		};
		problems[problemCount++] = newProblem;
	}

	private static void swap(IProblem arr[], int i, int j) {
		IProblem tmp;
		tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp;
	}

	CompilationResult tagAsAccepted() {
		this.hasBeenAccepted = true;
		return this;
	}

}
