package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.util.*;

import java.util.*;
import java.io.*;

/**
 * The abstract superclass of image builders.
 * Provides the building and compilation mechanism
 * in common with the batch and incremental builders.
 */
public abstract class AbstractImageBuilder implements ICompilerRequestor {

protected JavaBuilder javaBuilder;

// local copies
protected IContainer outputFolder;
protected IContainer[] sourceFolders;
protected BuildNotifier notifier;

protected boolean hasSeparateOutputFolder;
protected NameEnvironment nameEnvironment;
protected Compiler compiler;
protected State newState;
protected WorkQueue workQueue;

private boolean inCompiler;

public static int MAX_AT_ONCE = 1000;
static final String ProblemMarkerTag = IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER;

protected AbstractImageBuilder(JavaBuilder javaBuilder) {
	this.javaBuilder = javaBuilder;

	// local copies
	this.outputFolder = javaBuilder.outputFolder;
	this.sourceFolders = javaBuilder.sourceFolders;
	this.notifier = javaBuilder.notifier;

	this.hasSeparateOutputFolder = !outputFolder.getFullPath().equals(javaBuilder.currentProject.getFullPath());
	this.nameEnvironment = new NameEnvironment(javaBuilder.classpath);
	this.compiler = newCompiler();
	this.newState = new State(javaBuilder);
	this.workQueue = new WorkQueue();
}

public void acceptResult(CompilationResult result) {
	// In Batch mode, we write out the class files, hold onto the dependency info
	// & additional types and report problems.

	// In Incremental mode, when writing out a class file we need to compare it
	// against the previous file, remembering if structural changes occured.
	// Before reporting the new problems, we need to update the problem count &
	// remove the old problems. Plus delete additional class files that no longer exist.

// should CompilationResult keep this as a String? Or convert WorkQueue to hold onto char[]?
	char[] fileId = result.getFileName();
//	CharOperation.replace(fileId, '\\', '/');
	String filename = new String(fileId);
	// this is the full filesystem source path 'd:/xyz/eclipse/Test/p1/p2/A.java'
	if (!workQueue.isCompiled(filename)) {
		try {
			workQueue.finished(filename);
	
			ICompilationUnit compilationUnit = result.getCompilationUnit();
			ClassFile[] classFiles = result.getClassFiles();
			int length = classFiles.length;
			ArrayList otherTypeNames = new ArrayList(length);
			char[] mainTypeName = compilationUnit.getMainTypeName(); // may not match any produced class file
			for (int i = 0; i < length; i++) {
				ClassFile classFile = classFiles[i];
				char[][] compoundName = classFile.getCompoundName();
				char[] typeName = compoundName[compoundName.length - 1];
				if (CharOperation.equals(mainTypeName, typeName)) {
					writeClassFile(classFile, false);
				} else {
					boolean isSecondaryType = !CharOperation.contains('$', typeName);
					otherTypeNames.add(writeClassFile(classFile, isSecondaryType));
				}
			}
			newState.recordDependencies(fileId, result.qualifiedReferences, result.simpleNameReferences);
			updateProblemsFor(result);
			if (otherTypeNames.isEmpty()) {
				finishedWith(fileId, new char[0][]);
			} else {
				char[][] additionalTypeNames = new char[otherTypeNames.size()][];
				otherTypeNames.toArray(additionalTypeNames);
				finishedWith(fileId, additionalTypeNames);
			}
			notifier.compiled(compilationUnit);
		} catch (CoreException e) {
			throw internalException(e);
		}
	}
}

protected void cleanUp() {
	this.javaBuilder = null;
	this.compiler = null;
	this.nameEnvironment = null;
	this.workQueue = null;
	this.newState.cleanup();
}

/* Compile the given elements, adding more elements to the work queue 
* if they are affected by the changes.
*/
protected void compile(String[] filenames, String[] initialTypeNames) {
	nameEnvironment.initialTypeNames(initialTypeNames);

	int i = 0;
	int toDo = filenames.length;
	boolean inFirstPass = true;
	while (i < toDo) {
		ArrayList doNow = new ArrayList(Math.min(toDo, MAX_AT_ONCE));
		while (i < toDo && doNow.size() < MAX_AT_ONCE) {
			String filename = filenames[i++];
			// Although it needed compiling when this method was called, it may have
			// already been compiled when it was referenced by another unit.
			if (inFirstPass || workQueue.isWaiting(filename)) {
				CompilationUnit compUnit = new CompilationUnit(null, filename);
				doNow.add(compUnit);
// WHY is there no notification about which files are about to compiled?
// Would the names go by too fast?
			}
		}
		inFirstPass = false;
		notifier.checkCancel();
		if (doNow.size() > 0) {
			CompilationUnit[] toCompile = new CompilationUnit[doNow.size()];
			doNow.toArray(toCompile);
			try {
				inCompiler = true;
				compiler.compile(toCompile);
			} finally {
				inCompiler = false;
			}

			// Check for cancel immediately after a compile, because the compiler may
			// have been cancelled but without propagating the correct exception
			notifier.checkCancel();
		}
	}
}

protected void finishedWith(char[] fileId, char[][] additionalTypeNames) throws CoreException {
	newState.rememberAdditionalTypes(fileId, additionalTypeNames);
}

protected RuntimeException internalException(Throwable t) {
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
		JavaCore.getOptions(),
		this,
		ProblemFactory.getProblemFactory(Locale.getDefault()));
}

protected IMarker[] getProblemsFor(IResource resource) {
	try {
		if (resource != null && resource.exists())
			return resource.findMarkers(ProblemMarkerTag, false, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {
		throw internalException(e);
	}
	return new IMarker[0];
}

protected void removeProblemsFor(IResource resource) {
	try {
		if (resource != null && resource.exists())
			resource.deleteMarkers(ProblemMarkerTag, false, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {
		throw internalException(e);
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
protected void storeProblemsFor(IResource resource, IProblem[] problems) {
	if (problems == null || problems.length == 0) return;
	for (int i = 0, length = problems.length; i < length; i++) {
		try {
			IProblem problem = problems[i];
			IMarker marker = resource.createMarker(ProblemMarkerTag);
// WHY are we holding onto the problem id?
			marker.setAttributes(
				new String[] {IMarker.MESSAGE, IMarker.SEVERITY, "ID", IMarker.CHAR_START, IMarker.CHAR_END, IMarker.LINE_NUMBER}, //$NON-NLS-1$
				new Object[] { 
					problem.getMessage(),
					new Integer(problem.isError() ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING), 
					new Integer(problem.getID()),
					new Integer(problem.getSourceStart()),
					new Integer(problem.getSourceEnd() + 1),
					new Integer(problem.getSourceLineNumber())
				});

// Do we need to do this?
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
		} catch(CoreException e) {
			throw internalException(e);
		}
	}
}

protected void updateProblemsFor(CompilationResult result) {
	// expect subclasses to override
}

protected IContainer getOutputFolder(IPath packagePath) throws CoreException {
	IFolder folder = outputFolder.getFolder(packagePath);
// when can this ever happen if builders build the package tree before compiling source files?
	if (!folder.exists()) {
		getOutputFolder(packagePath.removeLastSegments(1));
		folder.create(true, true, null);
	}
	return folder;
}

protected boolean isClassFileChanged(IFile file, String fileName, byte[] bytes, boolean isSecondaryType) throws CoreException {
	// In Incremental mode, compare the bytes against the previous file for structural changes
	return true;
}

protected char[] writeClassFile(ClassFile classFile, boolean isSecondaryType) throws CoreException {
	// Before writing out the class file, compare it to the previous file
	// If structural changes occured then add dependent source files
	String fileName = new String(classFile.fileName());
	IPath filePath = new Path(fileName);			
	IContainer container = outputFolder;
	if (filePath.segmentCount() > 1) {
		container = getOutputFolder(filePath.removeLastSegments(1));
		filePath = new Path(filePath.lastSegment());
	}

	IFile file = container.getFile(filePath.addFileExtension(JavaBuilder.ClassExtension));
	byte[] bytes = classFile.getBytes();
	if (isClassFileChanged(file, fileName, bytes, isSecondaryType)) {
		if (JavaBuilder.DEBUG)
			System.out.println("Writing class file " + file.getName());
		file.create(new ByteArrayInputStream(bytes), true, null);
	}
	return file.getName().toCharArray();
}
}