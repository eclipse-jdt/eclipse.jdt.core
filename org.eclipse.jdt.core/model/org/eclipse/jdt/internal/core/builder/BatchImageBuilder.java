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

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.Util;

import java.util.*;

public class BatchImageBuilder extends AbstractImageBuilder {

protected BatchImageBuilder(JavaBuilder javaBuilder) {
	super(javaBuilder);
	this.nameEnvironment.isIncrementalBuild = false;
}

public void build() {
	if (JavaBuilder.DEBUG)
		System.out.println("FULL build"); //$NON-NLS-1$

	try {
		notifier.subTask(Util.bind("build.cleaningOutput")); //$NON-NLS-1$
		JavaBuilder.removeProblemsAndTasksFor(javaBuilder.currentProject);
		cleanOutputFolders();
		notifier.updateProgressDelta(0.1f);

		notifier.subTask(Util.bind("build.analyzingSources")); //$NON-NLS-1$
		ArrayList sourceFiles = new ArrayList(33);
		addAllSourceFiles(sourceFiles);
		notifier.updateProgressDelta(0.15f);

		if (sourceFiles.size() > 0) {
			SourceFile[] allSourceFiles = new SourceFile[sourceFiles.size()];
			sourceFiles.toArray(allSourceFiles);

			notifier.setProgressPerCompilationUnit(0.75f / allSourceFiles.length);
			workQueue.addAll(allSourceFiles);
			compile(allSourceFiles);
		}
	} catch (CoreException e) {
		throw internalException(e);
	} finally {
		cleanUp();
	}
}

protected void addAllSourceFiles(final ArrayList sourceFiles) throws CoreException {
	for (int i = 0, l = sourceLocations.length; i < l; i++) {
		final ClasspathMultiDirectory sourceLocation = sourceLocations[i];
		final char[][] exclusionPatterns = sourceLocation.exclusionPatterns;
		sourceLocation.sourceFolder.accept(
			new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					if (exclusionPatterns != null && Util.isExcluded(resource, exclusionPatterns))
						return false;
					if (resource.getType() == IResource.FILE) {
						if (JavaBuilder.JAVA_EXTENSION.equalsIgnoreCase(resource.getFileExtension()))
							sourceFiles.add(new SourceFile((IFile) resource, sourceLocation, encoding));
						return false;
					}
					return true;
				}
			}
		);
		notifier.checkCancel();
	}
}

protected void cleanOutputFolders() throws CoreException {
	boolean deleteAll = JavaCore.CLEAN.equals(
		javaBuilder.javaProject.getOption(JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, true));
	ArrayList visited = new ArrayList(sourceLocations.length);
	next : for (int i = 0, l = sourceLocations.length; i < l; i++) {
		ClasspathMultiDirectory sourceLocation = sourceLocations[i];
		if (sourceLocation.hasIndependentOutputFolder) {
			IContainer outputFolder = sourceLocation.binaryFolder;
			if (visited.contains(outputFolder)) continue next;
			visited.add(outputFolder);
			if (deleteAll) {
				IResource[] members = outputFolder.members(); 
				for (int ii = 0, ll = members.length; ii < ll; ii++)
					members[ii].delete(IResource.FORCE, null);
			} else {
				outputFolder.accept(
					new IResourceVisitor() {
						public boolean visit(IResource resource) throws CoreException {
							if (resource.getType() == IResource.FILE) {
								if (JavaBuilder.CLASS_EXTENSION.equalsIgnoreCase(resource.getFileExtension()))
									resource.delete(IResource.FORCE, null);
								return false;
							}
							return true;
						}
					}
				);
			}
			copyExtraResourcesBack(sourceLocation, deleteAll);
		} else {
			final char[][] exclusionPatterns =
				sourceLocation.sourceFolder.equals(sourceLocation.binaryFolder)
					? sourceLocation.exclusionPatterns
					: null; // ignore exclusionPatterns if output folder == another source folder... not this one
			sourceLocation.binaryFolder.accept(
				new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						if (exclusionPatterns != null && Util.isExcluded(resource, exclusionPatterns))
							return false;
						if (resource.getType() == IResource.FILE) {
							if (JavaBuilder.CLASS_EXTENSION.equalsIgnoreCase(resource.getFileExtension()))
								resource.delete(IResource.FORCE, null);
							return false;
						}
						return true;
					}
				}
			);
			notifier.checkCancel();
		}
	}
}

protected void copyExtraResourcesBack(ClasspathMultiDirectory sourceLocation, final boolean deletedAll) throws CoreException {
	// When, if ever, does a builder need to copy resources files (not .java or .class) into the output folder?
	// If we wipe the output folder at the beginning of the build then all 'extra' resources must be copied to the output folder.

	final int segmentCount = sourceLocation.sourceFolder.getFullPath().segmentCount();
	final char[][] exclusionPatterns = sourceLocation.exclusionPatterns;
	final IContainer outputFolder = sourceLocation.binaryFolder;
	sourceLocation.sourceFolder.accept(
		new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (exclusionPatterns != null && Util.isExcluded(resource, exclusionPatterns))
					return false;
				switch(resource.getType()) {
					case IResource.FILE :
						String extension = resource.getFileExtension();
						if (JavaBuilder.JAVA_EXTENSION.equalsIgnoreCase(extension)) return false;
						if (JavaBuilder.CLASS_EXTENSION.equalsIgnoreCase(extension)) return false;
						if (javaBuilder.filterExtraResource(resource)) return false;

						IPath partialPath = resource.getFullPath().removeFirstSegments(segmentCount);
						IResource copiedResource = outputFolder.getFile(partialPath);
						if (copiedResource.exists()) {
							if (deletedAll) {
								createErrorFor(resource, Util.bind("build.duplicateResource")); //$NON-NLS-1$
								return false;
							}
							copiedResource.delete(IResource.FORCE, null); // last one wins
						}
						resource.copy(copiedResource.getFullPath(), IResource.FORCE, null);
						copiedResource.setDerived(true);
						return false;
					case IResource.FOLDER :
						if (resource.equals(outputFolder)) return false;
						if (javaBuilder.filterExtraResource(resource)) return false;

						getOutputFolder(resource.getFullPath().removeFirstSegments(segmentCount), outputFolder);
				}
				return true;
			}
		}
	);
}

public String toString() {
	return "batch image builder for:\n\tnew state: " + newState; //$NON-NLS-1$
}
}