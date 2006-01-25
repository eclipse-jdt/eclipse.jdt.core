/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.SimpleSet;
import org.eclipse.jdt.internal.core.util.Util;

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

protected Compiler compiler;
protected WorkQueue workQueue;
protected ArrayList problemSourceFiles;
protected boolean compiledAllAtOnce;

private boolean inCompiler;

protected SimpleSet filesWithAnnotations = null;

public static int MAX_AT_ONCE = 1000;
public final static String[] JAVA_PROBLEM_MARKER_ATTRIBUTE_NAMES = {
	IMarker.MESSAGE, 
	IMarker.SEVERITY, 
	IJavaModelMarker.ID, 
	IMarker.CHAR_START, 
	IMarker.CHAR_END, 
	IMarker.LINE_NUMBER, 
	IJavaModelMarker.ARGUMENTS,
	IJavaModelMarker.CATEGORY_ID,	
};
public final static String[] JAVA_TASK_MARKER_ATTRIBUTE_NAMES = {
	IMarker.MESSAGE, 
	IMarker.PRIORITY, 
	IJavaModelMarker.ID, 
	IMarker.CHAR_START, 
	IMarker.CHAR_END, 
	IMarker.LINE_NUMBER, 
	IMarker.USER_EDITABLE,
};
public final static Integer S_ERROR = new Integer(IMarker.SEVERITY_ERROR);
public final static Integer S_WARNING = new Integer(IMarker.SEVERITY_WARNING);
public final static Integer P_HIGH = new Integer(IMarker.PRIORITY_HIGH);
public final static Integer P_NORMAL = new Integer(IMarker.PRIORITY_NORMAL);
public final static Integer P_LOW = new Integer(IMarker.PRIORITY_LOW);

protected AbstractImageBuilder(JavaBuilder javaBuilder, boolean buildStarting, State newState) {
	// local copies
	this.javaBuilder = javaBuilder;
	this.nameEnvironment = javaBuilder.nameEnvironment;
	this.sourceLocations = this.nameEnvironment.sourceLocations;
	this.notifier = javaBuilder.notifier;

	if (buildStarting) {
		this.newState = newState == null ? new State(javaBuilder) : newState;
		this.compiler = newCompiler();
		this.workQueue = new WorkQueue();
		this.problemSourceFiles = new ArrayList(3);

		if (this.javaBuilder.participants != null) {
			for (int i = 0, l = this.javaBuilder.participants.length; i < l; i++) {
				if (this.javaBuilder.participants[i].isAnnotationProcessor()) {
					// initialize this set so the builder knows to gather CUs that define Annotation types
					// each Annotation processor participant is then asked to process these files AFTER
					// the compile loop. The normal dependency loop will then recompile all affected types
					this.filesWithAnnotations = new SimpleSet(1);
					break;
				}
			}
		}
	}
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
		workQueue.finished(compilationUnit);

		try {
			updateProblemsFor(compilationUnit, result); // record compilation problems before potentially adding duplicate errors
			updateTasksFor(compilationUnit, result); // record tasks
		} catch (CoreException e) {
			throw internalException(e);
		}

		if (result.hasInconsistentToplevelHierarchies)
			// ensure that this file is always retrieved from source for the rest of the build
			if (!problemSourceFiles.contains(compilationUnit))
				problemSourceFiles.add(compilationUnit);

		IType mainType = null;
		String mainTypeName = null;
		String typeLocator = compilationUnit.typeLocator();
		ClassFile[] classFiles = result.getClassFiles();
		int length = classFiles.length;
		ArrayList duplicateTypeNames = null;
		ArrayList definedTypeNames = new ArrayList(length);
		for (int i = 0; i < length; i++) {
			ClassFile classFile = classFiles[i];

			char[][] compoundName = classFile.getCompoundName();
			char[] typeName = compoundName[compoundName.length - 1];
			boolean isNestedType = classFile.enclosingClassFile != null;

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
					if (mainType == null)
						try {
							mainTypeName = compilationUnit.initialTypeName; // slash separated qualified name "p1/p1/A"
							mainType = javaBuilder.javaProject.findType(mainTypeName.replace('/', '.'));
						} catch (JavaModelException e) {
							// ignore
						}
					IType type;
					if (qualifiedTypeName.equals(mainTypeName))
						type = mainType;
					else {
						String simpleName = qualifiedTypeName.substring(qualifiedTypeName.lastIndexOf('/')+1);
						type = mainType == null ? null : mainType.getCompilationUnit().getType(simpleName);
					}
					createProblemFor(compilationUnit.resource, type, Messages.bind(Messages.build_duplicateClassFile, new String(typeName)), JavaCore.ERROR); 
					continue;
				}
				newState.recordLocatorForType(qualifiedTypeName, typeLocator);
			}
			try {
				definedTypeNames.add(writeClassFile(classFile, compilationUnit, !isNestedType));
			} catch (CoreException e) {
				Util.log(e, "JavaBuilder handling CoreException"); //$NON-NLS-1$
				if (e.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS)
					createProblemFor(compilationUnit.resource, null, Messages.bind(Messages.build_classFileCollision, e.getMessage()), JavaCore.ERROR); 
				else
					createProblemFor(compilationUnit.resource, null, Messages.build_inconsistentClassFile, JavaCore.ERROR); 
			}
		}
		if (result.hasAnnotations && this.filesWithAnnotations != null) // only initialized if an annotation processor is attached
			this.filesWithAnnotations.add(compilationUnit);

		finishedWith(typeLocator, result, compilationUnit.getMainTypeName(), definedTypeNames, duplicateTypeNames);
		notifier.compiled(compilationUnit);
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
	if (this.filesWithAnnotations != null && this.filesWithAnnotations.elementSize > 0)
		// will add files that have annotations in acceptResult() & then processAnnotations() before exitting this method
		this.filesWithAnnotations.clear();

	// notify CompilationParticipants which source files are about to be compiled
	CompilationParticipantResult[] participantResults = this.javaBuilder.participants == null ? null : notifyParticipants(units);
	if (participantResults != null && participantResults.length > units.length) {
		units = new SourceFile[participantResults.length];
		for (int i = participantResults.length; --i >= 0;)
			units[i] = participantResults[i].sourceFile;
	}

	int unitsLength = units.length;
	this.compiledAllAtOnce = unitsLength <= MAX_AT_ONCE;
	if (this.compiledAllAtOnce) {
		// do them all now
		if (JavaBuilder.DEBUG)
			for (int i = 0; i < unitsLength; i++)
				System.out.println("About to compile " + units[i].typeLocator()); //$NON-NLS-1$
		compile(units, null);
	} else {
		int i = 0;
		boolean compilingFirstGroup = true;
		while (i < unitsLength) {
			int doNow = unitsLength < MAX_AT_ONCE ? unitsLength : MAX_AT_ONCE;
			int index = 0;
			SourceFile[] toCompile = new SourceFile[doNow];
			while (i < unitsLength && index < doNow) {
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
			SourceFile[] additionalUnits = new SourceFile[unitsLength - i];
			System.arraycopy(units, i, additionalUnits, 0, additionalUnits.length);
			compilingFirstGroup = false;
			compile(toCompile, additionalUnits);
		}
	}

	if (participantResults != null) {
		for (int i = participantResults.length; --i >= 0;)
			if (participantResults[i] != null)
				recordParticipantResult(participantResults[i]);

		processAnnotations(participantResults);
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
	} catch (AbortCompilation ignored) {
		// ignore the AbortCompilcation coming from BuildNotifier.checkCancelWithinCompiler()
		// the Compiler failed after the user has chose to cancel... likely due to an OutOfMemory error
	} finally {
		inCompiler = false;
	}
	// Check for cancel immediately after a compile, because the compiler may
	// have been cancelled but without propagating the correct exception
	notifier.checkCancel();
}

protected void createProblemFor(IResource resource, IMember javaElement, String message, String problemSeverity) {
	try {
		IMarker marker = resource.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
		int severity = problemSeverity.equals(JavaCore.WARNING) ? IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR;

		ISourceRange range = javaElement == null ? null : javaElement.getNameRange();
		int start = range == null ? 0 : range.getOffset();
		int end = range == null ? 1 : start + range.getLength();
		marker.setAttributes(
			new String[] {IMarker.MESSAGE, IMarker.SEVERITY, IMarker.CHAR_START, IMarker.CHAR_END},
			new Object[] {message, new Integer(severity), new Integer(start), new Integer(end)});
	} catch (CoreException e) {
		throw internalException(e);
	}
}

protected void deleteGeneratedFiles(IFile[] deletedGeneratedFiles) {
	// no op by default
}

protected SourceFile findSourceFile(IFile file) {
	if (!file.exists()) return null;

	// assumes the file exists in at least one of the source folders & is not excluded
	ClasspathMultiDirectory md = sourceLocations[0];
	if (sourceLocations.length > 1) {
		IPath sourceFileFullPath = file.getFullPath();
		for (int j = 0, m = sourceLocations.length; j < m; j++) {
			if (sourceLocations[j].sourceFolder.getFullPath().isPrefixOf(sourceFileFullPath)) {
				md = sourceLocations[j];
				if (md.exclusionPatterns == null && md.inclusionPatterns == null)
					break;
				if (!Util.isExcluded(file, md.inclusionPatterns, md.exclusionPatterns))
					break;
			}
		}
	}
	return new SourceFile(file, md);
}

protected void finishedWith(String sourceLocator, CompilationResult result, char[] mainTypeName, ArrayList definedTypeNames, ArrayList duplicateTypeNames) {
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
		folder.create(IResource.FORCE | IResource.DERIVED, true, null);
	}
	return folder;
}

protected RuntimeException internalException(CoreException t) {
	ImageBuilderInternalException imageBuilderException = new ImageBuilderInternalException(t);
	if (inCompiler)
		return new AbortCompilation(true, imageBuilderException);
	return imageBuilderException;
}

protected boolean isExcludedFromProject(IPath childPath) throws JavaModelException {
	// answer whether the folder should be ignored when walking the project as a source folder
	if (childPath.segmentCount() > 2) return false; // is a subfolder of a package

	for (int j = 0, k = sourceLocations.length; j < k; j++) {
		if (childPath.equals(sourceLocations[j].binaryFolder.getFullPath())) return true;
		if (childPath.equals(sourceLocations[j].sourceFolder.getFullPath())) return true;
	}
	// skip default output folder which may not be used by any source folder
	return childPath.equals(javaBuilder.javaProject.getOutputLocation());
}

protected Compiler newCompiler() {
	// disable entire javadoc support if not interested in diagnostics
	Map projectOptions = javaBuilder.javaProject.getOptions(true);
	String option = (String) projectOptions.get(JavaCore.COMPILER_PB_INVALID_JAVADOC);
	if (option == null || option.equals(JavaCore.IGNORE)) { // TODO (frederic) see why option is null sometimes while running model tests!?
		option = (String) projectOptions.get(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS);
		if (option == null || option.equals(JavaCore.IGNORE)) {
			option = (String) projectOptions.get(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS);
			if (option == null || option.equals(JavaCore.IGNORE)) {
				option = (String) projectOptions.get(JavaCore.COMPILER_PB_UNUSED_IMPORT);
				if (option == null || option.equals(JavaCore.IGNORE)) { // Unused import need also to look inside javadoc comment
					projectOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.DISABLED);
				}
			}
		}
	}
	
	// called once when the builder is initialized... can override if needed
	Compiler newCompiler = new Compiler(
		nameEnvironment,
		DefaultErrorHandlingPolicies.proceedWithAllProblems(),
		projectOptions,
		this,
		ProblemFactory.getProblemFactory(Locale.getDefault()));
	CompilerOptions options = newCompiler.options;

	// enable the compiler reference info support
	options.produceReferenceInfo = true;
	options.performStatementsRecovery = false;
	
	org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment env = newCompiler.lookupEnvironment;
	synchronized (env) {
		// enable shared byte[]'s used by ClassFile to avoid allocating MBs during a build
		env.sharedArraysUsed = false;
		env.sharedClassFileHeader = new byte[30000];
		env.sharedClassFileContents = new byte[30000];
	}

	return newCompiler;
}

protected CompilationParticipantResult[] notifyParticipants(SourceFile[] unitsAboutToCompile) {
	CompilationParticipantResult[] results = new CompilationParticipantResult[unitsAboutToCompile.length];
	for (int i = unitsAboutToCompile.length; --i >= 0;)
		results[i] = new CompilationParticipantResult(unitsAboutToCompile[i]);

	// TODO (kent) do we expect to have more than one participant?
	// and if so should we pass the generated files from the each processor to the others to process?
	// and what happens if some participants do not expect to be called with only a few files, after seeing 'all' the files?
	for (int i = 0, l = this.javaBuilder.participants.length; i < l; i++)
		this.javaBuilder.participants[i].buildStarting(results, this instanceof BatchImageBuilder);

	SimpleSet uniqueFiles = null;
	CompilationParticipantResult[] toAdd = null;
	int added = 0;
	for (int i = results.length; --i >= 0;) {
		CompilationParticipantResult result = results[i];
		if (result == null) continue;

		IFile[] deletedGeneratedFiles = result.deletedFiles;
		if (deletedGeneratedFiles != null)
			deleteGeneratedFiles(deletedGeneratedFiles);

		IFile[] addedGeneratedFiles = result.addedFiles;
		if (addedGeneratedFiles != null) {
			for (int j = addedGeneratedFiles.length; --j >= 0;) {
				SourceFile sourceFile = findSourceFile(addedGeneratedFiles[j]);
				if (sourceFile == null) continue;
				if (uniqueFiles == null) {
					uniqueFiles = new SimpleSet(unitsAboutToCompile.length + 3);
					for (int f = unitsAboutToCompile.length; --f >= 0;)
						uniqueFiles.add(unitsAboutToCompile[f]);
				}
				if (!uniqueFiles.includes(sourceFile))
					uniqueFiles.add(sourceFile);
					CompilationParticipantResult newResult = new CompilationParticipantResult(sourceFile);
					// is there enough room to add all the addedGeneratedFiles.length ?
					if (toAdd == null) {
						toAdd = new CompilationParticipantResult[addedGeneratedFiles.length];
					} else {
						int length = toAdd.length;
						if (added == length)
							System.arraycopy(toAdd, 0, toAdd = new CompilationParticipantResult[length + addedGeneratedFiles.length], 0, length);
					}
					toAdd[added++] = newResult;
			}
		}
	}

	if (added >0 ) {
		int length = results.length;
		System.arraycopy(results, 0, results = new CompilationParticipantResult[length + added], 0 , length);
		System.arraycopy(toAdd, 0, results, length, added);
	}
	return results;
}

protected abstract void processAnnotationResults(CompilationParticipantResult[] results);

protected void processAnnotations(CompilationParticipantResult[] results) {
	boolean hasAnnotationProcessor = false;
	for (int i = 0, l = this.javaBuilder.participants.length; !hasAnnotationProcessor && i < l; i++)
		hasAnnotationProcessor = this.javaBuilder.participants[i].isAnnotationProcessor();
	if (!hasAnnotationProcessor) return;

	boolean foundAnnotations = this.filesWithAnnotations != null && this.filesWithAnnotations.elementSize > 0;
	for (int i = results.length; --i >= 0;)
		results[i].reset(foundAnnotations && this.filesWithAnnotations.includes(results[i].sourceFile));

	// even if no files have annotations, must still tell every annotation processor in case the file used to have them
	for (int i = 0, l = this.javaBuilder.participants.length; i < l; i++)
		if (this.javaBuilder.participants[i].isAnnotationProcessor())
			this.javaBuilder.participants[i].processAnnotations(results);
	processAnnotationResults(results);
}

protected void recordParticipantResult(CompilationParticipantResult result) {
	// any added/changed/deleted generated files have already been taken care
	// just record the problems and dependencies - do not expect there to be many
	// must be called after we're finished with the compilation unit results but before incremental loop adds affected files
	IProblem[] problems = result.problems;
	if (problems != null && problems.length > 0) {
		// existing problems have already been removed so just add these as new problems
		this.notifier.updateProblemCounts(problems);
		try {
			storeProblemsFor(result.sourceFile, problems);
		} catch (CoreException e) {
			// must continue with compile loop so just log the CoreException
			e.printStackTrace();
		}
	}

	String[] dependencies = result.dependencies;
	if (dependencies != null) {
		ReferenceCollection refs = (ReferenceCollection) this.newState.references.get(result.sourceFile.typeLocator());
		if (refs != null)
			refs.addDependencies(dependencies);
	}
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
		if (id == IProblem.IsClassPathCorrect) {
			JavaBuilder.removeProblemsAndTasksFor(javaBuilder.currentProject); // make this the only problem for this project
			String[] args = problem.getArguments();
			missingClassFile = args[0];
		}

		if (id != IProblem.Task) {
			IMarker marker = resource.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
			marker.setAttributes(
				JAVA_PROBLEM_MARKER_ATTRIBUTE_NAMES,
				new Object[] { 
					problem.getMessage(), // message
					problem.isError() ? S_ERROR : S_WARNING, // severity
					new Integer(id), // ID
					new Integer(problem.getSourceStart()), // start
					new Integer(problem.getSourceEnd() + 1), // end
					new Integer(problem.getSourceLineNumber()), // line
					Util.getProblemArgumentsForMarker(problem.getArguments()), // arguments
					new Integer(ProblemReporter.getProblemCategory(id)), // category ID
				}
			);
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
			Integer priority = P_NORMAL;
			String compilerPriority = task.getArguments()[2];
			if (JavaCore.COMPILER_TASK_PRIORITY_HIGH.equals(compilerPriority))
				priority = P_HIGH;
			else if (JavaCore.COMPILER_TASK_PRIORITY_LOW.equals(compilerPriority))
				priority = P_LOW;
			marker.setAttributes(
				JAVA_TASK_MARKER_ATTRIBUTE_NAMES,
				new Object[] { 
					task.getMessage(),
					priority,
					new Integer(task.getID()),
					new Integer(task.getSourceStart()),
					new Integer(task.getSourceEnd() + 1),
					new Integer(task.getSourceLineNumber()),
					Boolean.FALSE,
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

protected char[] writeClassFile(ClassFile classFile, SourceFile compilationUnit, boolean isSecondaryType) throws CoreException {
	String fileName = new String(classFile.fileName()); // the qualified type name "p1/p2/A"
	IPath filePath = new Path(fileName);
	IContainer outputFolder = compilationUnit.sourceLocation.binaryFolder; 
	IContainer container = outputFolder;
	if (filePath.segmentCount() > 1) {
		container = createFolder(filePath.removeLastSegments(1), outputFolder);
		filePath = new Path(filePath.lastSegment());
	}

	IFile file = container.getFile(filePath.addFileExtension(SuffixConstants.EXTENSION_class));
	writeClassFileBytes(classFile.getBytes(), file, fileName, isSecondaryType, compilationUnit.updateClassFile);
	if (classFile.ownSharedArrays) {
		org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment env = this.compiler.lookupEnvironment;
		synchronized (env) {
			env.sharedArraysUsed = false;
		}
	}

	// answer the name of the class file as in Y or Y$M
	return filePath.lastSegment().toCharArray();
}

protected void writeClassFileBytes(byte[] bytes, IFile file, String qualifiedFileName, boolean isSecondaryType, boolean updateClassFile) throws CoreException {
	if (file.exists()) {
		// Deal with shared output folders... last one wins... no collision cases detected
		if (JavaBuilder.DEBUG)
			System.out.println("Writing changed class file " + file.getName());//$NON-NLS-1$
		if (!file.isDerived())
			file.setDerived(true);
		file.setContents(new ByteArrayInputStream(bytes), true, false, null);
	} else {
		// Default implementation just writes out the bytes for the new class file...
		if (JavaBuilder.DEBUG)
			System.out.println("Writing new class file " + file.getName());//$NON-NLS-1$
		file.create(new ByteArrayInputStream(bytes), IResource.FORCE | IResource.DERIVED, null);
	}
}
}
