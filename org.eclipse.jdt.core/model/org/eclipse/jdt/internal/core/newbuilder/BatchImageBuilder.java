package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.core.*;

import java.util.*;

public class BatchImageBuilder extends AbstractImageBuilder {

protected BatchImageBuilder(JavaBuilder javaBuilder) {
	super(javaBuilder);
}

public void build() {
	try {
		notifier.subTask(Util.bind("build.scrubbingOutput")); //$NON-NLS-1$
		scrubOutputFolder();
		notifier.updateProgressDelta(0.05f);
		removeProblemsFor(javaBuilder.currentProject);
		notifier.updateProgressDelta(0.05f);
		notifier.checkCancel();

		notifier.subTask(Util.bind("build.analyzingSources")); //$NON-NLS-1$
		ArrayList locations = new ArrayList(33);
		ArrayList typeNames = new ArrayList(33);
		addAllSourceFiles(locations, typeNames);
		notifier.updateProgressDelta(0.15f);
		notifier.checkCancel();

		if (locations.size() > 0) {
			String[] allSourceFiles = new String[locations.size()];
			locations.toArray(allSourceFiles);
			String[] initialTypeNames = new String[typeNames.size()];
			typeNames.toArray(initialTypeNames);

			notifier.setProgressPerCompilationUnit(0.75f / allSourceFiles.length);
			workQueue.addAll(allSourceFiles);
			compile(allSourceFiles, initialTypeNames);
		}
	} catch (CoreException e) {
		throw internalException(e);
	} finally {
		cleanUp();
	}
}

protected void addAllSourceFiles(final ArrayList locations, final ArrayList typeNames) throws CoreException {
	for (int i = 0, length = sourceFolders.length; i < length; i++) {
		final int count = sourceFolders[i].getFullPath().segmentCount();
		sourceFolders[i].accept(
			new IResourceVisitor() {
				public boolean visit(IResource resource) {
					if (resource.getType() == IResource.FILE) {
						if (JavaBuilder.JavaExtension.equalsIgnoreCase(resource.getFileExtension())) {
							locations.add(resource.getLocation().toString());
							typeNames.add(
								resource.getFullPath().removeFirstSegments(count).removeFileExtension().toString());
						}
						return false;
					}
					return true;
				}
			}
		);
	}
}

protected void scrubOutputFolder() throws CoreException {
	if (hasSeparateOutputFolder) {
		// outputPath is not on the class path so wipe it clean then copy extra resources back
		IResource[] members = outputFolder.members(); 
		for (int i = 0, length = members.length; i < length; i++)
				members[i].delete(true, null);
		copyExtraResourcesBack();
	} else {
		// outputPath == the source folder so just remove class files
		outputFolder.accept(
			new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					if (resource.getType() == IResource.FILE) {
						if (JavaBuilder.ClassExtension.equalsIgnoreCase(resource.getFileExtension()))
							resource.delete(true, null);
						return false;
					}
					return true;
				}
			}
		);
	}
}

protected void copyExtraResourcesBack() throws CoreException {
	// When, if ever, does a builder need to copy resources files (not .java
	// or .class) into the output folder?
	// If we wipe the output folder at the beginning of the build then all 'extra'
	// resources must be copied to the output folder.

	final IPath outputPath = outputFolder.getFullPath();
	for (int i = 0, length = sourceFolders.length; i < length; i++) {
		IContainer sourceFolder = sourceFolders[i];
		final IPath sourcePath = sourceFolder.getFullPath();
		final int segmentCount = sourcePath.segmentCount();
		sourceFolder.accept(
			new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					switch(resource.getType()) {
						case IResource.FILE :
							String extension = resource.getFileExtension();
							if (JavaBuilder.JavaExtension.equalsIgnoreCase(extension)) return false;
							if (JavaBuilder.ClassExtension.equalsIgnoreCase(extension)) return false;

							resource.copy(
								outputPath.append(
									resource.getFullPath().removeFirstSegments(segmentCount)),
								true,
								null);
							return false;
						case IResource.FOLDER :
							if (resource.getFullPath().equals(outputPath)) return false;
							if (resource.getFullPath().equals(sourcePath)) return true;

							getOutputFolder(resource.getFullPath().removeFirstSegments(segmentCount));
					}
					return true;
				}
			}
		);
	}
}

protected void updateProblemsFor(CompilationResult result) {
	IProblem[] problems = result.getProblems();
	if (problems == null || problems.length == 0) return;

	notifier.updateProblemCounts(null, problems);

	IPath filePath = new Path(new String(result.getFileName()));
	IResource resource = javaBuilder.workspaceRoot.getFileForLocation(filePath);
	storeProblemsFor(resource, problems);
}

public String toString() {
	return "batch image builder for:\n\tnew state: " + newState; //$NON-NLS-1$
}
}