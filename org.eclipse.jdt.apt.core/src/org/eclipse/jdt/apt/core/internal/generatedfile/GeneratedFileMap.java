/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.generatedfile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.util.ManyToMany;

/**
 * A bidirectional many-to-many map from parent files to generated files.
 * This extends the functionality of ManyToMany by adding serialization.
 */
public class GeneratedFileMap extends ManyToMany<IFile, IFile> {

	private static final int SERIALIZATION_VERSION = 1;
	
	private final IProject _proj;
	
	public GeneratedFileMap(IProject proj) {
		_proj = proj;
		readState();
	}
	
	/**
	 * Clear the file dependencies and delete the serialized state.
	 * This will take effect even if the dirty bit is not set.
	 */
	public synchronized void clearState() {
		clear();
		File state = getStateFile(_proj);
		if (state != null) {
			boolean successfullyDeleted = state.delete();
			if (!successfullyDeleted && state.exists()) {
				AptPlugin.log(new IOException("Could not delete apt dependency state file"), //$NON-NLS-1$
						state.getPath());
			}
		}
		clearDirtyBit();
	}
	
	/**
	 * Utility method for serialization
	 */
	private String convertIFileToPath(IFile file) {
		IPath path = file.getProjectRelativePath();
		return path.toOSString();
	}
	
	/**
	 * Utility method for deserialization
	 */
	private IFile convertPathToIFile(String projectRelativeString) {
		IPath path = new Path(projectRelativeString);
		return _proj.getFile(path);
	}
	
	/**
	 * Returns the File to use for saving and restoring the last built state for the given project.
	 * Returns null if the project does not exists (e.g. has been deleted)
	 */
	private File getStateFile(IProject project) {
		if (!project.exists()) return null;
		IPath workingLocation = project.getWorkingLocation(AptPlugin.PLUGIN_ID);
		return workingLocation.append("state.dat").toFile(); //$NON-NLS-1$
	}
	
	/**
	 * Reads the last serialized build state into memory. This includes dependency
	 * information so that we do not need to do a clean build in order to recreate
	 * our dependencies.
	 * 
	 * File format:
	 * 
	 * int version
	 * int sizeOfMap
	 *    String parentIFilePath
	 *    int numberOfChildren
	 *      String childIFilePath
	 * 
	 * This method is not synchronized because it is called only from this object's constructor.
	 */
	private void readState() {
		File file = getStateFile(_proj);
		if (file == null || !file.exists()) {
			// We'll just start with no dependencies
			return;
		}
		DataInputStream in = null;
		try {
			in= new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			int version = in.readInt();
			if (version != SERIALIZATION_VERSION) {
				throw new IOException("Dependency map file version does not match. Expected "  //$NON-NLS-1$
						+ SERIALIZATION_VERSION + ", but found " + version); //$NON-NLS-1$
			}
			int sizeOfMap = in.readInt();
			
			// For each entry, we'll have a parent and a set of children, 
			// which we can drop into the parent -> child map.
			for (int parentIndex=0; parentIndex<sizeOfMap; parentIndex++) {
				String parentPath = in.readUTF();
				IFile parent = convertPathToIFile(parentPath);
				int numChildren = in.readInt();
				for (int childIndex = 0; childIndex<numChildren; childIndex++) {
					String childPath = in.readUTF();
					IFile child = convertPathToIFile(childPath);
					// add the child to the parent->child map
					put(parent, child);
				}
			}
			// our serialized and in-memory states are now identical
			clearDirtyBit();
		}
		catch (IOException ioe) {
			// We can safely continue without having read our dependencies.
			AptPlugin.log(ioe, "Could not deserialize APT dependencies"); //$NON-NLS-1$
		}
		finally {
			if (in != null) {
				try {in.close();} catch (IOException ioe) {}
			}
		}
	}
	
	/**
	 * Write our dependencies to disk.  If not dirty, nothing is written.
	 */
	public synchronized void writeState() {
		if (!isDirty()) {
			return;
		}
		File file = getStateFile(_proj);
		if (file == null) {
			// Cannot write state, as project has been deleted
			return;
		}
		file.delete();
		
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			
			out.writeInt(SERIALIZATION_VERSION);
			
			// Number of parent files
			Set<IFile> parents = getKeySet();
			out.writeInt(parents.size());
			
			// for each parent...
			for (IFile parent : parents) {
				
				// ...parent name
				out.writeUTF(convertIFileToPath(parent));
				
				Set<IFile> children = getValues(parent);
				
				// ...number of children
				out.writeInt(children.size());
				
				// for each child...
				for (IFile child : children) {
					// ...child name.
					out.writeUTF(convertIFileToPath(child));
				}
			}
			// our serialized and in-memory states are now identical
			clearDirtyBit();
		}
		catch (IOException ioe) {
			// We can safely continue without having written our dependencies.
			AptPlugin.log(ioe, "Could not serialize APT dependencies"); //$NON-NLS-1$
		}
		finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				}
				catch (IOException ioe) {
					AptPlugin.log(ioe, "Failed to write the APT dependency state to disk"); //$NON-NLS-1$
				}
			}
		}
	}
	

}
