/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler;

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

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

import java.util.*;

public class CompilationResult {
	
	public IProblem problems[];
	public IProblem tasks[];
	public int problemCount;
	public int taskCount;
	public ICompilationUnit compilationUnit;
	private Map problemsMap;
	private Map firstErrorsMap;
	private int maxProblemPerUnit;
	public char[][][] qualifiedReferences;
	public char[][] simpleNameReferences;

	public int lineSeparatorPositions[];
	public Hashtable compiledTypes = new Hashtable(11);
	public int unitIndex, totalUnitsKnown;
	public boolean hasBeenAccepted = false;
	public char[] fileName;
	public boolean hasInconsistentToplevelHierarchies = false; // record the fact some toplevel types have inconsistent hierarchies
	
	public CompilationResult(
		char[] fileName,
		int unitIndex, 
		int totalUnitsKnown,
		int maxProblemPerUnit){
	
		this.fileName = fileName;
		this.unitIndex = unitIndex;
		this.totalUnitsKnown = totalUnitsKnown;
		this.maxProblemPerUnit = maxProblemPerUnit;
	}
	
	public CompilationResult(
		ICompilationUnit compilationUnit,
		int unitIndex, 
		int totalUnitsKnown,
		int maxProblemPerUnit){
	
		this.fileName = compilationUnit.getFileName();
		this.compilationUnit = compilationUnit;
		this.unitIndex = unitIndex;
		this.totalUnitsKnown = totalUnitsKnown;
		this.maxProblemPerUnit = maxProblemPerUnit;
	}

	private int computePriority(IProblem problem){
	
		final int P_STATIC = 10000;
		final int P_OUTSIDE_METHOD = 40000;
		final int P_FIRST_ERROR = 20000;
		final int P_ERROR = 100000;
		
		int priority = 10000 - problem.getSourceLineNumber(); // early problems first
		if (priority < 0) priority = 0;
		if (problem.isError()){
			priority += P_ERROR;
		}
		ReferenceContext context = problemsMap == null ? null : (ReferenceContext) problemsMap.get(problem);
		if (context != null){
			if (context instanceof AbstractMethodDeclaration){
				AbstractMethodDeclaration method = (AbstractMethodDeclaration) context;
				if (method.isStatic()) {
					priority += P_STATIC;
				}
			} else {
			priority += P_OUTSIDE_METHOD;
			}
		} else {
			priority += P_OUTSIDE_METHOD;
		}
		if (firstErrorsMap.containsKey(problem)){
			priority += P_FIRST_ERROR;
		}
		return priority;
	}

	
	public IProblem[] getAllProblems() {
		IProblem[] onlyProblems = this.getProblems();
		int onlyProblemCount = onlyProblems != null ? onlyProblems.length : 0;
		IProblem[] onlyTasks = this.getTasks();
		int onlyTaskCount = onlyTasks != null ? onlyTasks.length : 0;
		if (onlyTaskCount == 0) {
			return onlyProblems;
		}
		if (onlyProblemCount == 0) {
			return onlyTasks;
		}

		int totalNumberOfProblem = onlyProblemCount + onlyTaskCount;
		IProblem[] allProblems = new IProblem[totalNumberOfProblem];
		int allProblemIndex = 0;
		int taskIndex = 0;
		int problemIndex = 0;
		while (taskIndex + problemIndex < totalNumberOfProblem) {
			IProblem nextTask = null;
			IProblem nextProblem = null;
			if (taskIndex < onlyTaskCount) {
				nextTask = onlyTasks[taskIndex];
			}
			if (problemIndex < onlyProblemCount) {
				nextProblem = onlyProblems[problemIndex];
			}
			// select the next problem
			IProblem currentProblem = null;
			if (nextProblem != null) {
				if (nextTask != null) {
					if (nextProblem.getSourceStart() < nextTask.getSourceStart()) {
						currentProblem = nextProblem;
						problemIndex++;
					} else {
						currentProblem = nextTask;
						taskIndex++;
					}
				} else {
					currentProblem = nextProblem;
					problemIndex++;
				}
			} else {
				if (nextTask != null) {
					currentProblem = nextTask;
					taskIndex++;
				}
			}
			allProblems[allProblemIndex++] = currentProblem;
		}
		return allProblems;
	}
	
	public ClassFile[] getClassFiles() {
		Enumeration files = compiledTypes.elements();
		ClassFile[] classFiles = new ClassFile[compiledTypes.size()];
		int index = 0;
		while (files.hasMoreElements()){
			classFiles[index++] = (ClassFile)files.nextElement();
		}
		return classFiles;	
	}

	/**
	 * Answer the initial compilation unit corresponding to the present compilation result
	 */
	public ICompilationUnit getCompilationUnit(){
		return compilationUnit;
	}

	/**
	 * Answer the initial file name
	 */
	public char[] getFileName(){
		return fileName;
	}
	
	/**
	 * Answer the errors encountered during compilation.
	 */
	public IProblem[] getErrors() {
	
		IProblem[] reportedProblems = getProblems();
		int errorCount = 0;
		for (int i = 0; i < this.problemCount; i++) {
			if (reportedProblems[i].isError()) errorCount++;
		}
		if (errorCount == this.problemCount) return reportedProblems;
		IProblem[] errors = new IProblem[errorCount];
		int index = 0;
		for (int i = 0; i < this.problemCount; i++) {
			if (reportedProblems[i].isError()) errors[index++] = reportedProblems[i];
		}
		return errors;
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
	
			if (this.problemCount != problems.length) {
				System.arraycopy(problems, 0, (problems = new IProblem[problemCount]), 0, problemCount);
			}
	
			if (this.maxProblemPerUnit > 0 && this.problemCount > this.maxProblemPerUnit){
				quickPrioritize(problems, 0, problemCount - 1);
				this.problemCount = this.maxProblemPerUnit;
				System.arraycopy(problems, 0, (problems = new IProblem[problemCount]), 0, problemCount);
			}
	
			// Sort problems per source positions.
			quickSort(problems, 0, problems.length-1);
		}
		return problems;
	}

	/**
	 * Answer the tasks (TO-DO, ...) encountered during compilation.
	 *
	 * This is not a compiler internal API - it has side-effects !
	 * It is intended to be used only once all problems have been detected,
	 * and makes sure the problems slot as the exact size of the number of
	 * problems.
	 */
	public IProblem[] getTasks() {
		
		// Re-adjust the size of the tasks if necessary.
		if (this.tasks != null) {
	
			if (this.taskCount != this.tasks.length) {
				System.arraycopy(this.tasks, 0, (this.tasks = new IProblem[this.taskCount]), 0, this.taskCount);
			}
			quickSort(tasks, 0, tasks.length-1);
		}
		return this.tasks;
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

	public boolean hasSyntaxError(){

		if (problems != null)
			for (int i = 0; i < problemCount; i++) {
				IProblem problem = problems[i];
				if ((problem.getID() & IProblem.Syntax) != 0 && problem.isError())
					return true;
			}
		return false;
	}

	public boolean hasTasks() {
		return this.taskCount != 0;
	}
	
	public boolean hasWarnings() {

		if (problems != null)
			for (int i = 0; i < problemCount; i++) {
				if (problems[i].isWarning())
					return true;
			}
		return false;
	}
	
	private static void quickSort(IProblem[] list, int left, int right) {

		if (left >= right) return;
	
		// sort the problems by their source start position... starting with 0
		int original_left = left;
		int original_right = right;
		int mid = list[(left + right) / 2].getSourceStart();
		do {
			while (list[left].getSourceStart() < mid)
				left++;
			while (mid < list[right].getSourceStart())
				right--;
			if (left <= right) {
				IProblem tmp = list[left];
				list[left] = list[right];
				list[right] = tmp;
				left++;
				right--;
			}
		} while (left <= right);
		if (original_left < right)
			quickSort(list, original_left, right);
		if (left < original_right)
			quickSort(list, left, original_right);
	}
	
	private void quickPrioritize(IProblem[] list, int left, int right) {
		
		if (left >= right) return;
	
		// sort the problems by their priority... starting with the highest priority
		int original_left = left;
		int original_right = right;
		int mid = computePriority(list[(left + right) / 2]);
		do {
			while (computePriority(list[right]) < mid)
				right--;
			while (mid < computePriority(list[left]))
				left++;
			if (left <= right) {
				IProblem tmp = list[left];
				list[left] = list[right];
				list[right] = tmp;
				left++;
				right--;
			}
		} while (left <= right);
		if (original_left < right)
			quickPrioritize(list, original_left, right);
		if (left < original_right)
			quickPrioritize(list, left, original_right);
	}
	
	/**
	 * For now, remember the compiled type using its compound name.
	 */
	public void record(char[] typeName, ClassFile classFile) {

	    SourceTypeBinding sourceType = classFile.referenceBinding;
	    if (!sourceType.isLocalType() && sourceType.isHierarchyInconsistent()) {
	        this.hasInconsistentToplevelHierarchies = true;
	    }
		compiledTypes.put(typeName, classFile);
	}

	public void record(IProblem newProblem, ReferenceContext referenceContext) {

		if (newProblem.getID() == IProblem.Task) {
			recordTask(newProblem);
			return;
		}
		if (problemCount == 0) {
			problems = new IProblem[5];
		} else if (problemCount == problems.length) {
			System.arraycopy(problems, 0, (problems = new IProblem[problemCount * 2]), 0, problemCount);
		}
		problems[problemCount++] = newProblem;
		if (referenceContext != null){
			if (problemsMap == null) problemsMap = new Hashtable(5);
			if (firstErrorsMap == null) firstErrorsMap = new Hashtable(5);
			if (newProblem.isError() && !referenceContext.hasErrors()) firstErrorsMap.put(newProblem, newProblem);
			problemsMap.put(newProblem, referenceContext);
		}
	}

	private void recordTask(IProblem newProblem) {
		if (this.taskCount == 0) {
			this.tasks = new IProblem[5];
		} else if (this.taskCount == this.tasks.length) {
			System.arraycopy(this.tasks, 0, (this.tasks = new IProblem[this.taskCount * 2]), 0, this.taskCount);
		}
		this.tasks[this.taskCount++] = newProblem;
	}
	
	public CompilationResult tagAsAccepted(){

		this.hasBeenAccepted = true;
		this.problemsMap = null; // flush
		return this;
	}
	
	public String toString(){

		StringBuffer buffer = new StringBuffer();
		if (this.fileName != null){
			buffer.append("Filename : ").append(this.fileName).append('\n'); //$NON-NLS-1$
		}
		if (this.compiledTypes != null){
			buffer.append("COMPILED type(s)	\n");  //$NON-NLS-1$
			Enumeration typeNames = this.compiledTypes.keys();
			while (typeNames.hasMoreElements()) {
				char[] typeName = (char[]) typeNames.nextElement();
				buffer.append("\t - ").append(typeName).append('\n');   //$NON-NLS-1$
				
			}
		} else {
			buffer.append("No COMPILED type\n");  //$NON-NLS-1$
		}
		if (problems != null){
			buffer.append(this.problemCount).append(" PROBLEM(s) detected \n"); //$NON-NLS-1$//$NON-NLS-2$
			for (int i = 0; i < this.problemCount; i++){
				buffer.append("\t - ").append(this.problems[i]).append('\n'); //$NON-NLS-1$
			}
		} else {
			buffer.append("No PROBLEM\n"); //$NON-NLS-1$
		} 
		return buffer.toString();
	}
}
