/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;

import java.util.*;

public class BatchImageBuilder extends AbstractImageBuilder {

	IncrementalImageBuilder incrementalBuilder; // if annotations or secondary types have to be processed after the compile loop
	ArrayList missingSecondaryTypes; // qualified names for any secondary types found after the first compile loop

protected BatchImageBuilder(JavaBuilder javaBuilder, boolean buildStarting) {
	super(javaBuilder, buildStarting, null);
	this.nameEnvironment.isIncrementalBuild = false;
	this.incrementalBuilder = null;
	this.missingSecondaryTypes = null;
}

public void build() {
	if (JavaBuilder.DEBUG)
		System.out.println("FULL build"); //$NON-NLS-1$

	try {
		notifier.subTask(Messages.bind(Messages.build_cleaningOutput, this.javaBuilder.currentProject.getName()));
		JavaBuilder.removeProblemsAndTasksFor(javaBuilder.currentProject);
		cleanOutputFolders(true);
		notifier.updateProgressDelta(0.05f);

		notifier.subTask(Messages.build_analyzingSources); 
		ArrayList sourceFiles = new ArrayList(33);
		addAllSourceFiles(sourceFiles);
		notifier.updateProgressDelta(0.10f);

		if (sourceFiles.size() > 0) {
			SourceFile[] allSourceFiles = new SourceFile[sourceFiles.size()];
			sourceFiles.toArray(allSourceFiles);

			notifier.setProgressPerCompilationUnit(0.75f / allSourceFiles.length);
			workQueue.addAll(allSourceFiles);
			compile(allSourceFiles);

			if (this.missingSecondaryTypes != null && !this.missingSecondaryTypes.isEmpty())
				rebuildTypesAffectedByMissingSecondaryTypes();
			if (this.incrementalBuilder != null)
				this.incrementalBuilder.buildAfterBatchBuild();
		}

		if (javaBuilder.javaProject.hasCycleMarker())
			javaBuilder.mustPropagateStructuralChanges();
	} catch (CoreException e) {
		throw internalException(e);
	} finally {
		cleanUp();
	}
}

protected void acceptSecondaryType(ClassFile classFile) {
	if (this.missingSecondaryTypes != null)
		this.missingSecondaryTypes.add(classFile.fileName());
}

protected void addAllSourceFiles(final ArrayList sourceFiles) throws CoreException {
	for (int i = 0, l = sourceLocations.length; i < l; i++) {
		final ClasspathMultiDirectory sourceLocation = sourceLocations[i];
		final char[][] exclusionPatterns = sourceLocation.exclusionPatterns;
		final char[][] inclusionPatterns = sourceLocation.inclusionPatterns;
		final boolean isAlsoProject = sourceLocation.sourceFolder.equals(javaBuilder.currentProject);
		sourceLocation.sourceFolder.accept(
			new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) throws CoreException {
					IResource resource = null;
					switch(proxy.getType()) {
						case IResource.FILE :
							if (exclusionPatterns != null || inclusionPatterns != null) {
								resource = proxy.requestResource();
								if (Util.isExcluded(resource, inclusionPatterns, exclusionPatterns)) return false;
							}
							if (org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(proxy.getName())) {
								if (resource == null)
									resource = proxy.requestResource();
								sourceFiles.add(new SourceFile((IFile) resource, sourceLocation));
							}
							return false;
						case IResource.FOLDER :
							if (exclusionPatterns != null && inclusionPatterns == null) {
								// if there are inclusion patterns then we must walk the children
								resource = proxy.requestResource();
								if (Util.isExcluded(resource, inclusionPatterns, exclusionPatterns)) return false;
							}
							if (isAlsoProject && isExcludedFromProject(proxy.requestFullPath())) return false;
					}
					return true;
				}
			},
			IResource.NONE
		);
		notifier.checkCancel();
	}
}

protected void cleanOutputFolders(boolean copyBack) throws CoreException {
	boolean deleteAll = JavaCore.CLEAN.equals(
		javaBuilder.javaProject.getOption(JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, true));
	if (deleteAll) {
		if (this.javaBuilder.participants != null)
			for (int i = 0, l = this.javaBuilder.participants.length; i < l; i++)
				this.javaBuilder.participants[i].cleanStarting(this.javaBuilder.javaProject);

		ArrayList visited = new ArrayList(sourceLocations.length);
		for (int i = 0, l = sourceLocations.length; i < l; i++) {
			notifier.subTask(Messages.bind(Messages.build_cleaningOutput, this.javaBuilder.currentProject.getName())); 
			ClasspathMultiDirectory sourceLocation = sourceLocations[i];
			if (sourceLocation.hasIndependentOutputFolder) {
				IContainer outputFolder = sourceLocation.binaryFolder;
				if (!visited.contains(outputFolder)) {
					visited.add(outputFolder);
					IResource[] members = outputFolder.members(); 
					for (int j = 0, m = members.length; j < m; j++) {
						IResource member = members[j];
						if (!member.isDerived()) {
							member.accept(
								new IResourceVisitor() {
									public boolean visit(IResource resource) throws CoreException {
										resource.setDerived(true);
										return resource.getType() != IResource.FILE;
									}
								}
							);
						}
						member.delete(IResource.FORCE, null);
					}
				}
				notifier.checkCancel();
				if (copyBack)
					copyExtraResourcesBack(sourceLocation, true);
			} else {
				boolean isOutputFolder = sourceLocation.sourceFolder.equals(sourceLocation.binaryFolder);
				final char[][] exclusionPatterns =
					isOutputFolder
						? sourceLocation.exclusionPatterns
						: null; // ignore exclusionPatterns if output folder == another source folder... not this one
				final char[][] inclusionPatterns =
					isOutputFolder
						? sourceLocation.inclusionPatterns
						: null; // ignore inclusionPatterns if output folder == another source folder... not this one
				sourceLocation.binaryFolder.accept(
					new IResourceProxyVisitor() {
						public boolean visit(IResourceProxy proxy) throws CoreException {
							IResource resource = null;
							if (proxy.getType() == IResource.FILE) {
								if (exclusionPatterns != null || inclusionPatterns != null) {
									resource = proxy.requestResource();
									if (Util.isExcluded(resource, inclusionPatterns, exclusionPatterns)) return false;
								}
								if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(proxy.getName())) {
									if (resource == null)
										resource = proxy.requestResource();
									resource.delete(IResource.FORCE, null);
								}
								return false;
							}
							if (exclusionPatterns != null && inclusionPatterns == null) {
								// if there are inclusion patterns then we must walk the children
								resource = proxy.requestResource();
								if (Util.isExcluded(resource, inclusionPatterns, exclusionPatterns)) return false;
							}
							notifier.checkCancel();
							return true;
						}
					},
					IResource.NONE
				);
				if (!isOutputFolder && copyBack) {
					notifier.checkCancel();
					copyPackages(sourceLocation);
				}
			}
			notifier.checkCancel();
		}
	} else if (copyBack) {
		for (int i = 0, l = sourceLocations.length; i < l; i++) {
			ClasspathMultiDirectory sourceLocation = sourceLocations[i];
			if (sourceLocation.hasIndependentOutputFolder)
				copyExtraResourcesBack(sourceLocation, false);
			else if (!sourceLocation.sourceFolder.equals(sourceLocation.binaryFolder))
				copyPackages(sourceLocation); // output folder is different from source folder
			notifier.checkCancel();
		}
	}
}

protected void cleanUp() {
	this.incrementalBuilder = null;
	this.missingSecondaryTypes = null;
	super.cleanUp();
}

protected void compile(SourceFile[] units, SourceFile[] additionalUnits, boolean compilingFirstGroup) {
	if (!compilingFirstGroup && this.missingSecondaryTypes == null)
		this.missingSecondaryTypes = new ArrayList(7);
	super.compile(units, additionalUnits, compilingFirstGroup);
}

protected void copyExtraResourcesBack(ClasspathMultiDirectory sourceLocation, final boolean deletedAll) throws CoreException {
	// When, if ever, does a builder need to copy resources files (not .java or .class) into the output folder?
	// If we wipe the output folder at the beginning of the build then all 'extra' resources must be copied to the output folder.

	notifier.subTask(Messages.build_copyingResources); 
	final int segmentCount = sourceLocation.sourceFolder.getFullPath().segmentCount();
	final char[][] exclusionPatterns = sourceLocation.exclusionPatterns;
	final char[][] inclusionPatterns = sourceLocation.inclusionPatterns;
	final IContainer outputFolder = sourceLocation.binaryFolder;
	final boolean isAlsoProject = sourceLocation.sourceFolder.equals(javaBuilder.currentProject);
	sourceLocation.sourceFolder.accept(
		new IResourceProxyVisitor() {
			public boolean visit(IResourceProxy proxy) throws CoreException {
				IResource resource = null;
				switch(proxy.getType()) {
					case IResource.FILE :
						if (org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(proxy.getName()) ||
							org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(proxy.getName())) return false;

						resource = proxy.requestResource();
						if (javaBuilder.filterExtraResource(resource)) return false;
						if (exclusionPatterns != null || inclusionPatterns != null)
							if (Util.isExcluded(resource, inclusionPatterns, exclusionPatterns))
								return false;

						IPath partialPath = resource.getFullPath().removeFirstSegments(segmentCount);
						IResource copiedResource = outputFolder.getFile(partialPath);
						if (copiedResource.exists()) {
							if (deletedAll) {
								IResource originalResource = findOriginalResource(partialPath);
								String id = originalResource.getFullPath().removeFirstSegments(1).toString();
								createProblemFor(
									resource,
									null,
									Messages.bind(Messages.build_duplicateResource, id), 
									javaBuilder.javaProject.getOption(JavaCore.CORE_JAVA_BUILD_DUPLICATE_RESOURCE, true));
								return false;
							}
							copiedResource.delete(IResource.FORCE, null); // last one wins
						}
						resource.copy(copiedResource.getFullPath(), IResource.FORCE | IResource.DERIVED, null);
						Util.setReadOnly(copiedResource, false); // just in case the original was read only
						return false;
					case IResource.FOLDER :
						resource = proxy.requestResource();
						if (javaBuilder.filterExtraResource(resource)) return false;
						IPath folderPath = resource.getFullPath();
						if (isAlsoProject && isExcludedFromProject(folderPath)) return false; // the sourceFolder == project
						if (exclusionPatterns != null && Util.isExcluded(resource, inclusionPatterns, exclusionPatterns))
					        return inclusionPatterns != null; // need to go further only if inclusionPatterns are set
						createFolder(folderPath.removeFirstSegments(segmentCount), outputFolder);
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
	final char[][] inclusionPatterns = sourceLocation.inclusionPatterns;
	final IContainer outputFolder = sourceLocation.binaryFolder;
	final boolean isAlsoProject = sourceLocation.sourceFolder.equals(javaBuilder.currentProject);
	sourceLocation.sourceFolder.accept(
		new IResourceProxyVisitor() {
			public boolean visit(IResourceProxy proxy) throws CoreException {
				switch(proxy.getType()) {
					case IResource.FILE :
						return false;
					case IResource.FOLDER :
						IResource resource = proxy.requestResource();
						if (javaBuilder.filterExtraResource(resource)) return false;
						IPath folderPath = resource.getFullPath();
						if (isAlsoProject && isExcludedFromProject(folderPath)) return false; // the sourceFolder == project
						if (exclusionPatterns != null && Util.isExcluded(resource, inclusionPatterns, exclusionPatterns))
					        return inclusionPatterns != null; // need to go further only if inclusionPatterns are set
						createFolder(folderPath.removeFirstSegments(segmentCount), outputFolder);
				}
				return true;
			}
		},
		IResource.NONE
	);
}

protected IResource findOriginalResource(IPath partialPath) {
	for (int i = 0, l = sourceLocations.length; i < l; i++) {
		ClasspathMultiDirectory sourceLocation = sourceLocations[i];
		if (sourceLocation.hasIndependentOutputFolder) {
			IResource originalResource = sourceLocation.sourceFolder.getFile(partialPath);
			if (originalResource.exists()) return originalResource;
		}
	}
	return null;
}

protected void processAnnotationResults(CompilationParticipantResult[] results) {
	// to compile the compilation participant results, we need to incrementally recompile all affected types
	// whenever the generated types are initially added or structurally changed
	if (this.incrementalBuilder == null)
		this.incrementalBuilder = new IncrementalImageBuilder(this);
	this.incrementalBuilder.processAnnotationResults(results);
}

protected void rebuildTypesAffectedByMissingSecondaryTypes() {
	// to compile types that could not find 'missing' secondary types because of multiple
	// compile groups, we need to incrementally recompile all affected types as if the missing
	// secondary types have just been added
	if (this.incrementalBuilder == null)
		this.incrementalBuilder = new IncrementalImageBuilder(this);
	for (int i = this.missingSecondaryTypes.size(); --i >=0; )
		this.incrementalBuilder.addAffectedSourceFiles((char[]) this.missingSecondaryTypes.get(i));
}

public String toString() {
	return "batch image builder for:\n\tnew state: " + newState; //$NON-NLS-1$
}
}
