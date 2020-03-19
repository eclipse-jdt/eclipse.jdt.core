/*******************************************************************************
 * Copyright (c) 2006, 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.util.ManyToMany;

/**
 * A bidirectional many-to-many map from parent files to generated files.
 * This extends the functionality of ManyToMany by adding serialization.
 * The object also tracks attributes of the generated files.
 */
public class GeneratedFileMap extends ManyToMany<IFile, IFile> {

	public enum Flags {
		/** Non-source files, e.g., text or xml. */
		NONSOURCE;
	}

	// Version 2 since Eclipse 3.3.1: add ability to track attributes of generated files
	private static final int SERIALIZATION_VERSION = 2;

	private final IProject _proj;

	private final Map<IFile, Set<Flags>> _flags = new HashMap<>();

	private final boolean _isTestCode;

	public GeneratedFileMap(IProject proj, boolean isTestCode) {
		_proj = proj;
		_isTestCode = isTestCode;
		readState();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.internal.util.ManyToMany#clear()
	 */
	@Override
	public synchronized boolean clear() {
		_flags.clear();
		return super.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.internal.util.ManyToMany#remove(java.lang.Object, java.lang.Object)
	 */
	@Override
	public synchronized boolean remove(IFile key, IFile value) {
		boolean removed = super.remove(key, value);
		if (removed) {
			if (!containsValue(value)) {
				_flags.remove(value);
			}
		}
		return removed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.internal.util.ManyToMany#removeKey(java.lang.Object)
	 */
	@Override
	public synchronized boolean removeKey(IFile key) {
		Set<IFile> values = getValues(key);
		boolean removed = super.removeKey(key);
		if (removed) {
			for (IFile value : values) {
				if (!containsValue(value)) {
					_flags.remove(value);
				}
			}
		}
		return removed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.internal.util.ManyToMany#removeValue(java.lang.Object)
	 */
	@Override
	public synchronized boolean removeValue(IFile value) {
		boolean removed = super.removeValue(value);
		if (removed) {
			_flags.remove(value);
		}
		return removed;
	}

	/**
	 * Clear the file dependencies and delete the serialized state.
	 * This will take effect even if the dirty bit is not set.
	 */
	public synchronized void clearState() {
		clear();
		File state = getStateFile(_proj, _isTestCode);
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
	 * Convenience method, equivalent to put(key, value, [no flags])
	 */
	@Override
	public synchronized boolean put(IFile parent, IFile generated) {
		return put(parent, generated, Collections.<Flags>emptySet());
	}

	/**
	 * Convenience method, equivalent to put(key, value, isSource ? [no flags] : [NONSOURCE])
	 */
	public boolean put(IFile parent, IFile generated, boolean isSource) {
		return put(parent, generated, isSource ? Collections.<Flags>emptySet() : EnumSet.of(Flags.NONSOURCE));
	}

	/**
	 * Add a parent-to-generated association and specify attributes for the generated file.
	 * The attributes are associated with the file, not the link: that is, a given generated
	 * file can only have one set of attributes, not a different set per parent. The attributes
	 * set in the most recent call will override those set in previous calls.
	 */
	public synchronized boolean put(IFile parent, IFile generated, Set<Flags> flags) {
		if (flags.isEmpty()) {
			_flags.remove(generated);
		}
		else {
			_flags.put(generated, flags);
		}
		return super.put(parent, generated);
	}

	public Set<Flags> getFlags(IFile generated) {
		Set<Flags> flags = _flags.get(generated);
		return flags == null ? Collections.<Flags>emptySet() : flags;
	}

	/**
	 * Convenience method, equivalent to !getFlags(generated).contains(Flags.NONSOURCE)
	 * @return true if the generated file is a source (Java) file rather than text, xml, etc.
	 */
	public boolean isSource(IFile generated) {
		return !getFlags(generated).contains(Flags.NONSOURCE);
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
	 * @param isTestCode
	 */
	private File getStateFile(IProject project, boolean isTestCode) {
		if (!project.exists()) return null;
		IPath workingLocation = project.getWorkingLocation(AptPlugin.PLUGIN_ID);
		return workingLocation.append(isTestCode ? "teststate.dat" : "state.dat").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
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
		File file = getStateFile(_proj, _isTestCode);
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

			// Now the _flags map:
			int sizeOfFlags = in.readInt();
			for (int i = 0; i < sizeOfFlags; ++i) {
				String childPath = in.readUTF();
				IFile child = convertPathToIFile(childPath);
				if (!containsValue(child)) {
					throw new IOException("Error in generated file attributes: did not expect file " + childPath); //$NON-NLS-1$
				}

				int attributeCount = in.readInt();
				EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);
				for (int j = 0; j < attributeCount; ++j) {
					String attr = in.readUTF();
					Flags f = Flags.valueOf(attr);
					flags.add(f);
				}
				_flags.put(child, flags);
			}

			// our serialized and in-memory states are now identical
			clearDirtyBit();
		}
		catch (IOException ioe) {
			// Avoid partial initialization
			clear();
			// We can safely continue without having read our dependencies.
			AptPlugin.logWarning(ioe, "Could not read APT dependencies: generated files may not be deleted until the next clean"); //$NON-NLS-1$
		}
		catch (IllegalArgumentException iae) {
			// Avoid partial initialization
			clear();
			// We can safely continue without having read our dependencies.
			AptPlugin.logWarning(iae, "Could not read APT dependencies: generated files may not be deleted until the next clean"); //$NON-NLS-1$
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
		File file = getStateFile(_proj, _isTestCode);
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

			// Number of generated files with attributes
			out.writeInt(_flags.size());

			// for each generated file that has attributes...
			for (Entry<IFile, Set<Flags>> entry : _flags.entrySet()) {
				// ...generated file name
				out.writeUTF(convertIFileToPath(entry.getKey()));

				Set<Flags> flags = entry.getValue();
				// ...number of attributes
				out.writeInt(flags.size());
				for (Flags f : flags) {
					// ...attribute name
					out.writeUTF(f.name());
				}
			}

			// our serialized and in-memory states are now identical
			clearDirtyBit();
			out.flush();
		}
		catch (IOException ioe) {
			// We can safely continue without having written our dependencies.
			AptPlugin.logWarning(ioe, "Could not serialize APT dependencies"); //$NON-NLS-1$
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (IOException ioe) {
					// Do nothing
				}
			}
		}
	}


}
