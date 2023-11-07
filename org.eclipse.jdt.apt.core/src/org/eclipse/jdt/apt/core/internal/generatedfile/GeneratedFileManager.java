/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    wharley@bea.com - refactored, and reinstated reconcile-time type gen
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.generatedfile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.internal.Messages;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.apt.core.internal.util.ManyToMany;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

/**
 * This class is used for managing generated files; in particular, keeping track of
 * dependencies so that no-longer-generated files can be deleted, and managing the
 * lifecycle of working copies in memory.
 * <p>
 * During build, a generated file may be a "type", in the sense of a generated Java source
 * file, or it may be a generated class file or an arbitrary resource (such as an XML
 * file). During reconcile, it is only possible to generate Java source files. Also,
 * during reconcile, it is not possible to write to disk or delete files from disk; all
 * operations take place in memory only, using "working copies" provided by the Java
 * Model.
 *
 * <h2>DATA STRUCTURES</h2>
 * <code>_buildDeps</code> is a many-to-many map that tracks which parent files
 * are responsible for which generated files. Entries in this map are created when files
 * are created during builds. This map is serialized so that dependencies can be reloaded
 * when a project is opened without having to do a full build.
 * <p>
 * When types are generated during reconcile, they are not actually laid down on disk (ie
 * we do not commit the working copy). However, the file handles are still used as keys
 * into the various maps in this case.
 * <p>
 * <code>_reconcileDeps</code> is the reconcile-time analogue of
 * <code>_buildDeps</code>.  This map is not serialized.
 * <p>
 * Given a working copy, it is easy to determine the IFile that it models by calling
 * <code>ICompilationUnit.getResource()</code>.  To go the other way, we store maps
 * of IFile to ICompilationUnit.  Working copies that represent generated types are
 * stored in <code>_reconcileGenTypes</code>; working copies that represent deleted types
 * are stored in <code>_hiddenBuiltTypes</code>.
 * <p>
 * List invariants: for the many-to-many maps, every forward entry must correspond to a
 * reverse entry; this is managed (and verified) by the ManyToMany map code. Also, every
 * entry in the <code>_reconcileGenTypes</code> list must correspond to an entry in the
 * <code>_reconcileDeps</code> map. There can be no overlap between these
 * entries and the <code>_hiddenBuiltTypes</code> map. Whenever a working copy is placed
 * into this overall collection, it must have <code>becomeWorkingCopy()</code> called on
 * it; whenever it is removed, it must have <code>discardWorkingCopy()</code> called on
 * it.
 *
 * <h2>SYNCHRONIZATION NOTES</h2>
 * Synchronization around the GeneratedFileManager's maps uses the GeneratedFileMap
 * instance's monitor. When acquiring this monitor, DO NOT PERFORM ANY OPERATIONS THAT
 * TAKE ANY OTHER LOCKS (e.g., java model operations, or file system operations like
 * creating or deleting a file or folder). If you do this, then the code is subject to
 * deadlock situations. For example, a resource-changed listener may take a resource lock
 * and then call into the GeneratedFileManager for clean-up, where your code could reverse
 * the order in which the locks are taken. This is bad, so be careful.
 *
 * <h2>RECONCILE vs. BUILD</h2>
 * Reconciles are based on in-memory type information, i.e., working copies. Builds are
 * based on files on disk. At any given moment, a build thread and any number of reconcile
 * threads may be executing. All share the same GeneratedFileManager object, but each
 * thread will have a separate BuildEnvironment. Multiple files are built in a loop, with
 * files generated on one round being compiled (and possibly generating new files) on the
 * next; only one file at a time is reconciled, but when a file is generated during
 * reconcile it will invoke a recursive call to reconcile, with a unique
 * ReconcileBuildEnvironment.
 * <p>
 * What is the relationship between reconcile-time dependency information and build-time
 * dependency information? In general, there is one set of dependency maps for build time
 * information and a separate set for reconcile time information (with the latter being
 * shared by all reconcile threads). Reconciles do not write to build-time information,
 * nor do they write to the disk. Builds, however, may have to interact with
 * reconcile-time info. The tricky bit is that a change to a file "B.java" in the
 * foreground editor window might affect the way that background file "A.java" generates
 * "AGen.java". That is, editing B.java is in effect making A.java dirty; but the Eclipse
 * build system has no way of knowing that, so A will not be reconciled.
 * <p>
 * The nearest Eclipse analogy is to refactoring, where a refactor operation in the
 * foreground editor can modify background files; Eclipse solves this problem by requiring
 * that all files be saved before and after a refactoring operation, but that solution is
 * not practical for the simple case of making edits to a file that might happen to be an
 * annotation processing dependency. The JSR269 API works around this problem by letting
 * processors state these out-of-band dependencies explicitly, but com.sun.mirror.apt has
 * no such mechanism.
 * <p>
 * The approach taken here is that when a build is performed, we discard the working
 * copies of any files that are open in editors but that are not dirty (meaning the file
 * on disk is the same as the version in the editor). This still means that if file A is
 * dirty, AGen will not be updated even when B is edited; thus, making a breaking change
 * to A and then making a change to B that is supposed to fix the break will not work.
 */
public class GeneratedFileManager
{

	/**
	 * Access to the package fragment root for generated types.
	 * Encapsulated into this class so that synchronization can be guaranteed.
	 */
	private class GeneratedPackageFragmentRoot {

		// The name and root are returned as a single object to ensure synchronization.
		final class NameAndRoot {
			final String name;
			final IPackageFragmentRoot root;
			NameAndRoot(String name, IPackageFragmentRoot root) {
				this.name = name;
				this.root = root;
			}
		}

		private IPackageFragmentRoot _root = null;

		private String _folderName = null;

		/**
		 * Get the package fragment root and the name of the folder
		 * it corresponds to.  If the folder is not on the classpath,
		 * the root will be null.
		 */
		public synchronized NameAndRoot get() {
			return new NameAndRoot(_folderName, _root);
		}

		/**
		 * Force the package fragment root and folder name to be recalculated.
		 * Check whether the new folder is actually on the classpath; if not,
		 * set root to be null.
		 */
		public synchronized void set() {
			IFolder genFolder = _gsfm.getFolder();
			_root = null;
			if (_jProject.isOnClasspath(genFolder)) {
				_root = _jProject.getPackageFragmentRoot(genFolder);
			}
			_folderName = genFolder.getProjectRelativePath().toString();
		}
	}

	/**
	 * If true, when buffer contents are updated during a reconcile, reconcile() will
	 * be called on the new contents.  This is not necessary to update the open editor,
	 * but if the generated file is itself a parent file, it will cause recursive
	 * type generation.
	 */
	private static final boolean RECURSIVE_RECONCILE = true;

	/**
	 * Disable type generation during reconcile.  In the past, reconcile-time type
	 * generation caused deadlocks; see (BEA internal) Radar bug #238684.  As of
	 * Eclipse 3.3 this should work.
	 */
	private static final boolean GENERATE_TYPE_DURING_RECONCILE = true;

	/**
	 * If true, the integrity of internal data structures will be verified after various
	 * operations are performed.
	 */
	private static final boolean ENABLE_INTEGRITY_CHECKS = true;

	/**
	 * A singleton instance of CompilationUnitHelper, which encapsulates operations on working copies.
	 */
	private static final CompilationUnitHelper _CUHELPER = new CompilationUnitHelper();

	/**
	 * The regex delimiter used to parse package names.
	 */
	private static final Pattern _PACKAGE_DELIMITER = Pattern.compile("\\."); //$NON-NLS-1$

	static {
		// register element-changed listener to clean up working copies
		int mask = ElementChangedEvent.POST_CHANGE;
		JavaCore.addElementChangedListener(new WorkingCopyCleanupListener(), mask);
	}

	/**
	 * Many-to-many map from parent files to files generated during build. These files all
	 * exist on disk. This map is used to keep track of dependencies during build, and is
	 * read-only during reconcile. This map is serialized.
	 */
	private final GeneratedFileMap _buildDeps;

	/**
	 * Set of files that have been generated during build by processors that
	 * support reconcile-time type generation.  Files in this set are expected to
	 * be generated during reconcile, and therefore will be deleted after a reconcile
	 * if they're not generated.  This is different from the value set of
	 * _reconcileDeps in that the contents of this set are known to have been
	 * generated during a build.
	 */
	private final Set<IFile> _clearDuringReconcile;

	/**
	 * Many-to-many map from parent files to files generated during reconcile.
	 * Both the keys and the values may correspond to files that exist on disk or only in
	 * memory. This map is used to keep track of dependencies created during reconcile,
	 * and is not accessed during build. This map is not serialized.
	 */
	private final ManyToMany<IFile, IFile> _reconcileDeps;

	/**
	 * Many-to-many map from parent files to files that are generated in build but not
	 * during reconcile.  We need this so we can tell parents that were never reconciled
	 * (meaning their generated children on disk are valid) from parents that have been
	 * edited so that they no longer generate their children (meaning the generated
	 * children may need to be removed from the typesystem).  This map is not serialized.
	 */
	private final ManyToMany<IFile, IFile> _reconcileNonDeps;

	/**
	 * Map of types that were generated during build but are being hidden (removed from
	 * the reconcile-time typesystem) by blank WorkingCopies. These are tracked separately
	 * from regular working copies for the sake of clarity. The keys all correspond to
	 * files that exist on disk; if they didn't, there would be no reason for an entry.
	 * <p>
	 * This is a map of file to working copy of that file, <strong>NOT</strong> a map of
	 * parents to generated children.  The keys in this map are a subset of the values in
	 * {@link #_reconcileNonDeps}.  This map exists so that given a file, we can find the
	 * working copy that represents it.
	 * <p>
	 * Every working copy exists either in this map or in {@link #_hiddenBuiltTypes}, but
	 * not in both. These maps exist to track the lifecycle of a working copy. When a new
	 * working copy is created, {@link ICompilationUnit#becomeWorkingCopy()} is called. If
	 * an entry is removed from this map without being added to the other,
	 * {@link ICompilationUnit#discardWorkingCopy()} must be called.
	 *
	 * @see #_reconcileGenTypes
	 */
	private final Map<IFile, ICompilationUnit> _hiddenBuiltTypes;

	/**
	 * Cache of working copies (in-memory types created or modified during reconcile).
	 * Includes working copies that represent changes to types that were generated during
	 * a build and thus exist on disk, as well as working copies for types newly generated
	 * during reconcile that thus do not exist on disk.
	 * <p>
	 * This is a map of file to working copy of that file, <strong>NOT</strong> a map of
	 * parents to generated children. There is a 1:1 correspondence between keys in this
	 * map and values in {@link #_reconcileDeps}. This map exists so that given a file,
	 * we can find the working copy that represents it.
	 * <p>
	 * Every working copy exists either in this map or in {@link #_hiddenBuiltTypes}, but
	 * not in both. These maps exist to track the lifecycle of a working copy. When a new
	 * working copy is created, {@link ICompilationUnit#becomeWorkingCopy()} is called. If
	 * an entry is removed from this map without being added to the other,
	 * {@link ICompilationUnit#discardWorkingCopy()} must be called.
	 *
	 * @see #_hiddenBuiltTypes
	 */
	private final Map<IFile, ICompilationUnit> _reconcileGenTypes;

	/**
	 * Access to the package fragment root for generated types.  Encapsulated into a
	 * helper class in order to ensure synchronization.
	 */
	private final GeneratedPackageFragmentRoot _generatedPackageFragmentRoot;

	private final IJavaProject _jProject;

	private final GeneratedSourceFolderManager _gsfm;

	/**
	 * Initialized when the build starts, and accessed during type generation.
	 * This has the same lifecycle as _generatedPackageFragmentRoot.
	 * If there is a configuration problem, this may be set to <code>true</code>
	 * during generation of the first type to prevent any other types from
	 * being generated.
	 */
	private boolean _skipTypeGeneration = false;

	/**
	 * Clients should not instantiate this class; it is created only by {@link AptProject}.
	 */
	public GeneratedFileManager(final AptProject aptProject, final GeneratedSourceFolderManager gsfm) {
		_jProject = aptProject.getJavaProject();
		_gsfm = gsfm;
		_buildDeps = new GeneratedFileMap(_jProject.getProject(), gsfm.isTestCode());
		_clearDuringReconcile = new HashSet<>();
		_reconcileDeps = new ManyToMany<>();
		_reconcileNonDeps = new ManyToMany<>();
		_hiddenBuiltTypes = new HashMap<>();
		_reconcileGenTypes = new HashMap<>();
		_generatedPackageFragmentRoot = new GeneratedPackageFragmentRoot();
	}

	/**
	 * Add a non-Java-source entry to the build-time dependency maps. Java source files are added to
	 * the maps when they are generated, as by {@link #generateFileDuringBuild}, but files of other
	 * types must be added explicitly by the code that creates the file.
	 * <p>
	 * This method must only be called during build, not reconcile. It is not possible to add
	 * non-Java-source files during reconcile.
	 */
	public void addGeneratedFileDependency(Collection<IFile> parentFiles, IFile generatedFile)
	{
		addBuiltFileToMaps(parentFiles, generatedFile, false);
	}

	/**
	 * Called at the start of build in order to cache our package fragment root
	 */
	public void compilationStarted()
	{
		try {
			// clear out any generated source folder config markers
			if(!_gsfm.isTestCode()) {
				IMarker[] markers = _jProject.getProject().findMarkers(AptPlugin.APT_CONFIG_PROBLEM_MARKER, true,
						IResource.DEPTH_INFINITE);
				if (markers != null) {
					for (IMarker marker : markers)
						marker.delete();
				}
			}
		} catch (CoreException e) {
			AptPlugin.log(e, "Unable to delete configuration marker."); //$NON-NLS-1$
		}
		_skipTypeGeneration = false;
		_gsfm.ensureFolderExists();
		_generatedPackageFragmentRoot.set();

	}

	/**
	 * This method should only be used for testing purposes to ensure that maps contain
	 * entries when we expect them to.
	 */
	public synchronized boolean containsWorkingCopyMapEntriesForParent(IFile f)
	{
		return _reconcileDeps.containsKey(f);
	}

	/**
	 * Invoked at the end of a build to delete files that are no longer parented by
	 * <code>parentFile</code>. Files that are multiply parented will not actually be
	 * deleted, but the association from this parent to the generated file will be
	 * removed, so that when the last parent ceases to generate a given file it will be
	 * deleted at that time.
	 *
	 * @param newlyGeneratedFiles
	 *            the set of files generated by <code>parentFile</code> on the most
	 *            recent compilation; these files will be spared deletion.
	 * @return the set of source files that were actually deleted, or an empty set.
	 *            The returned set does not include non-source (e.g. text or xml) files.
	 */
	public Set<IFile> deleteObsoleteFilesAfterBuild(IFile parentFile, Set<IFile> newlyGeneratedFiles)
	{
		Set<IFile> deleted;
		List<ICompilationUnit> toDiscard = new ArrayList<>();
		Set<IFile> toReport = new HashSet<>();
		deleted = computeObsoleteFiles(parentFile, newlyGeneratedFiles, toDiscard, toReport);

		for (IFile toDelete : deleted) {
			if (AptPlugin.DEBUG_GFM) AptPlugin.trace(
					"deleted obsolete file during build: " + toDelete); //$NON-NLS-1$
			deletePhysicalFile(toDelete);
		}

		// Discard blank WCs *after* we delete the corresponding files:
		// we don't want the type to become briefly visible to a reconcile thread.
		for (ICompilationUnit wcToDiscard : toDiscard) {
			_CUHELPER.discardWorkingCopy(wcToDiscard);
		}

		return toReport;
	}

	/**
	 * Invoked at the end of a reconcile to get rid of any files that are no longer being
	 * generated. If the file existed on disk, we can't actually delete it, we can only
	 * create a blank WorkingCopy to hide it. Therefore, we can only remove Java source
	 * files, not arbitrary files. If the file was generated during reconcile and exists
	 * only in memory, we can actually remove it altogether.
	 * <p>
	 * Only some processors specify (via {@link org.eclipse.jdt.apt.core.util.AptPreferenceConstants#RTTG_ENABLED_OPTION})
	 * that they support type generation during reconcile.  We need to remove obsolete
	 * files generated by those processors, but preserve files generated by
	 * other processors.
	 *
	 * @param parentWC
	 *            the WorkingCopy being reconciled
	 * @param newlyGeneratedFiles
	 *            the complete list of files generated during the reconcile (including
	 *            files that exist on disk as well as files that only exist in memory)
	 */
	public void deleteObsoleteTypesAfterReconcile(ICompilationUnit parentWC, Set<IFile> newlyGeneratedFiles)
	{
		IFile parentFile = (IFile) parentWC.getResource();

		List<ICompilationUnit> toSetBlank = new ArrayList<>();
		List<ICompilationUnit> toDiscard = new ArrayList<>();
		computeObsoleteReconcileTypes(parentFile, newlyGeneratedFiles, _CUHELPER, toSetBlank, toDiscard);

		for (ICompilationUnit wcToDiscard : toDiscard) {
			if (AptPlugin.DEBUG_GFM) AptPlugin.trace(
					"discarded obsolete working copy during reconcile: " + wcToDiscard.getElementName()); //$NON-NLS-1$
			_CUHELPER.discardWorkingCopy(wcToDiscard);
		}

		WorkingCopyOwner workingCopyOwner = parentWC.getOwner();
		for (ICompilationUnit wcToSetBlank : toSetBlank) {
			if (AptPlugin.DEBUG_GFM) AptPlugin.trace(
					"hiding file with blank working copy during reconcile: " + wcToSetBlank.getElementName()); //$NON-NLS-1$
			_CUHELPER.updateWorkingCopyContents("", wcToSetBlank, workingCopyOwner, RECURSIVE_RECONCILE); //$NON-NLS-1$
		}

		assert checkIntegrity();
	}

	/**
	 * Called by the resource change listener when a file is deleted (eg by the user).
	 * Removes any files parented by this file, and removes the file from dependency maps
	 * if it is generated. This does not remove working copies parented by the file; that
	 * will happen when the working copy corresponding to the parent file is discarded.
	 */
	public void fileDeleted(IFile f)
	{
		List<IFile> toDelete = removeFileFromBuildMaps(f);

		for (IFile fileToDelete : toDelete) {
			deletePhysicalFile(fileToDelete);
		}

	}

	/**
	 * Invoked when a file is generated during a build. The generated file and
	 * intermediate directories will be created if they don't exist. This method takes
	 * file-system locks, and assumes that the calling method has at some point acquired a
	 * workspace-level resource lock.
	 *
	 * @param parentFiles
	 *            the parent or parents of the type being generated.  May be empty, and/or
	 *            may contain null entries, but must not itself be null.
	 * @param typeName
	 *            the dot-separated java type name of the type being generated
	 * @param contents
	 *            the java code contents of the new type .
	 * @param clearDuringReconcile
	 *            if true, this file should be removed after any reconcile in which it was not
	 *            regenerated.  This typically is used when the file is being generated by a
	 *            processor that supports {@linkplain org.eclipse.jdt.apt.core.util.AptPreferenceConstants#RTTG_ENABLED_OPTION
	 *            reconcile-time type generation}.
	 * @param progressMonitor
	 *            a progress monitor. This may be null.
	 * @return - the newly created IFile along with whether it was modified
	 */
	public FileGenerationResult generateFileDuringBuild(Collection<IFile> parentFiles, String typeName, String contents,
			boolean clearDuringReconcile, IProgressMonitor progressMonitor) throws CoreException
	{
		if (_skipTypeGeneration)
			return null;

		GeneratedPackageFragmentRoot.NameAndRoot gpfr = _generatedPackageFragmentRoot.get();
		IPackageFragmentRoot root = gpfr.root;
		if (root == null) {
			// If the generated package fragment root wasn't set, then our classpath
			// is incorrect. Add a marker and return.  We do this here, rather than in
			// the set() method, because if they're not going to generate any types
			// then it doesn't matter that the classpath is wrong.
			String message = Messages.bind(Messages.GeneratedFileManager_missing_classpath_entry,
					new String[] { gpfr.name });
			IMarker marker = _jProject.getProject().createMarker(AptPlugin.APT_CONFIG_PROBLEM_MARKER);
			marker.setAttributes(new String[] { IMarker.MESSAGE, IMarker.SEVERITY, IMarker.SOURCE_ID },
					new Object[] { message,	IMarker.SEVERITY_ERROR, AptPlugin.APT_MARKER_SOURCE_ID });
			// disable any future type generation
			_skipTypeGeneration = true;
			return null;
		}

		// Do the new contents differ from what is already on disk?
		// We need to know so we can tell the caller whether this is a modification.
		IFile file = getIFileForTypeName(typeName);
		boolean contentsDiffer = compareFileContents(contents, file);

		try {
			if (contentsDiffer) {
				final String[] names = parseTypeName(typeName);
				final String pkgName = names[0];
				final String cuName = names[1];

				// Get a list of the folders that will have to be created for this package to exist
				IFolder genSrcFolder = (IFolder) root.getResource();
				final Set<IFolder> newFolders = computeNewPackageFolders(pkgName, genSrcFolder);

				// Create the package fragment in the Java Model.  This creates all needed parent folders.
				IPackageFragment pkgFrag = _CUHELPER.createPackageFragment(pkgName, root, progressMonitor);

				if (pkgFrag == null) {
					pkgFrag = root.getPackageFragment(pkgName);
					// Force an error status
					IStatus validatePath = root.getJavaProject().getProject().getWorkspace().validatePath(null, IResource.FOLDER);
					String message = "invalid name for package:" + pkgName; //$NON-NLS-1$
					AptPlugin.log(new CoreException(validatePath), message);
					return null;
				}

				// Mark all newly created folders (but not pre-existing ones) as derived.
				for (IContainer folder : newFolders) {
					try {
						folder.setDerived(true, progressMonitor);
					} catch (CoreException e) {
						AptPlugin.logWarning(e, "Unable to mark generated type folder as derived: " + folder.getName()); //$NON-NLS-1$
						break;
					}
				}

				saveCompilationUnit(pkgFrag, cuName, contents, progressMonitor);
			}

			// during a batch build, parentFile will be null.
			// Only keep track of ownership in iterative builds
			addBuiltFileToMaps(parentFiles, file, true);
			if (clearDuringReconcile) {
				_clearDuringReconcile.add(file);
			}

			// Mark the file as derived. Note that certain user actions may have
			// deleted this file before we get here, so if the file doesn't
			// exist, marking it derived throws a ResourceException.
			if (file.exists()) {
				file.setDerived(true, progressMonitor);
			}
			// We used to also make the file read-only. This is a bad idea,
			// as refactorings then fail in the future, which is worse
			// than allowing a user to modify a generated file.

			assert checkIntegrity();

			return new FileGenerationResult(file, contentsDiffer);
		} catch (CoreException e) {
			AptPlugin.log(e, "Unable to generate type " + typeName); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * This function generates a type "in-memory" by creating or updating a working copy
	 * with the specified contents. The generated-source folder must be configured
	 * correctly for this to work. This method takes no locks, so it is safe to call when
	 * holding fine-grained resource locks (e.g., during some reconcile paths). Since this
	 * only works on an in-memory working copy of the type, the IFile for the generated
	 * type might not exist on disk. Likewise, the corresponding package directories of
	 * type-name might not exist on disk.
	 *
	 * TODO: figure out how to create a working copy with a client-specified character set
	 *
	 * @param parentCompilationUnit the parent compilation unit.
	 * @param typeName the dot-separated java type name for the new type
	 * @param contents the contents of the new type
	 * @return The FileGenerationResult. This will return null if the generated source
	 *         folder is not configured, or if there is some other error during type
	 *         generation.
	 */
	public FileGenerationResult generateFileDuringReconcile(ICompilationUnit parentCompilationUnit, String typeName,
			String contents) throws CoreException
	{
		if (!GENERATE_TYPE_DURING_RECONCILE)
			return null;

		IFile parentFile = (IFile) parentCompilationUnit.getResource();

		ICompilationUnit workingCopy = getWorkingCopyForReconcile(parentFile, typeName, _CUHELPER);

		// Update its contents and recursively reconcile
		boolean modified = _CUHELPER.updateWorkingCopyContents(
				contents, workingCopy, parentCompilationUnit.getOwner(), RECURSIVE_RECONCILE);
		if (AptPlugin.DEBUG_GFM) {
			if (modified)
				AptPlugin.trace("working copy modified during reconcile: " + typeName); //$NON-NLS-1$
			else
				AptPlugin.trace("working copy unmodified during reconcile: " + typeName); //$NON-NLS-1$
		}

		IFile generatedFile = (IFile) workingCopy.getResource();
		return new FileGenerationResult(generatedFile, modified);
	}

	/**
	 * @param parent -
	 *            the parent file that you want to get generated files for
	 * @return Set of IFile instances that are the files known to be generated by this
	 *         parent, or an empty collection if there are none.
	 *
	 * @see #isParentFile(IFile)
	 * @see #isGeneratedFile(IFile)
	 */
	public synchronized Set<IFile> getGeneratedFilesForParent(IFile parent)
	{
		return _buildDeps.getValues(parent);
	}

	/**
	 * returns true if the specified file is a generated file (i.e., it has one or more
	 * parent files)
	 *
	 * @param f
	 *            the file in question
	 * @return true
	 */
	public synchronized boolean isGeneratedFile(IFile f)
	{
		return _buildDeps.containsValue(f);
	}



	/**
	 * returns true if the specified file is a parent file (i.e., it has one or more
	 * generated files)
	 *
	 * @param f -
	 *            the file in question
	 * @return true if the file is a parent, false otherwise
	 *
	 * @see #getGeneratedFilesForParent(IFile)
	 * @see #isGeneratedFile(IFile)
	 */
	public synchronized boolean isParentFile(IFile f)
	{
		return _buildDeps.containsKey(f);
	}

	/**
	 * Perform the actions necessary to respond to a clean.
	 */
	public void projectCleaned() {
		Iterable<ICompilationUnit> toDiscard = computeClean();
		for (ICompilationUnit wc : toDiscard) {
			_CUHELPER.discardWorkingCopy(wc);
		}
		if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace(
				"cleared build file dependencies"); //$NON-NLS-1$
	}

	/**
	 * Perform the actions necessary to respond to a project being closed.
	 * Throw out the reconcile-time information and working copies; throw
	 * out the build-time dependency information but leave its serialized
	 * version on disk in case the project is re-opened.
	 */
	public void projectClosed()
	{
		if (AptPlugin.DEBUG_GFM) AptPlugin.trace("discarding working copy state"); //$NON-NLS-1$
		List<ICompilationUnit> toDiscard;
		toDiscard = computeProjectClosed(false);
		for (ICompilationUnit wc : toDiscard) {
			_CUHELPER.discardWorkingCopy(wc);
		}
	}

	/**
	 * Perform the actions necessary to respond to a project being deleted.
	 * Throw out everything related to the project, including its serialized
	 * build dependencies.
	 */
	public void projectDeleted()
	{
		if (AptPlugin.DEBUG_GFM) AptPlugin.trace("discarding all state"); //$NON-NLS-1$
		List<ICompilationUnit> toDiscard;
		toDiscard = computeProjectClosed(true);
		for (ICompilationUnit wc : toDiscard) {
			_CUHELPER.discardWorkingCopy(wc);
		}
	}

	/**
	 * Called at the start of reconcile in order to cache our package fragment root
	 */
	public void reconcileStarted()
	{
		_generatedPackageFragmentRoot.set();
	}

	/**
	 * Invoked when a working copy is released, ie, an editor is closed.  This
	 * includes IDE shutdown.
	 *
	 * @param wc
	 *            must not be null, but does not have to be a parent.
	 */
	public void workingCopyDiscarded(ICompilationUnit wc) throws CoreException
	{
		List<ICompilationUnit> toDiscard = removeFileFromReconcileMaps((IFile)(wc.getResource()));
		if (AptPlugin.DEBUG_GFM) AptPlugin.trace(
				"Working copy discarded: " + wc.getElementName() + //$NON-NLS-1$
				" removing " + toDiscard.size() + " children");  //$NON-NLS-1$//$NON-NLS-2$
		for (ICompilationUnit obsoleteWC : toDiscard) {
			_CUHELPER.discardWorkingCopy(obsoleteWC);
		}
	}

	/**
	 * Serialize the generated file dependency data for builds, so that when a workspace
	 * is reopened, incremental builds will work correctly.
	 */
	public void writeState()
	{
		_buildDeps.writeState();
	}

	/**
	 * Add a file dependency at build time. This updates the build dependency map but does
	 * not affect the reconcile-time dependencies.
	 * <p>
	 * This method only affects maps; it does not touch disk or modify working copies.
	 *
	 * @param isSource true for source files that will be compiled; false for non-source, e.g., text or xml.
	 */
	private synchronized void addBuiltFileToMaps(Collection<IFile> parentFiles, IFile generatedFile, boolean isSource)
	{
		// during a batch build, parentFile will be null.
		// Only keep track of ownership in iterative builds
		for (IFile parentFile : parentFiles) {
			if (parentFile != null) {
				boolean added = _buildDeps.put(parentFile, generatedFile, isSource);
				if (AptPlugin.DEBUG_GFM_MAPS) {
					if (added)
						AptPlugin.trace("build file dependency added: " + parentFile + " -> " + generatedFile); //$NON-NLS-1$//$NON-NLS-2$
					else
						AptPlugin.trace("build file dependency already exists: " + parentFile + " -> " + generatedFile); //$NON-NLS-1$//$NON-NLS-2$
				}
			}
		}
	}

	/**
	 * Check integrity of data structures.
	 * @return true always, so that it can be called within an assert to turn it off at runtime
	 */
	private synchronized boolean checkIntegrity() throws IllegalStateException
	{
		if (!ENABLE_INTEGRITY_CHECKS || !AptPlugin.DEBUG_GFM_MAPS) {
			return true;
		}

		// There is a 1:1 correspondence between values in _reconcileDeps and
		// keys in _reconcileGenTypes.
		Set<IFile> depChildren = _reconcileDeps.getValueSet(); // copy - safe to modify
		Set<IFile> genTypes = _reconcileGenTypes.keySet(); // not a copy!
		List<IFile> extraFiles = new ArrayList<>();
		for (IFile f : genTypes) {
			if (!depChildren.remove(f)) {
				extraFiles.add(f);
			}
		}
		if (!extraFiles.isEmpty()) {
			logExtraFiles("File(s) in reconcile-generated list but not in reconcile dependency map: ", //$NON-NLS-1$
					extraFiles);
		}
		if (!depChildren.isEmpty()) {
			logExtraFiles("File(s) in reconcile dependency map but not in reconcile-generated list: ", //$NON-NLS-1$
					depChildren);
		}

		// Every file in _clearDuringReconcile must be a value in _buildDeps.
		List<IFile> extraClearDuringReconcileFiles = new ArrayList<>();
		for (IFile clearDuringReconcile : _clearDuringReconcile) {
			if (!_buildDeps.containsValue(clearDuringReconcile)) {
				extraClearDuringReconcileFiles.add(clearDuringReconcile);
			}
		}
		if (!extraClearDuringReconcileFiles.isEmpty()) {
			logExtraFiles("File(s) in list to clear during reconcile but not in build dependency map: ", //$NON-NLS-1$
					extraClearDuringReconcileFiles);
		}

		// Every key in _hiddenBuiltTypes must be a value in _reconcileNonDeps.
		List<IFile> extraHiddenTypes = new ArrayList<>();
		for (IFile hidden : _hiddenBuiltTypes.keySet()) {
			if (!_reconcileNonDeps.containsValue(hidden)) {
				extraHiddenTypes.add(hidden);
			}
		}
		if (!extraHiddenTypes.isEmpty()) {
			logExtraFiles("File(s) in hidden types list but not in reconcile-obsoleted list: ", //$NON-NLS-1$
					extraHiddenTypes);
		}

		// There can be no parent/child pairs that exist in both _reconcileDeps
		// and _reconcileNonDeps.
		Map<IFile, IFile> reconcileOverlaps = new HashMap<>();
		for (IFile parent : _reconcileNonDeps.getKeySet()) {
			for (IFile child : _reconcileNonDeps.getValues(parent)) {
				if (_reconcileDeps.containsKeyValuePair(parent, child)) {
					reconcileOverlaps.put(parent, child);
				}
			}
		}
		if (!reconcileOverlaps.isEmpty()) {
			logExtraFilePairs("Entries exist in both reconcile map and reconcile-obsoleted maps: ",  //$NON-NLS-1$
					reconcileOverlaps);
		}

		// Every parent/child pair in _reconcileNonDeps must have a matching
		// parent/child pair in _buildDeps.
		Map<IFile, IFile> extraNonDeps = new HashMap<>();
		for (IFile parent : _reconcileNonDeps.getKeySet()) {
			for (IFile child : _reconcileNonDeps.getValues(parent)) {
				if (!_buildDeps.containsKeyValuePair(parent, child)) {
					extraNonDeps.put(parent, child);
				}
			}
		}
		if (!extraNonDeps.isEmpty()) {
			logExtraFilePairs("Entries exist in reconcile-obsoleted map but not in build map: ", //$NON-NLS-1$
					extraNonDeps);
		}

		// Values in _hiddenBuiltTypes must not be null
		List<IFile> nullHiddenTypes = new ArrayList<>();
		for (Map.Entry<IFile, ICompilationUnit> entry : _hiddenBuiltTypes.entrySet()) {
			if (entry.getValue() == null) {
				nullHiddenTypes.add(entry.getKey());
			}
		}
		if (!nullHiddenTypes.isEmpty()) {
			logExtraFiles("Null entries in hidden type list: ", nullHiddenTypes); //$NON-NLS-1$
		}

		// Values in _reconcileGenTypes must not be null
		List<IFile> nullReconcileTypes = new ArrayList<>();
		for (Map.Entry<IFile, ICompilationUnit> entry : _reconcileGenTypes.entrySet()) {
			if (entry.getValue() == null) {
				nullReconcileTypes.add(entry.getKey());
			}
		}
		if (!nullReconcileTypes.isEmpty()) {
			logExtraFiles("Null entries in reconcile type list: ", nullReconcileTypes); //$NON-NLS-1$
		}

		return true;
	}

	/**
	 * Clear the working copy maps, that is, the reconcile-time dependency information.
	 * Returns a list of working copies that are no longer referenced and should be
	 * discarded. Typically called when a project is being closed or deleted.
	 * <p>
	 * It's not obvious we actually need this. As long as the IDE discards the parent
	 * working copies before the whole GeneratedFileManager is discarded, there'll be
	 * nothing left to clear by the time we get here. This is a "just in case."
	 * <p>
	 * This method affects maps only; it does not touch disk nor create, modify, nor
	 * discard any working copies. This method is atomic with respect to data structure
	 * integrity.
	 *
	 * @param deleteState
	 *            true if this should delete the serialized build dependencies.
	 *
	 * @return a list of working copies which must be discarded by the caller
	 */
	private synchronized List<ICompilationUnit> computeProjectClosed(boolean deleteState)
	{
		int size = _hiddenBuiltTypes.size() + _reconcileGenTypes.size();
		List<ICompilationUnit> toDiscard = new ArrayList<>(size);
		toDiscard.addAll(_hiddenBuiltTypes.values());
		toDiscard.addAll(_reconcileGenTypes.values());
		_reconcileGenTypes.clear();
		_hiddenBuiltTypes.clear();
		_reconcileDeps.clear();
		_reconcileNonDeps.clear();

		if (deleteState) {
			_buildDeps.clearState();
		}
		else {
			_buildDeps.clear();
		}
		_clearDuringReconcile.clear();

		assert checkIntegrity();
		return toDiscard;
	}

	/**
	 * Compare <code>contents</code> with the contents of <code>file</code>.
	 * @param contents the text to compare with the file's contents on disk.
	 * @param file does not have to exist.
	 * @return true if the file on disk cannot be read, or if its contents differ.
	 */
	private boolean compareFileContents(String contents, IFile file)
	{
		boolean contentsDiffer = true;
		if (file.exists()) {
			InputStream oldData = null;
			InputStream is = null;
			try {
				is = new ByteArrayInputStream(contents.getBytes());
				oldData = new BufferedInputStream(file.getContents());
				contentsDiffer = !FileSystemUtil.compareStreams(oldData, is);
			} catch (CoreException ce) {
				// Do nothing. Assume the new content is different
			} finally {
				if (oldData != null) {
					try {
						oldData.close();
					} catch (IOException ioe) {
					}
				}
				if (is != null) {
					try {
						is.close();
					} catch (IOException ioe) {
					}
				}
			}
		}
		return contentsDiffer;
	}

	/**
	 * Make the map updates necessary to discard build state. Typically called while
	 * processing a clean. In addition to throwing away the build dependencies, we also
	 * throw away all the blank working copies used to hide existing generated files, on
	 * the premise that since they were deleted in the clean we don't need to hide them
	 * any more.  We leave the rest of the reconcile-time dependency info, though.
	 * <p>
	 * This method is atomic with regard to data structure integrity.  This method
	 * does not touch disk nor create, discard, or modify compilation units.
	 *
	 * @return a list of working copies that the caller must discard by calling
	 *         {@link CompilationUnitHelper#discardWorkingCopy(ICompilationUnit)}.
	 */
	private synchronized List<ICompilationUnit> computeClean()
	{
		_buildDeps.clearState();
		_clearDuringReconcile.clear();
		_reconcileNonDeps.clear();
		List<ICompilationUnit> toDiscard = new ArrayList<>(_hiddenBuiltTypes.values());
		_hiddenBuiltTypes.clear();

		assert checkIntegrity();
		return toDiscard;
	}

	/**
	 * Get the IFolder handles for any additional folders needed to
	 * contain a type in package <code>pkgName</code> under root
	 * <code>parent</code>.  This does not actually create the folders
	 * on disk, it just gets resource handles.
	 *
	 * @return a set containing all the newly created folders.
	 */
	private Set<IFolder> computeNewPackageFolders(String pkgName, IFolder parent)
	{
		Set<IFolder> newFolders = new HashSet<>();
		String[] folders = _PACKAGE_DELIMITER.split(pkgName);
		for (String folderName : folders) {
			final IFolder folder = parent.getFolder(folderName);
			if (!folder.exists()) {
				newFolders.add(folder);
			}
			parent = folder;
		}
		return newFolders;
	}

	/**
	 * Calculate the list of previously generated files that are no longer
	 * being generated and thus need to be deleted.
	 * <p>
	 * This method does not touch the disk, nor does it create, update, or
	 * discard working copies.  This method is atomic with regard to the
	 * integrity of data structures.
	 *
	 * @param parentFile only files solely parented by this file will be
	 * added to the list to be deleted.
	 * @param newlyGeneratedFiles files on this list will be spared.
	 * @param toDiscard must be non-null. The caller should pass in an empty
	 * list; on return the list will contain working copies which the caller
	 * is responsible for discarding.
	 * @param toReport must be non-null.  The caller should pass in an empty
	 * set; on return the set will contain IFiles representing source files
	 * (but not non-source files such as text or xml files) which are being
	 * deleted and which should therefore be removed from compilation.
	 * @return a list of files which the caller should delete, ie by calling
	 * {@link #deletePhysicalFile(IFile)}.
	 */
	private synchronized Set<IFile> computeObsoleteFiles(
			IFile parentFile, Set<IFile> newlyGeneratedFiles,
			List<ICompilationUnit> toDiscard,
			Set<IFile> toReport)
	{
		Set<IFile> deleted = new HashSet<>();
		Set<IFile> obsoleteFiles = _buildDeps.getValues(parentFile);
		// spare all the newly generated files
		obsoleteFiles.removeAll(newlyGeneratedFiles);
		for (IFile generatedFile : obsoleteFiles) {
			boolean isSource = _buildDeps.isSource(generatedFile);
			_buildDeps.remove(parentFile, generatedFile);
			if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace(
					"removed build file dependency: " + parentFile + " -> " + generatedFile); //$NON-NLS-1$ //$NON-NLS-2$
			// If the file is still parented by any other parent, spare it
			if (!_buildDeps.containsValue(generatedFile)) {
				deleted.add(generatedFile);
				if (isSource) {
					toReport.add(generatedFile);
				}
			}
		}
		_clearDuringReconcile.removeAll(deleted);
		toDiscard.addAll(computeObsoleteHiddenTypes(parentFile, deleted));
		assert checkIntegrity();
		return deleted;
	}

	/**
		 * Calculate what needs to happen to working copies after a reconcile in order to get
		 * rid of any no-longer-generated files. If there's an existing generated file, we
		 * need to hide it with a blank working copy; if there's no existing file, we need to
		 * get rid of any generated working copy.
		 * <p>
		 * A case to keep in mind: the user imports a project with already-existing generated
		 * files, but without a serialized build dependency map.  Then they edit a parent
		 * file, causing a generated type to disappear.  We need to discover and hide the
		 * generated file on disk, even though it is not in the build-time dependency map.
		 *
		 * @param parentFile
		 *            the parent type being reconciled, which need not exist on disk.
		 * @param newlyGeneratedFiles
		 *            the set of files generated in the last reconcile
		 * @param toSetBlank
		 *            a list, to which this will add files that the caller must then set blank
		 *            with {@link CompilationUnitHelper#updateWorkingCopyContents(String,
		 *            ICompilationUnit, WorkingCopyOwner, boolean)}
		 * @param toDiscard
		 *            a list, to which this will add files that the caller must then discard
		 *            with {@link CompilationUnitHelper#discardWorkingCopy(ICompilationUnit)}.
		 */
		private synchronized void computeObsoleteReconcileTypes(
				IFile parentFile, Set<IFile> newlyGeneratedFiles,
				CompilationUnitHelper cuh,
				List<ICompilationUnit> toSetBlank, List<ICompilationUnit> toDiscard)
		{
			// Get types previously but no longer generated during reconcile
			Set<IFile> obsoleteFiles = _reconcileDeps.getValues(parentFile);
			Map<IFile, ICompilationUnit> typesToDiscard = new HashMap<>();
			obsoleteFiles.removeAll(newlyGeneratedFiles);
			for (IFile obsoleteFile : obsoleteFiles) {
				_reconcileDeps.remove(parentFile, obsoleteFile);
				if (_reconcileDeps.getKeys(obsoleteFile).isEmpty()) {
					ICompilationUnit wc = _reconcileGenTypes.remove(obsoleteFile);
					assert wc != null :
						"Value in reconcile deps missing from reconcile type list: " + obsoleteFile; //$NON-NLS-1$
					typesToDiscard.put(obsoleteFile, wc);
				}
			}

			Set<IFile> builtChildren = _buildDeps.getValues(parentFile);
			builtChildren.retainAll(_clearDuringReconcile);
			builtChildren.removeAll(newlyGeneratedFiles);
			for (IFile builtChild : builtChildren) {
				_reconcileNonDeps.put(parentFile, builtChild);
				// If it's on typesToDiscard there are no other reconcile-time parents.
				// If there are no other parents that are not masked by a nonDep entry...
				boolean foundOtherParent = false;
				Set<IFile> parents = _buildDeps.getKeys(builtChild);
				parents.remove(parentFile);
				for (IFile otherParent : parents) {
					if (!_reconcileNonDeps.containsKeyValuePair(otherParent, builtChild)) {
						foundOtherParent = true;
						break;
					}
				}
				if (!foundOtherParent) {
					ICompilationUnit wc = typesToDiscard.remove(builtChild);
					if (wc == null) {
						IPackageFragmentRoot root = _generatedPackageFragmentRoot.get().root;
						String typeName = getTypeNameForDerivedFile(builtChild);
						wc = cuh.getWorkingCopy(typeName, root);
					}
					_hiddenBuiltTypes.put(builtChild, wc);
					toSetBlank.add(wc);
				}
			}

			// discard any working copies that we're not setting blank
			toDiscard.addAll(typesToDiscard.values());

			assert checkIntegrity();
		}

	/**
	 * Calculate the list of blank working copies that are no longer needed because the
	 * files that they hide have been deleted during a build. Remove these working copies
	 * from the _hiddenBuiltTypes list and return them in a list. The caller MUST then
	 * discard the contents of the list (outside of any synchronized block) by calling
	 * CompilationUnitHelper.discardWorkingCopy().
	 * <p>
	 * This method does not touch the disk and does not create, update, or discard working
	 * copies. This method is atomic with regard to data structure integrity.
	 *
	 * @param parentFile
	 *            used to be a parent but may no longer be.
	 * @param deletedFiles
	 *            a list of files which are being deleted, which might or might not have
	 *            been hidden by blank working copies.
	 *
	 * @return a list of working copies which the caller must discard
	 */
	private synchronized List<ICompilationUnit> computeObsoleteHiddenTypes(IFile parentFile, Set<IFile> deletedFiles)
	{
		List<ICompilationUnit> toDiscard = new ArrayList<>();
		for (IFile deletedFile : deletedFiles) {
			if (_reconcileNonDeps.remove(parentFile, deletedFile)) {
				ICompilationUnit wc = _hiddenBuiltTypes.remove(deletedFile);
				if (wc != null) {
					toDiscard.add(wc);
				}
			}
		}
		assert checkIntegrity();
		return toDiscard;
	}

	/**
	 * Delete a generated file from disk. Also deletes the parent folder hierarchy, up to
	 * but not including the root generated source folder, as long as the folders are
	 * empty and are marked as "derived".
	 * <p>
	 * This does not affect or refer to the dependency maps.
	 *
	 * @param file is assumed to be under the generated source folder.
	 */
	private void deletePhysicalFile(IFile file)
	{
		final IFolder genFolder = _gsfm.getFolder();
		assert genFolder != null : "Generated folder == null"; //$NON-NLS-1$
		IContainer parent = file.getParent(); // parent in the folder sense,
		// not the typegen sense
		try {
			if (AptPlugin.DEBUG_GFM) AptPlugin.trace(
					"delete physical file: " + file); //$NON-NLS-1$
			file.delete(true, true, /* progressMonitor */null);
		} catch (CoreException e) {
			// File was locked or read-only
			AptPlugin.logWarning(e, "Unable to delete generated file: " + file); //$NON-NLS-1$
		}
		// Delete the parent folders
		while (!genFolder.equals(parent) && parent != null && parent.isDerived()) {
			IResource[] members = null;
			try {
				members = parent.members();
			} catch (CoreException e) {
				AptPlugin.logWarning(e, "Unable to read contents of generated file folder " + parent); //$NON-NLS-1$
			}
			IContainer grandParent = parent.getParent();
			// last one turns the light off.
			if (members == null || members.length == 0)
				try {
					parent.delete(true, /* progressMonitor */null);
				} catch (CoreException e) {
					AptPlugin.logWarning(e, "Unable to delete generated file folder " + parent); //$NON-NLS-1$
				}
			else
				break;
			parent = grandParent;
		}
	}

	/**
	 * Given a typename a.b.C, this will return the IFile for the type name, where the
	 * IFile is in the GENERATED_SOURCE_FOLDER_NAME.
	 * <p>
	 * This does not affect or refer to the dependency maps.
	 */
	public IFile getIFileForTypeName(String typeName)
	{
		// split the type name into its parts
		String[] parts = _PACKAGE_DELIMITER.split(typeName);

		IFolder folder = _gsfm.getFolder();
		for (int i = 0; i < parts.length - 1; i++)
			folder = folder.getFolder(parts[i]);

		// the last part of the type name is the file name
		String fileName = parts[parts.length - 1] + ".java"; //$NON-NLS-1$
		IFile file = folder.getFile(fileName);
		return file;
	}

	/**
	 * given file f, return the typename corresponding to the file.  This assumes
	 * that derived files use java naming rules (i.e., type "a.b.C" will be file
	 * "a/b/C.java".
	 */
	private String getTypeNameForDerivedFile( IFile f )
	{
		IPath p = f.getFullPath();

		IFolder folder = _gsfm.getFolder();
		IPath generatedSourcePath = folder.getFullPath();

		int count = p.matchingFirstSegments( generatedSourcePath );
		p = p.removeFirstSegments( count );

		String s = p.toPortableString();
		int idx = s.lastIndexOf( '.' );
		s = p.toPortableString().replace( '/', '.' );
		return s.substring( 0, idx );
	}

	/**
	 * Get a working copy for the specified generated type.  If we already have
	 * one cached, use that; if not, create a new one.  Update the reconcile-time
	 * dependency maps.
	 * <p>
	 * This method does not touch disk, nor does it update or discard any working
	 * copies.  However, it may call CompilationUnitHelper to get a new working copy.
	 * This method is atomic with respect to data structures.
	 *
	 * @param parentFile the IFile whose processing is causing the new type to be generated
	 * @param typeName the name of the type to be generated
	 * @param cuh the CompilationUnitHelper utility object
	 * @return a working copy ready to be updated with the new type's contents
	 */
	private synchronized ICompilationUnit getWorkingCopyForReconcile(IFile parentFile, String typeName, CompilationUnitHelper cuh)
	{
		IPackageFragmentRoot root = _generatedPackageFragmentRoot.get().root;
		IFile generatedFile = getIFileForTypeName(typeName);
		ICompilationUnit workingCopy;

		workingCopy = _hiddenBuiltTypes.remove(generatedFile);
		if (null != workingCopy) {
			// file is currently hidden with a blank WC. Move that WC to the regular list.
			_reconcileNonDeps.remove(parentFile, generatedFile);
			_reconcileGenTypes.put(generatedFile, workingCopy);
			_reconcileDeps.put(parentFile, generatedFile);
			if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace(
					"moved working copy from hidden to regular list: " + generatedFile); //$NON-NLS-1$
		} else {
			workingCopy = _reconcileGenTypes.get(generatedFile);
			if (null != workingCopy) {
				if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace(
						"obtained existing working copy from regular list: " + generatedFile); //$NON-NLS-1$
			} else {
				// we've not yet created a working copy for this file, so make one now.
				workingCopy = cuh.getWorkingCopy(typeName, root);
				_reconcileDeps.put(parentFile, generatedFile);
				_reconcileGenTypes.put(generatedFile, workingCopy);
				if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace(
						"added new working copy to regular list: " + generatedFile); //$NON-NLS-1$
			}
		}

		assert checkIntegrity();
		return workingCopy;
	}

	/**
	 * Check whether a child file has any parents that could apply in reconcile.
	 *
	 * @return true if <code>child</code> has no other parents in
	 *         {@link #_reconcileDeps}, and also no other parents in {@link #_buildDeps}
	 *         that are not masked by a corresponding entry in {@link #_reconcileNonDeps}.
	 */
	private boolean hasNoOtherReconcileParents(IFile child, IFile parent) {
		if (_reconcileDeps.valueHasOtherKeys(child, parent))
			return true;
		Set<IFile> buildParents = _buildDeps.getKeys(child);
		buildParents.remove(parent);
		buildParents.removeAll(_reconcileNonDeps.getKeys(child));
		return buildParents.isEmpty();
	}

	/**
	 * Log extra file pairs, with a message like "message p1->g1, p2->g2".
	 * Assumes that pairs has at least one entry.
	 */
	private void logExtraFilePairs(String message, Map<IFile, IFile> pairs) {
		StringBuilder sb = new StringBuilder();
		sb.append(message);
		Iterator<Map.Entry<IFile, IFile>> iter = pairs.entrySet().iterator();
		while (true) {
			Map.Entry<IFile, IFile> entry = iter.next();
			sb.append(entry.getKey().getName());
			sb.append("->"); //$NON-NLS-1$
			sb.append(entry.getValue().getName());
			if (!iter.hasNext()) {
				break;
			}
			sb.append(", "); //$NON-NLS-1$
		}
		String s = sb.toString();
		AptPlugin.log(new IllegalStateException(s), s);
	}

	/**
	 * Log extra files, with a message like "message file1, file2, file3".
	 * Assumes that files has at least one entry.
	 */
	private void logExtraFiles(String message, Iterable<IFile> files) {
		StringBuilder sb = new StringBuilder();
		sb.append(message);
		Iterator<IFile> iter = files.iterator();
		while (true) {
			sb.append(iter.next().getName());
			if (!iter.hasNext()) {
				break;
			}
			sb.append(", "); //$NON-NLS-1$
		}
		String s = sb.toString();
		AptPlugin.log(new IllegalStateException(s), s);
	}

	/**
	 * Given a fully qualified type name, generate the package name and the local filename
	 * including the extension. For instance, type name <code>foo.bar.Baz</code> is
	 * turned into package <code>foo.bar</code> and filename <code>Baz.java</code>.
	 * <p>
	 * TODO: this is almost identical to code in CompilationUnitHelper.  Is the difference
	 * intentional?
	 *
	 * @param qualifiedName
	 *            a fully qualified type name
	 * @return a String array containing {package name, filename}
	 */
	private static String[] parseTypeName(String qualifiedName) {

		//TODO: the code in CompilationUnitHelper doesn't perform this check.  Should it?
		if (qualifiedName.indexOf('/') != -1)
			qualifiedName = qualifiedName.replace('/', '.');

		String[] names = new String[2];
		String pkgName;
		String fname;
		int idx = qualifiedName.lastIndexOf( '.' );
		if ( idx > 0 )
		{
		    pkgName = qualifiedName.substring( 0, idx );
		    fname =
				qualifiedName.substring(idx + 1, qualifiedName.length()) + ".java"; //$NON-NLS-1$
		}
		else
		{
			pkgName = ""; //$NON-NLS-1$
			fname = qualifiedName + ".java"; //$NON-NLS-1$
		}
		names[0] = pkgName;
		names[1] = fname;
		return names;
	}

	/**
	 * Remove a file from the build-time dependency maps, and calculate the consequences
	 * of the removal. This is called in response to a file being deleted by the
	 * environment.
	 * <p>
	 * This operation affects the maps only. This operation is atomic with respect to map
	 * integrity. This operation does not touch the disk nor create, update, or discard
	 * any working copies.
	 *
	 * @param f
	 *            can be a parent, generated, both, or neither.
	 * @return a list of generated files that are no longer relevant and must be deleted.
	 *         This operation must be done by the caller without holding any locks. The
	 *         list may be empty but will not be null.
	 */
	private synchronized List<IFile> removeFileFromBuildMaps(IFile f)
	{
		List<IFile> toDelete = new ArrayList<>();
		// Is this file the sole parent of files generated during build?
		// If so, add them to the deletion list. Then remove the file from
		// the build dependency list.
		Set<IFile> childFiles = _buildDeps.getValues(f);
		for (IFile childFile : childFiles) {
			Set<IFile> parentFiles = _buildDeps.getKeys(childFile);
			if (parentFiles.size() == 1 && parentFiles.contains(f)) {
				toDelete.add(childFile);
			}
		}
		boolean removed = _buildDeps.removeKey(f);
		if (removed) {
			if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace(
					"removed parent file from build dependencies: " + f); //$NON-NLS-1$
		}

		assert checkIntegrity();
		return toDelete;
	}

	/**
	 * Remove the generated children of a working copy from the reconcile dependency maps.
	 * Typically invoked when a working copy of a parent file has been discarded by the
	 * editor; in this case we want to remove any generated working copies that it
	 * parented.
	 * <p>
	 * This method does not touch disk nor create, modify, or discard working copies. This
	 * method is atomic with regard to data structure integrity.
	 *
	 * @param file
	 *            a file representing a working copy that is not necessarily a parent or
	 *            generated file
	 * @return a list of generated working copies that are no longer referenced and should
	 *         be discarded by calling
	 *         {@link CompilationUnitHelper#discardWorkingCopy(ICompilationUnit)}
	 */
	private synchronized List<ICompilationUnit> removeFileFromReconcileMaps(IFile file)
	{
		List<ICompilationUnit> toDiscard = new ArrayList<>();
		// remove all the orphaned children
		Set<IFile> genFiles = _reconcileDeps.getValues(file);
		for (IFile child : genFiles) {
			if (hasNoOtherReconcileParents(child, file)) {
				ICompilationUnit childWC = _reconcileGenTypes.remove(child);
				assert null != childWC : "Every value in _reconcileDeps must be a key in _reconcileGenTypes"; //$NON-NLS-1$
				toDiscard.add(childWC);
			}
		}
		_reconcileDeps.removeKey(file);

		// remove obsolete entries in non-generated list
		Set<IFile> nonGenFiles = _reconcileNonDeps.getValues(file);
		for (IFile child : nonGenFiles) {
			ICompilationUnit hidingWC = _hiddenBuiltTypes.remove(child);
			if (null != hidingWC) {
				toDiscard.add(hidingWC);
			}
		}
		_reconcileNonDeps.removeKey(file);

		assert checkIntegrity();
		return toDiscard;
	}

	/**
	 * Write <code>contents</code> to disk in the form of a compilation unit named
	 * <code>name</code> under package fragment <code>pkgFrag</code>. The way in
	 * which the write is done depends whether the compilation unit is a working copy.
	 * <p>
	 * The working copy is used in reconcile. In principle changing the contents during
	 * build should be a problem, since the Java builder is based on file contents rather
	 * than on the current Java Model. However, annotation processors get their type info
	 * from the Java Model even during build, so there is in general no difference between
	 * build and reconcile. This causes certain bugs (if a build is performed while there
	 * is unsaved content in editors), so it may change in the future, and this routine
	 * will need to be fixed. - WHarley 11/06
	 * <p>
	 * This method touches the disk and modifies working copies. It can only be called
	 * during build, not during reconcile, and it should not be called while holding any
	 * locks (other than the workspace rules held by the build).
	 *
	 * @param pkgFrag
	 *            the package fragment in which the type will be created. The fragment's
	 *            folders must already exist on disk.
	 * @param cuName
	 *            the simple name of the type, with extension, such as 'Obj.java'
	 * @param contents
	 *            the text of the compilation unit
	 */
	private void saveCompilationUnit(IPackageFragment pkgFrag, final String cuName, String contents,
			IProgressMonitor progressMonitor)
	{

		ICompilationUnit unit = pkgFrag.getCompilationUnit(cuName);
		boolean isWorkingCopy = unit.isWorkingCopy();
		if (isWorkingCopy) {
			try {
				// If we have a working copy, all we
				// need to do is update its contents and commit it...
				_CUHELPER.commitNewContents(unit, contents, progressMonitor);
				if (AptPlugin.DEBUG_GFM) AptPlugin.trace(
						"Committed existing working copy during build: " + unit.getElementName()); //$NON-NLS-1$
			}
			catch (JavaModelException e) {
				// ...unless, that is, the resource has been deleted behind our back
				// due to a clean.  In that case, discard the working copy and try again.
				if (e.getJavaModelStatus().getCode() == IJavaModelStatusConstants.INVALID_RESOURCE) {
					_CUHELPER.discardWorkingCopy(unit);
					isWorkingCopy = false;
					if (AptPlugin.DEBUG_GFM) AptPlugin.trace(
							"Discarded invalid existing working copy in order to try again: " + unit.getElementName()); //$NON-NLS-1$
				}
				else {
					AptPlugin.log(e, "Unable to commit working copy to disk: " + unit.getElementName()); //$NON-NLS-1$
					return;
				}
			}
		}
		if (!isWorkingCopy) {
			try {
				unit = pkgFrag.createCompilationUnit(cuName, contents, true, progressMonitor);
				if (AptPlugin.DEBUG_GFM) AptPlugin.trace(
						"Created compilation unit during build: " + unit.getElementName()); //$NON-NLS-1$
			} catch (JavaModelException e) {
				AptPlugin.log(e, "Unable to create compilation unit on disk: " +  //$NON-NLS-1$
						cuName + " in pkg fragment: " + pkgFrag.getElementName()); //$NON-NLS-1$
			}
		}
	}

}
