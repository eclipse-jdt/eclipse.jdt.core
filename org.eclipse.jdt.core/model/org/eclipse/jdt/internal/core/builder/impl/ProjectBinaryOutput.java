package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.IState;
import org.eclipse.jdt.internal.core.builder.IType;
import org.eclipse.jdt.internal.core.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * A <ProjectBinaryOutput> is a <BinaryOutput> that stores the
 * binaries in a folder of an <IProject>.
 */
public class ProjectBinaryOutput extends BinaryOutput {
	private IProject project;
	private IPath outputPath;
	private JavaDevelopmentContextImpl dc;

	public final static String ADDED = "ADDED";
	public final static String MODIFIED = "MODIFIED";
	public final static String DELETED = "DELETED";

	/**
	 * Creates a new ProjectBinaryOutput for the given project and output path
	 * in this project.
	 */
	public ProjectBinaryOutput(
		IProject project,
		IPath outputPath,
		JavaDevelopmentContextImpl dc) {
		this.project = project;
		this.outputPath = outputPath;
		this.dc = dc;

		/* create the output folder is it doesn't exist */
		if (!project.getFullPath().equals(outputPath)) {
			this.makeContainersIfNecessary(outputPath);
		}
	}

	/**
	 * @see BinaryOutput
	 */
	protected void basicPutBinary(
		TypeStructureEntry tsEntry,
		byte[] binary,
		int crc) {

		IType type;
		IPath path = getPathForBinary(type = tsEntry.getType());
		deleteBinary(type);

		IContainer container = makeContainersIfNecessary(path.removeLastSegments(1));

		PackageElement element =
			new PackageElement(type.getPackage(), new SourceEntry(path, null, null));
		IFile file = container.getFile(new Path(path.lastSegment()));

		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(binary);
			file.create(stream, true, null);
		} catch (CoreException e) {
			throw this.dc.internalException(e);
		}
	}

	/**
	 * Deletes everything in the given container.
	 */
	private void deleteAllInContainer(IContainer container) {
		try {
			if (!container.exists())
				return;
			IResource[] members = container.members();
			for (int i = 0, max = members.length; i < max; i++) {
				IResource resource = (IResource) members[i];
				resource.delete(true, null);
			}
		} catch (CoreException e) {
			throw this.dc.internalException(e);
		}
	}

	/**
	 * @see BinaryOutput
	 */
	public void deleteBinary(IType type) {
		IPath path = getPathForBinary(type);
		IFile file = getFile(path);
		try {
			file.delete(true, null);
		} catch (CoreException e) {
			throw this.dc.internalException(e);
		}
	}

	/**
	 * Deletes the classes in the given container, recursively.
	 * Delete any folders which become empty.
	 */
	private void deleteClassesInContainer(IContainer container) {
		try {
			if (!container.exists())
				return;
			IResource[] members = container.members();
			for (int i = 0, max = members.length; i < max; i++) {
				IResource resource = (IResource) members[i];
				switch (resource.getType()) {
					case IResource.FILE :
						if (resource.getName().toLowerCase().endsWith(".class")) {
							resource.delete(true, null);
						}
						break;
					case IResource.PROJECT :
					case IResource.FOLDER :
						deleteClassesInContainer((IContainer) resource);
						break;
				}
			}
			//
			//		Don't delete empty folders, since the output may overlap with the source, and
			//		we don't want to delete empty folders which the user may have created.
			//
			//		if (container.getType() == IResource.FOLDER && !container.members().hasMoreElements()) {
			//			container.delete(true, null);
			//		}
		} catch (CoreException e) {
			throw this.dc.internalException(e);
		}
	}

	/**
	 * @see BinaryOutput
	 */
	public void garbageCollect(IState[] statesInUse) {
		// Nothing to do for a Project binary output
	}

	/**
	 * @see BinaryOutput
	 */
	public byte[] getBinary(TypeStructureEntry tsEntry, IType type) {
		IPath path = getPathForBinary(type);
		IFile file = getFile(path);
		try {
			InputStream input = file.getContents(true);
			return Util.readContentsAsBytes(input);
		} catch (IOException e) {
			throw this.dc.internalException(e);
		} catch (CoreException e) {
			return this.dc.getBinaryFromFileSystem(file);
		}
	}

	/**
	 * Returns the container for a path.
	 */
	private IContainer getContainer(IPath path) {
		if (path.isAbsolute()) {
			if (this.project.getFullPath().equals(path)) {
				return this.project;
			} else {
				return this.project.getWorkspace().getRoot().getFolder(path);
			}
		}
		return this.project.getFolder(path);
	}

	/**
	 * Returns the file for a path.
	 */
	private IFile getFile(IPath path) {
		if (path.isAbsolute()) {
			return this.project.getWorkspace().getRoot().getFile(path);
		}
		return this.project.getFile(path);
	}

	/**
	 * Returns the path for the output package fragment root.
	 */
	IPath getOutputPath() {
		return this.outputPath;
	}

	/**
	 * Returns the path in the output folder for the given type.
	 */
	private IPath getPathForBinary(IType type) {
		return getOutputPath().append(type.getName().replace('.', '/') + ".class");
	}

	/**
	 * Returns the container at the given path, creating it and any parent folders if necessary.
	 */
	IContainer makeContainersIfNecessary(IPath path) {
		try {
			IContainer container = getContainer(path);
			if (container.exists())
				return container;
			Assert.isTrue(container instanceof IFolder);
			makeContainersIfNecessary(path.removeLastSegments(1));
			((IFolder) container).create(true, true, null);
			return container;
		} catch (CoreException e) {
			throw this.dc.internalException(e);
		}
	}

	/**
	 * @see BinaryOutput
	 */
	public void scrubOutput() {

		IJavaProject projectElement = JavaCore.create(this.project);
		IClasspathEntry[] entries;
		try {
			entries = projectElement.getResolvedClasspath(true);
		} catch (JavaModelException e) {
			throw this.dc.internalException(e);
		}

		/* detect whether the binary ouput coincidates with source folder */
		boolean flushAllOutput = false;
		for (int i = 0, length = entries.length; i < length; i++) {
			IClasspathEntry entry = entries[i];
			if ((entry.getEntryKind() == IClasspathEntry.CPE_SOURCE)) {
				if (!this.outputPath.equals(entry.getPath())) {
					flushAllOutput = true; // output is distinct - flush all
					break;
				}
			}
		}
		if (flushAllOutput) {
			deleteAllInContainer(getContainer(this.outputPath));
		} else {
			deleteClassesInContainer(getContainer(this.outputPath));
		}
	}

}
