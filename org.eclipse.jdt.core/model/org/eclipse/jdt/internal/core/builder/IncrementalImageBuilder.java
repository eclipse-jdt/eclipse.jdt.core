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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.classfmt.*;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;

import java.io.*;
import java.util.*;

/**
 * The incremental image builder
 */
public class IncrementalImageBuilder extends AbstractImageBuilder {

protected ArrayList sourceFiles;
protected ArrayList previousSourceFiles;
protected ArrayList qualifiedStrings;
protected ArrayList simpleStrings;
protected SimpleLookupTable secondaryTypesToRemove;
protected boolean hasStructuralChanges;

public static int MaxCompileLoop = 5; // perform a full build if it takes more than ? incremental compile loops

protected IncrementalImageBuilder(JavaBuilder javaBuilder) {
	super(javaBuilder);
	this.nameEnvironment.isIncrementalBuild = true;
	this.newState.copyFrom(javaBuilder.lastState);

	this.sourceFiles = new ArrayList(33);
	this.previousSourceFiles = null;
	this.qualifiedStrings = new ArrayList(33);
	this.simpleStrings = new ArrayList(33);
	this.hasStructuralChanges = false;
}

public boolean build(SimpleLookupTable deltas) {
	// initialize builder
	// walk this project's deltas, find changed source files
	// walk prereq projects' deltas, find changed class files & add affected source files
	//   use the build state # to skip the deltas for certain prereq projects
	//   ignore changed zip/jar files since they caused a full build
	// compile the source files & acceptResult()
	// compare the produced class files against the existing ones on disk
	// recompile all dependent source files of any type with structural changes or new/removed secondary type
	// keep a loop counter to abort & perform a full build

	if (JavaBuilder.DEBUG)
		System.out.println("INCREMENTAL build"); //$NON-NLS-1$

	try {
		resetCollections();

		notifier.subTask(Util.bind("build.analyzingDeltas")); //$NON-NLS-1$
		IResourceDelta sourceDelta = (IResourceDelta) deltas.get(javaBuilder.currentProject);
		if (sourceDelta != null)
			if (!findSourceFiles(sourceDelta)) return false;
		notifier.updateProgressDelta(0.10f);

		Object[] keyTable = deltas.keyTable;
		Object[] valueTable = deltas.valueTable;
		for (int i = 0, l = valueTable.length; i < l; i++) {
			IResourceDelta delta = (IResourceDelta) valueTable[i];
			if (delta != null) {
				ClasspathLocation[] classFoldersAndJars = (ClasspathLocation[]) javaBuilder.binaryLocationsPerProject.get(keyTable[i]);
				if (classFoldersAndJars != null)
					if (!findAffectedSourceFiles(delta, classFoldersAndJars)) return false;
			}
		}
		notifier.updateProgressDelta(0.10f);

		notifier.subTask(Util.bind("build.analyzingSources")); //$NON-NLS-1$
		addAffectedSourceFiles();
		notifier.updateProgressDelta(0.05f);

		int compileLoop = 0;
		float increment = 0.40f;
		while (sourceFiles.size() > 0) { // added to in acceptResult
			if (++compileLoop > MaxCompileLoop) {
				if (JavaBuilder.DEBUG)
					System.out.println("ABORTING incremental build... exceeded loop count"); //$NON-NLS-1$
				return false;
			}
			notifier.checkCancel();

			SourceFile[] allSourceFiles = new SourceFile[sourceFiles.size()];
			sourceFiles.toArray(allSourceFiles);
			resetCollections();

			workQueue.addAll(allSourceFiles);
			notifier.setProgressPerCompilationUnit(increment / allSourceFiles.length);
			increment = increment / 2;
			compile(allSourceFiles);
			removeSecondaryTypes();
			addAffectedSourceFiles();
		}
		if (this.hasStructuralChanges && javaBuilder.javaProject.hasCycleMarker())
			javaBuilder.mustPropagateStructuralChanges();
	} catch (AbortIncrementalBuildException e) {
		// abort the incremental build and let the batch builder handle the problem
		if (JavaBuilder.DEBUG)
			System.out.println("ABORTING incremental build... cannot find " + e.qualifiedTypeName + //$NON-NLS-1$
				". Could have been renamed inside its existing source file."); //$NON-NLS-1$
		return false;
	} catch (CoreException e) {
		throw internalException(e);
	} finally {
		cleanUp();
	}
	return true;
}

protected void addAffectedSourceFiles() {
	if (qualifiedStrings.isEmpty() && simpleStrings.isEmpty()) return;

	// the qualifiedStrings are of the form 'p1/p2' & the simpleStrings are just 'X'
	char[][][] qualifiedNames = ReferenceCollection.internQualifiedNames(qualifiedStrings);
	// if a well known qualified name was found then we can skip over these
	if (qualifiedNames.length < qualifiedStrings.size())
		qualifiedNames = null;
	char[][] simpleNames = ReferenceCollection.internSimpleNames(simpleStrings);
	// if a well known name was found then we can skip over these
	if (simpleNames.length < simpleStrings.size())
		simpleNames = null;

	Object[] keyTable = newState.references.keyTable;
	Object[] valueTable = newState.references.valueTable;
	next : for (int i = 0, l = valueTable.length; i < l; i++) {
		ReferenceCollection refs = (ReferenceCollection) valueTable[i];
		if (refs != null && refs.includes(qualifiedNames, simpleNames)) {
			String typeLocator = (String) keyTable[i];
			IFile file = javaBuilder.currentProject.getFile(typeLocator);
			if (file.exists()) {
				ClasspathMultiDirectory md = sourceLocations[0];
				if (sourceLocations.length > 1) {
					IPath sourceFileFullPath = file.getFullPath();
					for (int j = 0, m = sourceLocations.length; j < m; j++) {
						if (sourceLocations[j].sourceFolder.getFullPath().isPrefixOf(sourceFileFullPath)) {
							md = sourceLocations[j];
							if (md.exclusionPatterns == null || !Util.isExcluded(file, md.exclusionPatterns))
								break;
						}
					}
				}
				SourceFile sourceFile = new SourceFile(file, md, encoding);
				if (sourceFiles.contains(sourceFile)) continue next;
				if (compiledAllAtOnce && previousSourceFiles != null && previousSourceFiles.contains(sourceFile))
					continue next; // can skip previously compiled files since already saw hierarchy related problems

				if (JavaBuilder.DEBUG)
					System.out.println("  adding affected source file " + typeLocator); //$NON-NLS-1$
				sourceFiles.add(sourceFile);
			}
		}
	}
}

protected void addDependentsOf(IPath path, boolean hasStructuralChanges) {
	if (hasStructuralChanges) {
		newState.tagAsStructurallyChanged();
		this.hasStructuralChanges = true;
	}
	// the qualifiedStrings are of the form 'p1/p2' & the simpleStrings are just 'X'
	path = path.setDevice(null);
	String packageName = path.removeLastSegments(1).toString();
	if (!qualifiedStrings.contains(packageName))
		qualifiedStrings.add(packageName);
	String typeName = path.lastSegment();
	int memberIndex = typeName.indexOf('$');
	if (memberIndex > 0)
		typeName = typeName.substring(0, memberIndex);
	if (!simpleStrings.contains(typeName)) {
		if (JavaBuilder.DEBUG)
			System.out.println("  will look for dependents of " //$NON-NLS-1$
				+ typeName + " in " + packageName); //$NON-NLS-1$
		simpleStrings.add(typeName);
	}
}

protected void cleanUp() {
	super.cleanUp();

	this.sourceFiles = null;
	this.previousSourceFiles = null;
	this.qualifiedStrings = null;
	this.simpleStrings = null;
	this.secondaryTypesToRemove = null;
	this.hasStructuralChanges = false;
}

protected boolean findAffectedSourceFiles(IResourceDelta delta, ClasspathLocation[] classFoldersAndJars) {
	for (int i = 0, l = classFoldersAndJars.length; i < l; i++) {
		ClasspathLocation bLocation = classFoldersAndJars[i];
		// either a .class file folder or a zip/jar file
		if (bLocation != null) { // skip unchanged output folder
			IPath p = bLocation.getProjectRelativePath();
			if (p != null) {
				IResourceDelta binaryDelta = delta.findMember(p);
				if (binaryDelta != null) {
					if (bLocation instanceof ClasspathJar) {
						if (JavaBuilder.DEBUG)
							System.out.println("ABORTING incremental build... found delta to jar/zip file"); //$NON-NLS-1$
						return false; // do full build since jar file was changed (added/removed were caught as classpath change)
					}
					if (binaryDelta.getKind() == IResourceDelta.ADDED || binaryDelta.getKind() == IResourceDelta.REMOVED) {
						if (JavaBuilder.DEBUG)
							System.out.println("ABORTING incremental build... found added/removed binary folder"); //$NON-NLS-1$
						return false; // added/removed binary folder should not make it here (classpath change), but handle anyways
					}
					int segmentCount = binaryDelta.getFullPath().segmentCount();
					IResourceDelta[] children = binaryDelta.getAffectedChildren(); // .class files from class folder
					for (int j = 0, m = children.length; j < m; j++)
						findAffectedSourceFiles(children[j], segmentCount);
					notifier.checkCancel();
				}
			}
		}
	}
	return true;
}

protected void findAffectedSourceFiles(IResourceDelta binaryDelta, int segmentCount) {
	// When a package becomes a type or vice versa, expect 2 deltas,
	// one on the folder & one on the class file
	IResource resource = binaryDelta.getResource();
	switch(resource.getType()) {
		case IResource.FOLDER :
			switch (binaryDelta.getKind()) {
				case IResourceDelta.ADDED :
				case IResourceDelta.REMOVED :
					IPath packagePath = resource.getFullPath().removeFirstSegments(segmentCount);
					String packageName = packagePath.toString();
					if (binaryDelta.getKind() == IResourceDelta.ADDED) {
						// see if any known source file is from the same package... classpath already includes new package
						if (!newState.isKnownPackage(packageName)) {
							if (JavaBuilder.DEBUG)
								System.out.println("Found added package " + packageName); //$NON-NLS-1$
							addDependentsOf(packagePath, false);
							return;
						}
						if (JavaBuilder.DEBUG)
							System.out.println("Skipped dependents of added package " + packageName); //$NON-NLS-1$
					} else {
						// see if the package still exists on the classpath
						if (!nameEnvironment.isPackage(packageName)) {
							if (JavaBuilder.DEBUG)
								System.out.println("Found removed package " + packageName); //$NON-NLS-1$
							addDependentsOf(packagePath, false);
							return;
						}
						if (JavaBuilder.DEBUG)
							System.out.println("Skipped dependents of removed package " + packageName); //$NON-NLS-1$
					}
					// fall thru & traverse the sub-packages and .class files
				case IResourceDelta.CHANGED :
					IResourceDelta[] children = binaryDelta.getAffectedChildren();
					for (int i = 0, l = children.length; i < l; i++)
						findAffectedSourceFiles(children[i], segmentCount);
			}
			return;
		case IResource.FILE :
			if (Util.isClassFileName(resource.getName())) {
				IPath typePath = resource.getFullPath().removeFirstSegments(segmentCount).removeFileExtension();
				switch (binaryDelta.getKind()) {
					case IResourceDelta.ADDED :
					case IResourceDelta.REMOVED :
						if (JavaBuilder.DEBUG)
							System.out.println("Found added/removed class file " + typePath); //$NON-NLS-1$
						addDependentsOf(typePath, false);
						return;
					case IResourceDelta.CHANGED :
						if ((binaryDelta.getFlags() & IResourceDelta.CONTENT) == 0)
							return; // skip it since it really isn't changed
						if (JavaBuilder.DEBUG)
							System.out.println("Found changed class file " + typePath); //$NON-NLS-1$
						addDependentsOf(typePath, false);
				}
				return;
			}
	}
}

protected boolean findSourceFiles(IResourceDelta delta) throws CoreException {
	for (int i = 0, l = sourceLocations.length; i < l; i++) {
		ClasspathMultiDirectory md = sourceLocations[i];
		if (md.sourceFolder.equals(javaBuilder.currentProject)) {
			// skip nested source & output folders when the project is a source folder
			int segmentCount = delta.getFullPath().segmentCount();
			IResourceDelta[] children = delta.getAffectedChildren();
			for (int j = 0, m = children.length; j < m; j++)
				if (!isExcludedFromProject(children[i].getFullPath()))
					findSourceFiles(children[j], md, segmentCount);
		} else {
			IResourceDelta sourceDelta = delta.findMember(md.sourceFolder.getProjectRelativePath());

			if (sourceDelta != null) {
				if (sourceDelta.getKind() == IResourceDelta.REMOVED) {
					if (JavaBuilder.DEBUG)
						System.out.println("ABORTING incremental build... found removed source folder"); //$NON-NLS-1$
					return false; // removed source folder should not make it here, but handle anyways (ADDED is supported)
				}
				int segmentCount = sourceDelta.getFullPath().segmentCount();
				IResourceDelta[] children = sourceDelta.getAffectedChildren();
				for (int j = 0, m = children.length; j < m; j++)
					findSourceFiles(children[j], md, segmentCount);
			}
		}
		notifier.checkCancel();
	}
	return true;
}

protected void findSourceFiles(IResourceDelta sourceDelta, ClasspathMultiDirectory md, int segmentCount) throws CoreException {
	// When a package becomes a type or vice versa, expect 2 deltas,
	// one on the folder & one on the source file
	IResource resource = sourceDelta.getResource();
	if (md.exclusionPatterns != null && Util.isExcluded(resource, md.exclusionPatterns)) return;
	switch(resource.getType()) {
		case IResource.FOLDER :
			switch (sourceDelta.getKind()) {
				case IResourceDelta.ADDED :
					IPath addedPackagePath = resource.getFullPath().removeFirstSegments(segmentCount);
					createFolder(addedPackagePath, md.binaryFolder); // ensure package exists in the output folder
					// add dependents even when the package thinks it exists to be on the safe side
					if (JavaBuilder.DEBUG)
						System.out.println("Found added package " + addedPackagePath); //$NON-NLS-1$
					addDependentsOf(addedPackagePath, true);
					// fall thru & collect all the source files
				case IResourceDelta.CHANGED :
					IResourceDelta[] children = sourceDelta.getAffectedChildren();
					for (int i = 0, l = children.length; i < l; i++)
						findSourceFiles(children[i], md, segmentCount);
					return;
				case IResourceDelta.REMOVED :
					IPath removedPackagePath = resource.getFullPath().removeFirstSegments(segmentCount);
					if (sourceLocations.length > 1) {
						for (int i = 0, l = sourceLocations.length; i < l; i++) {
							if (sourceLocations[i].sourceFolder.getFolder(removedPackagePath).exists()) {
								// only a package fragment was removed, same as removing multiple source files
								createFolder(removedPackagePath, md.binaryFolder); // ensure package exists in the output folder
								IResourceDelta[] removedChildren = sourceDelta.getAffectedChildren();
								for (int j = 0, m = removedChildren.length; j < m; j++)
									findSourceFiles(removedChildren[j], md, segmentCount);
								return;
							}
						}
					}
					IFolder removedPackageFolder = md.binaryFolder.getFolder(removedPackagePath);
					if (removedPackageFolder.exists())
						removedPackageFolder.delete(IResource.FORCE, null);
					// add dependents even when the package thinks it does not exist to be on the safe side
					if (JavaBuilder.DEBUG)
						System.out.println("Found removed package " + removedPackagePath); //$NON-NLS-1$
					addDependentsOf(removedPackagePath, true);
					newState.removePackage(sourceDelta);
			}
			return;
		case IResource.FILE :
			String resourceName = resource.getName();
			if (Util.isJavaFileName(resourceName)) {
				IPath typePath = resource.getFullPath().removeFirstSegments(segmentCount).removeFileExtension();
				String typeLocator = resource.getProjectRelativePath().toString();
				switch (sourceDelta.getKind()) {
					case IResourceDelta.ADDED :
						if (JavaBuilder.DEBUG)
							System.out.println("Compile this added source file " + typeLocator); //$NON-NLS-1$
						sourceFiles.add(new SourceFile((IFile) resource, md, encoding));
						String typeName = typePath.toString();
						if (!newState.isDuplicateLocator(typeName, typeLocator)) { // adding dependents results in 2 duplicate errors
							if (JavaBuilder.DEBUG)
								System.out.println("Found added source file " + typeName); //$NON-NLS-1$
							addDependentsOf(typePath, true);
						}
						return;
					case IResourceDelta.REMOVED :
						char[][] definedTypeNames = newState.getDefinedTypeNamesFor(typeLocator);
						if (definedTypeNames == null) { // defined a single type matching typePath
							removeClassFile(typePath, md.binaryFolder);
							if ((sourceDelta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
								// remove problems and tasks for a compilation unit that is being moved (to another package or renamed)
								// if the target file is a compilation unit, the new cu will be recompiled
								// if the target file is a non-java resource, then markers are removed
								// see bug 2857
								IResource movedFile = javaBuilder.workspaceRoot.getFile(sourceDelta.getMovedToPath());
								JavaBuilder.removeProblemsAndTasksFor(movedFile); 
							}
						} else {
							if (JavaBuilder.DEBUG)
								System.out.println("Found removed source file " + typePath.toString()); //$NON-NLS-1$
							addDependentsOf(typePath, true); // add dependents of the source file since it may be involved in a name collision
							if (definedTypeNames.length > 0) { // skip it if it failed to successfully define a type
								IPath packagePath = typePath.removeLastSegments(1);
								for (int i = 0, l = definedTypeNames.length; i < l; i++)
									removeClassFile(packagePath.append(new String(definedTypeNames[i])), md.binaryFolder);
							}
						}
						newState.removeLocator(typeLocator);
						return;
					case IResourceDelta.CHANGED :
						if ((sourceDelta.getFlags() & IResourceDelta.CONTENT) == 0)
							return; // skip it since it really isn't changed
						if (JavaBuilder.DEBUG)
							System.out.println("Compile this changed source file " + typeLocator); //$NON-NLS-1$
						sourceFiles.add(new SourceFile((IFile) resource, md, encoding));
				}
				return;
			} else if (Util.isClassFileName(resourceName)) {
				return; // skip class files
			} else if (md.hasIndependentOutputFolder) {
				if (javaBuilder.filterExtraResource(resource)) return;

				// copy all other resource deltas to the output folder
				IPath resourcePath = resource.getFullPath().removeFirstSegments(segmentCount);
				IResource outputFile = md.binaryFolder.getFile(resourcePath);
				switch (sourceDelta.getKind()) {
					case IResourceDelta.ADDED :
						if (outputFile.exists()) {
							if (JavaBuilder.DEBUG)
								System.out.println("Deleting existing file " + resourcePath); //$NON-NLS-1$
							outputFile.delete(IResource.FORCE, null);
						}
						if (JavaBuilder.DEBUG)
							System.out.println("Copying added file " + resourcePath); //$NON-NLS-1$
						createFolder(resourcePath.removeLastSegments(1), md.binaryFolder); // ensure package exists in the output folder
						resource.copy(outputFile.getFullPath(), IResource.FORCE, null);
						outputFile.setDerived(true);
						return;
					case IResourceDelta.REMOVED :
						if (outputFile.exists()) {
							if (JavaBuilder.DEBUG)
								System.out.println("Deleting removed file " + resourcePath); //$NON-NLS-1$
							outputFile.delete(IResource.FORCE, null);
						}
						return;
					case IResourceDelta.CHANGED :
						if ((sourceDelta.getFlags() & IResourceDelta.CONTENT) == 0)
							return; // skip it since it really isn't changed
						if (outputFile.exists()) {
							if (JavaBuilder.DEBUG)
								System.out.println("Deleting existing file " + resourcePath); //$NON-NLS-1$
							outputFile.delete(IResource.FORCE, null);
						}
						if (JavaBuilder.DEBUG)
							System.out.println("Copying changed file " + resourcePath); //$NON-NLS-1$
						createFolder(resourcePath.removeLastSegments(1), md.binaryFolder); // ensure package exists in the output folder
						resource.copy(outputFile.getFullPath(), IResource.FORCE, null);
						outputFile.setDerived(true);
				}
				return;
			}
	}
}

protected void finishedWith(String sourceLocator, CompilationResult result, char[] mainTypeName, ArrayList definedTypeNames, ArrayList duplicateTypeNames) throws CoreException {
	char[][] previousTypeNames = newState.getDefinedTypeNamesFor(sourceLocator);
	if (previousTypeNames == null)
		previousTypeNames = new char[][] {mainTypeName};
	IPath packagePath = null;
	next : for (int i = 0, l = previousTypeNames.length; i < l; i++) {
		char[] previous = previousTypeNames[i];
		for (int j = 0, m = definedTypeNames.size(); j < m; j++)
			if (CharOperation.equals(previous, (char[]) definedTypeNames.get(j)))
				continue next;

		SourceFile sourceFile = (SourceFile) result.getCompilationUnit();
		if (packagePath == null) {
			int count = sourceFile.sourceLocation.sourceFolder.getFullPath().segmentCount();
			packagePath = sourceFile.resource.getFullPath().removeFirstSegments(count).removeLastSegments(1);
		}
		if (secondaryTypesToRemove == null)
			this.secondaryTypesToRemove = new SimpleLookupTable();
		ArrayList types = (ArrayList) secondaryTypesToRemove.get(sourceFile.sourceLocation.binaryFolder);
		if (types == null)
			types = new ArrayList(definedTypeNames.size());
		types.add(packagePath.append(new String(previous)));
		secondaryTypesToRemove.put(sourceFile.sourceLocation.binaryFolder, types);
	}
	super.finishedWith(sourceLocator, result, mainTypeName, definedTypeNames, duplicateTypeNames);
}

protected void removeClassFile(IPath typePath, IContainer outputFolder) throws CoreException {
	if (typePath.lastSegment().indexOf('$') == -1) { // is not a nested type
		newState.removeQualifiedTypeName(typePath.toString());
		// add dependents even when the type thinks it does not exist to be on the safe side
		if (JavaBuilder.DEBUG)
			System.out.println("Found removed type " + typePath); //$NON-NLS-1$
		addDependentsOf(typePath, true); // when member types are removed, their enclosing type is structurally changed
	}
	IFile classFile = outputFolder.getFile(typePath.addFileExtension(JavaBuilder.CLASS_EXTENSION));
	if (classFile.exists()) {
		if (JavaBuilder.DEBUG)
			System.out.println("Deleting class file of removed type " + typePath); //$NON-NLS-1$
		classFile.delete(IResource.FORCE, null);
	}
}

protected void removeSecondaryTypes() throws CoreException {
	if (secondaryTypesToRemove != null) { // delayed deleting secondary types until the end of the compile loop
		Object[] keyTable = secondaryTypesToRemove.keyTable;
		Object[] valueTable = secondaryTypesToRemove.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++) {
			IContainer outputFolder = (IContainer) keyTable[i];
			if (outputFolder != null) {
				ArrayList paths = (ArrayList) valueTable[i];
				for (int j = 0, m = paths.size(); j < m; j++)
					removeClassFile((IPath) paths.get(j), outputFolder);
			}
		}
		this.secondaryTypesToRemove = null;
		if (previousSourceFiles != null && previousSourceFiles.size() > 1)
			this.previousSourceFiles = null; // cannot optimize recompile case when a secondary type is deleted
	}
}

protected void resetCollections() {
	previousSourceFiles = sourceFiles.isEmpty() ? null : (ArrayList) sourceFiles.clone();

	sourceFiles.clear();
	qualifiedStrings.clear();
	simpleStrings.clear();
	workQueue.clear();
}

protected void updateProblemsFor(SourceFile sourceFile, CompilationResult result) throws CoreException {
	IMarker[] markers = JavaBuilder.getProblemsFor(sourceFile.resource);
	IProblem[] problems = result.getProblems();
	if (problems == null && markers.length == 0) return;

	notifier.updateProblemCounts(markers, problems);
	JavaBuilder.removeProblemsFor(sourceFile.resource);
	storeProblemsFor(sourceFile, problems);
}

protected void updateTasksFor(SourceFile sourceFile, CompilationResult result) throws CoreException {
	IMarker[] markers = JavaBuilder.getTasksFor(sourceFile.resource);
	IProblem[] tasks = result.getTasks();
	if (tasks == null && markers.length == 0) return;

	JavaBuilder.removeTasksFor(sourceFile.resource);
	storeTasksFor(sourceFile, tasks);
}

protected void writeClassFileBytes(byte[] bytes, IFile file, String qualifiedFileName, boolean isSecondaryType) throws CoreException {
	// Before writing out the class file, compare it to the previous file
	// If structural changes occured then add dependent source files
	if (file.exists()) {
		if (writeClassFileCheck(file, qualifiedFileName, bytes)) {
			if (JavaBuilder.DEBUG)
				System.out.println("Writing changed class file " + file.getName());//$NON-NLS-1$
			file.setContents(new ByteArrayInputStream(bytes), true, false, null);
			if (!file.isDerived())
				file.setDerived(true);
		} else if (JavaBuilder.DEBUG) {
			System.out.println("Skipped over unchanged class file " + file.getName());//$NON-NLS-1$
		}
	} else {
		if (isSecondaryType)
			addDependentsOf(new Path(qualifiedFileName), true); // new secondary type
		if (JavaBuilder.DEBUG)
			System.out.println("Writing new class file " + file.getName());//$NON-NLS-1$
		file.create(new ByteArrayInputStream(bytes), IResource.FORCE, null);
		file.setDerived(true);
	}
}

protected boolean writeClassFileCheck(IFile file, String fileName, byte[] newBytes) throws CoreException {
	try {
		byte[] oldBytes = Util.getResourceContentsAsByteArray(file);
		notEqual : if (newBytes.length == oldBytes.length) {
			for (int i = newBytes.length; --i >= 0;)
				if (newBytes[i] != oldBytes[i]) break notEqual;
			return false; // bytes are identical so skip them
		}
		IPath location = file.getLocation();
		if (location == null) return false; // unable to determine location of this class file
		ClassFileReader reader = new ClassFileReader(oldBytes, location.toString().toCharArray());
		// ignore local types since they're only visible inside a single method
		if (!(reader.isLocal() || reader.isAnonymous()) && reader.hasStructuralChanges(newBytes)) {
			if (JavaBuilder.DEBUG)
				System.out.println("Type has structural changes " + fileName); //$NON-NLS-1$
			addDependentsOf(new Path(fileName), true);
		}
	} catch (ClassFormatException e) {
		addDependentsOf(new Path(fileName), true);
	}
	return true;
}

public String toString() {
	return "incremental image builder for:\n\tnew state: " + newState; //$NON-NLS-1$
}


/* Debug helper

static void dump(IResourceDelta delta) {
	StringBuffer buffer = new StringBuffer();
	IPath path = delta.getFullPath();
	for (int i = path.segmentCount(); --i > 0;)
		buffer.append("  ");
	switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			buffer.append('+');
			break;
		case IResourceDelta.REMOVED:
			buffer.append('-');
			break;
		case IResourceDelta.CHANGED:
			buffer.append('*');
			break;
		case IResourceDelta.NO_CHANGE:
			buffer.append('=');
			break;
		default:
			buffer.append('?');
			break;
	}
	buffer.append(path);
	System.out.println(buffer.toString());
	IResourceDelta[] children = delta.getAffectedChildren();
	for (int i = 0, l = children.length; i < l; i++)
		dump(children[i]);
}
*/
}