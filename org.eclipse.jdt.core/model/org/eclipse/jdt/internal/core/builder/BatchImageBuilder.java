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
import org.eclipse.jdt.internal.core.JavaModelManager;
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
		JavaModelManager.getJavaModelManager().deltaProcessor.addForRefresh(javaBuilder.javaProject);
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

		if (javaBuilder.javaProject.hasCycleMarker())
			javaBuilder.mustPropagateStructuralChanges();
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
			new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) throws CoreException {
					IResource resource = null;
					if (exclusionPatterns != null) {
						resource = proxy.requestResource();
						if (Util.isExcluded(resource, exclusionPatterns)) return false;
					}
					if (proxy.getType() == IResource.FILE) {
						if (Util.isJavaFileName(proxy.getName())) {
							if (resource == null)
								resource = proxy.requestResource();
							sourceFiles.add(new SourceFile((IFile) resource, sourceLocation, encoding));
						}
						return false;
					}
					return true;
				}
			},
			IResource.NONE
		);
		notifier.checkCancel();
	}
}

protected void cleanOutputFolders() throws CoreException {
	boolean deleteAll = JavaCore.CLEAN.equals(
		javaBuilder.javaProject.getOption(JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, true));
	if (deleteAll) {
		ArrayList visited = new ArrayList(sourceLocations.length);
		for (int i = 0, l = sourceLocations.length; i < l; i++) {
			notifier.subTask(Util.bind("build.cleaningOutput")); //$NON-NLS-1$
			ClasspathMultiDirectory sourceLocation = sourceLocations[i];
			if (sourceLocation.hasIndependentOutputFolder) {
				IContainer outputFolder = sourceLocation.binaryFolder;
				if (!visited.contains(outputFolder)) {
					visited.add(outputFolder);
					IResource[] members = outputFolder.members(); 
					for (int j = 0, m = members.length; j < m; j++)
						members[j].delete(IResource.FORCE, null);
				}
				copyExtraResourcesBack(sourceLocation, deleteAll);
			} else {
				boolean isOutputFolder = sourceLocation.sourceFolder.equals(sourceLocation.binaryFolder);
				final char[][] exclusionPatterns =
					isOutputFolder
						? sourceLocation.exclusionPatterns
						: null; // ignore exclusionPatterns if output folder == another source folder... not this one
				sourceLocation.binaryFolder.accept(
					new IResourceProxyVisitor() {
						public boolean visit(IResourceProxy proxy) throws CoreException {
							IResource resource = null;
							if (exclusionPatterns != null) {
								resource = proxy.requestResource();
								if (Util.isExcluded(resource, exclusionPatterns)) return false;
							}
							if (proxy.getType() == IResource.FILE) {
								if (Util.isClassFileName(proxy.getName())) {
									if (resource == null)
										resource = proxy.requestResource();
									resource.delete(IResource.FORCE, null);
								}
								return false;
							}
							return true;
						}
					},
					IResource.NONE
				);
				if (!isOutputFolder)
					copyPackages(sourceLocation);
			}
			notifier.checkCancel();
		}
	} else {
		for (int i = 0, l = sourceLocations.length; i < l; i++) {
			ClasspathMultiDirectory sourceLocation = sourceLocations[i];
			if (sourceLocation.hasIndependentOutputFolder)
				copyExtraResourcesBack(sourceLocation, deleteAll);
			else if (!sourceLocation.sourceFolder.equals(sourceLocation.binaryFolder))
				copyPackages(sourceLocation); // output folder is different from source folder
			notifier.checkCancel();
		}
	}
}

protected void copyExtraResourcesBack(ClasspathMultiDirectory sourceLocation, final boolean deletedAll) throws CoreException {
	// When, if ever, does a builder need to copy resources files (not .java or .class) into the output folder?
	// If we wipe the output folder at the beginning of the build then all 'extra' resources must be copied to the output folder.

	notifier.subTask(Util.bind("build.copyingResources")); //$NON-NLS-1$
	final int segmentCount = sourceLocation.sourceFolder.getFullPath().segmentCount();
	final char[][] exclusionPatterns = sourceLocation.exclusionPatterns;
	final IContainer outputFolder = sourceLocation.binaryFolder;
	sourceLocation.sourceFolder.accept(
		new IResourceProxyVisitor() {
			public boolean visit(IResourceProxy proxy) throws CoreException {
				IResource resource = null;
				switch(proxy.getType()) {
					case IResource.FILE :
						if (Util.isJavaFileName(proxy.getName()) || Util.isClassFileName(proxy.getName())) return false;

						resource = proxy.requestResource();
						if (javaBuilder.filterExtraResource(resource)) return false;
						if (exclusionPatterns != null && Util.isExcluded(resource, exclusionPatterns))
							return false;

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
						resource = proxy.requestResource();
						if (resource.equals(outputFolder)) return false;
						if (javaBuilder.filterExtraResource(resource)) return false;
						if (exclusionPatterns != null && Util.isExcluded(resource, exclusionPatterns))
							return false;

						createFolder(resource.getFullPath().removeFirstSegments(segmentCount), outputFolder);
				}
				return true;
			}
		},
		IResource.NONE
	);
}

protected void copyPackages(ClasspathMultiDirectory sourceLocation) throws CoreException {
	final int segmentCount = sourceLocation.sourceFolder.getFullPath().segmentCount();
	final char[][] exclusionPatterns = sourceLocation.exclusionPatterns;
	final IContainer outputFolder = sourceLocation.binaryFolder;
	sourceLocation.sourceFolder.accept(
		new IResourceProxyVisitor() {
			public boolean visit(IResourceProxy proxy) throws CoreException {
				switch(proxy.getType()) {
					case IResource.FILE :
						return false;
					case IResource.FOLDER :
						IResource resource = proxy.requestResource();
						if (resource.equals(outputFolder)) return false;
						if (javaBuilder.filterExtraResource(resource)) return false;
						if (exclusionPatterns != null && Util.isExcluded(resource, exclusionPatterns))
							return false;

						createFolder(resource.getFullPath().removeFirstSegments(segmentCount), outputFolder);
				}
				return true;
			}
		},
		IResource.NONE
	);
}

public String toString() {
	return "batch image builder for:\n\tnew state: " + newState; //$NON-NLS-1$
}
}