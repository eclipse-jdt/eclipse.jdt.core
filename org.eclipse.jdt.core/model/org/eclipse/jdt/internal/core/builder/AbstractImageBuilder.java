/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.core.*;

import java.io.*;
import java.util.*;

/**
 * The abstract superclass of Java builders.
 * Provides the building and compilation mechanism
 * in common with the batch and incremental builders.
 */
public abstract class AbstractImageBuilder implements ICompilerRequestor {

protected JavaBuilder javaBuilder;
protected State newState;

// local copies
protected NameEnvironment nameEnvironment;
protected ClasspathMultiDirectory[] sourceLocations;
protected BuildNotifier notifier;

protected String encoding;
protected Compiler compiler;
protected WorkQueue workQueue;
protected ArrayList problemSourceFiles;
protected boolean compiledAllAtOnce;

private boolean inCompiler;

public static int MAX_AT_ONCE = 1000;

protected AbstractImageBuilder(JavaBuilder javaBuilder) {
	this.javaBuilder = javaBuilder;
	this.newState = new State(javaBuilder);

	// local copies
	this.nameEnvironment = javaBuilder.nameEnvironment;
	this.sourceLocations = this.nameEnvironment.sourceLocations;
	this.notifier = javaBuilder.notifier;

	this.encoding = javaBuilder.javaProject.getOption(JavaCore.CORE_ENCODING, true);
	this.compiler = newCompiler();
	this.workQueue = new WorkQueue();
	this.problemSourceFiles = new ArrayList(3);
}

public void acceptResult(CompilationResult result) {
	// In Batch mode, we write out the class files, hold onto the dependency info
	// & additional types and report problems.

	// In Incremental mode, when writing out a class file we need to compare it
	// against the previous file, remembering if structural changes occured.
	// Before reporting the new problems, we need to update the problem count &
	// remove the old problems. Plus delete additional class files that no longer exist.

	SourceFile compilationUnit = (SourceFile) result.getCompilationUnit(); // go directly back to the sourceFile
	if (!workQueue.isCompiled(compilationUnit)) {
		try {
			workQueue.finished(compilationUnit);
			updateProblemsFor(compilationUnit, result); // record compilation problems before potentially adding duplicate errors
			updateTasksFor(compilationUnit, result); // record tasks

			String typeLocator = compilationUnit.typeLocator();
			ClassFile[] classFiles = result.getClassFiles();
			int length = classFiles.length;
			ArrayList duplicateTypeNames = null;
			ArrayList definedTypeNames = new ArrayList(length);
			for (int i = 0; i < length; i++) {
				ClassFile classFile = classFiles[i];
				char[][] compoundName = classFile.getCompoundName();
				char[] typeName = compoundName[compoundName.length - 1];
				boolean isNestedType = CharOperation.contains('$', typeName);

				// Look for a possible collision, if one exists, report an error but do not write the class file
				if (isNestedType) {
					String qualifiedTypeName = new String(classFile.outerMostEnclosingClassFile().fileName());
					if (newState.isDuplicateLocator(qualifiedTypeName, typeLocator))
						continue;
				} else {
					String qualifiedTypeName = new String(classFile.fileName()); // the qualified type name "p1/p2/A"
					if (newState.isDuplicateLocator(qualifiedTypeName, typeLocator)) {
						if (duplicateTypeNames == null)
							duplicateTypeNames = new ArrayList();
						duplicateTypeNames.add(compoundName);
						createErrorFor(compilationUnit.resource, Util.bind("build.duplicateClassFile", new String(typeName))); //$NON-NLS-1$
						continue;
					}
					newState.recordLocatorForType(qualifiedTypeName, typeLocator);
				}
				definedTypeNames.add(writeClassFile(classFile, compilationUnit.sourceLocation.binaryFolder, !isNestedType));
			}

			finishedWith(typeLocator, result, compilationUnit.getMainTypeName(), definedTypeNames, duplicateTypeNames);
			notifier.compiled(compilationUnit);
		} catch (CoreException e) {
			Util.log(e, "JavaBuilder handling CoreException"); //$NON-NLS-1$
			createErrorFor(compilationUnit.resource, Util.bind("build.inconsistentClassFile")); //$NON-NLS-1$
		}
	}
}

protected void cleanUp() {
	this.nameEnvironment.cleanup();

	this.javaBuilder = null;
	this.nameEnvironment = null;
	this.sourceLocations = null;
	this.notifier = null;
	this.compiler = null;
	this.workQueue = null;
	this.problemSourceFiles = null;
}

/* Compile the given elements, adding more elements to the work queue 
* if they are affected by the changes.
*/
protected void compile(SourceFile[] units) {
	int toDo = units.length;
	if (this.compiledAllAtOnce = toDo <= MAX_AT_ONCE) {
		// do them all now
		if (JavaBuilder.DEBUG)
			for (int i = 0; i < toDo; i++)
				System.out.println("About to compile " + units[i].typeLocator()); //$NON-NLS-1$
		compile(units, null);
	} else {
		int i = 0;
		boolean compilingFirstGroup = true;
		while (i < toDo) {
			int doNow = toDo < MAX_AT_ONCE ? toDo : MAX_AT_ONCE;
			int index = 0;
			SourceFile[] toCompile = new SourceFile[doNow];
			while (i < toDo && index < doNow) {
				// Although it needed compiling when this method was called, it may have
				// already been compiled when it was referenced by another unit.
				SourceFile unit = units[i++];
				if (compilingFirstGroup || workQueue.isWaiting(unit)) {
					if (JavaBuilder.DEBUG)
						System.out.println("About to compile " + unit.typeLocator()); //$NON-NLS-1$
					toCompile[index++] = unit;
				}
			}
			if (index < doNow)
				System.arraycopy(toCompile, 0, toCompile = new SourceFile[index], 0, index);
			SourceFile[] additionalUnits = new SourceFile[toDo - i];
			System.arraycopy(units, i, additionalUnits, 0, additionalUnits.length);
			compilingFirstGroup = false;
			compile(toCompile, additionalUnits);
		}
	}
}

void compile(SourceFile[] units, SourceFile[] additionalUnits) {
	if (units.length == 0) return;
	notifier.aboutToCompile(units[0]); // just to change the message

	// extend additionalFilenames with all hierarchical problem types found during this entire build
	if (!problemSourceFiles.isEmpty()) {
		int toAdd = problemSourceFiles.size();
		int length = additionalUnits == null ? 0 : additionalUnits.length;
		if (length == 0)
			additionalUnits = new SourceFile[toAdd];
		else
			System.arraycopy(additionalUnits, 0, additionalUnits = new SourceFile[length + toAdd], 0, length);
		for (int i = 0; i < toAdd; i++)
			additionalUnits[length + i] = (SourceFile) problemSourceFiles.get(i);
	}
	String[] initialTypeNames = new String[units.length];
	for (int i = 0, l = units.length; i < l; i++)
		initialTypeNames[i] = units[i].initialTypeName;
	nameEnvironment.setNames(initialTypeNames, additionalUnits);
	notifier.checkCancel();
	try {
		inCompiler = true;
		compiler.compile(units);
	} finally {
		inCompiler = false;
	}
	// Check for cancel immediately after a compile, because the compiler may
	// have been cancelled but without propagating the correct exception
	notifier.checkCancel();
}

protected void createErrorFor(IResource resource, String message) {
	try {
		IMarker marker = resource.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
		int severity = IMarker.SEVERITY_ERROR;
		if (message.equals(Util.bind("build.duplicateResource"))) //$NON-NLS-1$
			if (JavaCore.WARNING.equals(javaBuilder.javaProject.getOption(JavaCore.CORE_JAVA_BUILD_DUPLICATE_RESOURCE, true)))
				severity = IMarker.SEVERITY_WARNING;
		marker.setAttributes(
			new String[] {IMarker.MESSAGE, IMarker.SEVERITY, IMarker.CHAR_START, IMarker.CHAR_END},
			new Object[] {message, new Integer(severity), new Integer(0), new Integer(1)});
	} catch (CoreException e) {
		throw internalException(e);
	}
}

protected void finishedWith(String sourceLocator, CompilationResult result, char[] mainTypeName, ArrayList definedTypeNames, ArrayList duplicateTypeNames) throws CoreException {
	if (duplicateTypeNames == null) {
		newState.record(sourceLocator, result.qualifiedReferences, result.simpleNameReferences, mainTypeName, definedTypeNames);
		return;
	}

	char[][][] qualifiedRefs = result.qualifiedReferences;
	char[][] simpleRefs = result.simpleNameReferences;
	// for each duplicate type p1.p2.A, add the type name A (package was already added)
	next : for (int i = 0, l = duplicateTypeNames.size(); i < l; i++) {
		char[][] compoundName = (char[][]) duplicateTypeNames.get(i);
		char[] typeName = compoundName[compoundName.length - 1];
		int sLength = simpleRefs.length;
		for (int j = 0; j < sLength; j++)
			if (CharOperation.equals(simpleRefs[j], typeName))
				continue next;
		System.arraycopy(simpleRefs, 0, simpleRefs = new char[sLength + 1][], 0, sLength);
		simpleRefs[sLength] = typeName;
	}
	newState.record(sourceLocator, qualifiedRefs, simpleRefs, mainTypeName, definedTypeNames);
}

protected IContainer createFolder(IPath packagePath, IContainer outputFolder) throws CoreException {
	if (packagePath.isEmpty()) return outputFolder;
	IFolder folder = outputFolder.getFolder(packagePath);
	if (!folder.exists()) {
		createFolder(packagePath.removeLastSegments(1), outputFolder);
		folder.create(true, true, null);
		folder.setDerived(true);
	}
	return folder;
}

protected RuntimeException internalException(CoreException t) {
	ImageBuilderInternalException imageBuilderException = new ImageBuilderInternalException(t);
	if (inCompiler)
		return new AbortCompilation(true, imageBuilderException);
	return imageBuilderException;
}

protected Compiler newCompiler() {
	// called once when the builder is initialized... can override if needed
	return new Compiler(
		nameEnvironment,
		DefaultErrorHandlingPolicies.proceedWithAllProblems(),
		javaBuilder.javaProject.getOptions(true),
		this,
		ProblemFactory.getProblemFactory(Locale.getDefault()));
}

/**
 * Creates a marker from each problem and adds it to the resource.
 * The marker is as follows:
 *   - its type is T_PROBLEM
 *   - its plugin ID is the JavaBuilder's plugin ID
 *	 - its message is the problem's message
 *	 - its priority reflects the severity of the problem
 *	 - its range is the problem's range
 *	 - it has an extra attribute "ID" which holds the problem's id
 */
protected void storeProblemsFor(SourceFile sourceFile, IProblem[] problems) throws CoreException {
	if (sourceFile == null || problems == null || problems.length == 0) return;

	String missingClassFile = null;
	IResource resource = sourceFile.resource;
	for (int i = 0, l = problems.length; i < l; i++) {
		IProblem problem = problems[i];
		int id = problem.getID();
		switch (id) {
			case IProblem.IsClassPathCorrect :
				JavaBuilder.removeProblemsAndTasksFor(javaBuilder.currentProject); // make this the only problem for this project
				String[] args = problem.getArguments();
				missingClassFile = args[0];
				break;
			case IProblem.SuperclassMustBeAClass :
			case IProblem.SuperInterfaceMustBeAnInterface :
			case IProblem.HierarchyCircularitySelfReference :
			case IProblem.HierarchyCircularity :
			case IProblem.HierarchyHasProblems :
			case IProblem.SuperclassNotFound :
			case IProblem.SuperclassNotVisible :
			case IProblem.SuperclassAmbiguous :
			case IProblem.SuperclassInternalNameProvided :
			case IProblem.SuperclassInheritedNameHidesEnclosingName :
			case IProblem.InterfaceNotFound :
			case IProblem.InterfaceNotVisible :
			case IProblem.InterfaceAmbiguous :
			case IProblem.InterfaceInternalNameProvided :
			case IProblem.InterfaceInheritedNameHidesEnclosingName :
				// ensure that this file is always retrieved from source for the rest of the build
				if (!problemSourceFiles.contains(sourceFile))
					problemSourceFiles.add(sourceFile);
				break;
		}

		if (id != IProblem.Task) {
			IMarker marker = resource.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
			marker.setAttributes(
				new String[] {
					IMarker.MESSAGE, 
					IMarker.SEVERITY, 
					IJavaModelMarker.ID, 
					IMarker.CHAR_START, 
					IMarker.CHAR_END, 
					IMarker.LINE_NUMBER, 
					IJavaModelMarker.ARGUMENTS},
				new Object[] { 
					problem.getMessage(),
					new Integer(problem.isError() ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING), 
					new Integer(id),
					new Integer(problem.getSourceStart()),
					new Integer(problem.getSourceEnd() + 1),
					new Integer(problem.getSourceLineNumber()),
					Util.getProblemArgumentsForMarker(problem.getArguments())
				});
		}

/* Do NOT want to populate the Java Model just to find the matching Java element.
 * Also cannot query compilation units located in folders with invalid package
 * names such as 'a/b.c.d/e'.

		// compute a user-friendly location
		IJavaElement element = JavaCore.create(resource);
		if (element instanceof org.eclipse.jdt.core.ICompilationUnit) { // try to find a finer grain element
			org.eclipse.jdt.core.ICompilationUnit unit = (org.eclipse.jdt.core.ICompilationUnit) element;
			IJavaElement fragment = unit.getElementAt(problem.getSourceStart());
			if (fragment != null) element = fragment;
		}
		String location = null;
		if (element instanceof JavaElement)
			location = ((JavaElement) element).readableName();
		if (location != null)
			marker.setAttribute(IMarker.LOCATION, location);
*/

		if (missingClassFile != null)
			throw new MissingClassFileException(missingClassFile);
	}
}

protected void storeTasksFor(SourceFile sourceFile, IProblem[] tasks) throws CoreException {
	if (sourceFile == null || tasks == null || tasks.length == 0) return;

	IResource resource = sourceFile.resource;
	for (int i = 0, l = tasks.length; i < l; i++) {
		IProblem task = tasks[i];
		if (task.getID() == IProblem.Task) {
			IMarker marker = resource.createMarker(IJavaModelMarker.TASK_MARKER);
			int priority = IMarker.PRIORITY_NORMAL;
			String compilerPriority = task.getArguments()[2];
			if (JavaCore.COMPILER_TASK_PRIORITY_HIGH.equals(compilerPriority))
				priority = IMarker.PRIORITY_HIGH;
			else if (JavaCore.COMPILER_TASK_PRIORITY_LOW.equals(compilerPriority))
				priority = IMarker.PRIORITY_LOW;
			marker.setAttributes(
				new String[] {
					IMarker.MESSAGE, 
					IMarker.PRIORITY, 
					IMarker.DONE, 
					IMarker.CHAR_START, 
					IMarker.CHAR_END, 
					IMarker.LINE_NUMBER,
					"readOnly"}, //TODO: improve once IMarker constant is added //$NON-NLS-1$
				new Object[] { 
					task.getMessage(),
					new Integer(priority),
					new Boolean(false),
					new Integer(task.getSourceStart()),
					new Integer(task.getSourceEnd() + 1),
					new Integer(task.getSourceLineNumber()),
					new Boolean(true),
				});
		}
	}
}

protected void updateProblemsFor(SourceFile sourceFile, CompilationResult result) throws CoreException {
	IProblem[] problems = result.getProblems();
	if (problems == null || problems.length == 0) return;

	notifier.updateProblemCounts(problems);
	storeProblemsFor(sourceFile, problems);
}

protected void updateTasksFor(SourceFile sourceFile, CompilationResult result) throws CoreException {
	IProblem[] tasks = result.getTasks();
	if (tasks == null || tasks.length == 0) return;

	storeTasksFor(sourceFile, tasks);
}

protected char[] writeClassFile(ClassFile classFile, IContainer outputFolder, boolean isSecondaryType) throws CoreException {
	String fileName = new String(classFile.fileName()); // the qualified type name "p1/p2/A"
	IPath filePath = new Path(fileName);
	IContainer container = outputFolder;
	if (filePath.segmentCount() > 1) {
		container = createFolder(filePath.removeLastSegments(1), outputFolder);
		filePath = new Path(filePath.lastSegment());
	}

	IFile file = container.getFile(filePath.addFileExtension(JavaBuilder.CLASS_EXTENSION));
	writeClassFileBytes(classFile.getBytes(), file, fileName, isSecondaryType);
	// answer the name of the class file as in Y or Y$M
	return filePath.lastSegment().toCharArray();
}

protected void writeClassFileBytes(byte[] bytes, IFile file, String qualifiedFileName, boolean isSecondaryType) throws CoreException {
	if (file.exists()) {
		// Deal with shared output folders... last one wins... no collision cases detected
		if (JavaBuilder.DEBUG)
			System.out.println("Writing changed class file " + file.getName());//$NON-NLS-1$
		file.setContents(new ByteArrayInputStream(bytes), true, false, null);
		if (!file.isDerived())
			file.setDerived(true);
	} else {
		// Default implementation just writes out the bytes for the new class file...
		if (JavaBuilder.DEBUG)
			System.out.println("Writing new class file " + file.getName());//$NON-NLS-1$
		file.create(new ByteArrayInputStream(bytes), IResource.FORCE, null);
		file.setDerived(true);
	}
}
}