/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.internal.Messages;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.apt.core.internal.util.ManyToMany;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
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
 * <code>_parentToGenFiles</code> is a many-to-many map that tracks which parent files
 * are responsible for which generated files. Entries in this map are created when files
 * are created during builds. This map is serialized so that dependencies can be reloaded
 * when a project is opened without having to do a full build.
 * <p>
 * When types are generated during reconcile, they are not actually laid down on disk (ie
 * we do not commit the working copy). However, the file handles are still used as keys
 * into the various maps in this case.
 * <p>
 * <code>_parentToGenWorkingCopies</code> is the reconcile-time analogue of
 * <code>_parentToGenFiles</code>.  This map is not serialized.
 * <p>
 * Given a working copy, it is easy to determine the IFile that it models by calling
 * <code>ICompilationUnit.getResource()</code>.  To go the other way, we store maps
 * of IFile to ICompilationUnit.  Working copies that represent generated types are
 * stored in <code>_workingCopies</code>; working copies that represent deleted types
 * are stored in <code>_hiddenBuiltTypes</code>.  
 * <p>
 * List invariants: for the many-to-many maps, every forward entry must correspond to a
 * reverse entry; this is managed (and verified) by the ManyToMany map code. Also, every
 * entry in the <code>_workingCopies</code> list must correspond to an entry in the
 * <code>_parentToGenWorkingCopies</code> map. There can be no overlap between these
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
	private final GeneratedFileMap _parentToGenFiles;

	/**
	 * Many-to-many map from parent files to working copies generated during reconcile.
	 * Both the keys and the values may correspond to files that exist on disk or only in
	 * memory. This map is used to keep track of dependencies created during reconcile,
	 * and is not accessed during build. This map is not serialized.
	 */
	private final ManyToMany<IFile, ICompilationUnit> _parentToGenWorkingCopies;

	/**
	 * Map of types that were generated during build but are being hidden by blank
	 * WorkingCopies. These are tracked separately from regular working copies for the
	 * sake of clarity. The keys all correspond to files that exist on disk; if they
	 * didn't, there would be no reason for an entry.
	 * <p>
	 * Every working copy exists either in this map or in {@link #_hiddenBuiltTypes}, but
	 * not in both. These maps exist to track the lifecycle of a working copy. When a new
	 * working copy is created, {@link ICompilationUnit#becomeWorkingCopy()} is called. If
	 * an entry is removed from this map without being added to the other,
	 * {@link ICompilationUnit#discardWorkingCopy()} must be called.
	 * 
	 * @see #_workingCopies
	 */
	private final Map<IFile, ICompilationUnit> _hiddenBuiltTypes;

	/**
	 * Cache of working copies (in-memory types created or modified during reconcile).
	 * Includes working copies that represent changes to types that were generated during
	 * a build and thus exist on disk, as well as working copies for types newly generated
	 * during reconcile that thus do not exist on disk.
	 * <p>
	 * Every working copy exists either in this map or in {@link #_hiddenBuiltTypes}, but
	 * not in both. These maps exist to track the lifecycle of a working copy. When a new
	 * working copy is created, {@link ICompilationUnit#becomeWorkingCopy()} is called. If
	 * an entry is removed from this map without being added to the other,
	 * {@link ICompilationUnit#discardWorkingCopy()} must be called.
	 * 
	 * @see #_hiddenBuiltTypes
	 */
	private final Map<IFile, ICompilationUnit> _workingCopies;

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
		_parentToGenFiles = new GeneratedFileMap(_jProject.getProject());
		_parentToGenWorkingCopies = new ManyToMany<IFile, ICompilationUnit>();
		_hiddenBuiltTypes = new HashMap<IFile, ICompilationUnit>();
		_workingCopies = new HashMap<IFile, ICompilationUnit>();
		_generatedPackageFragmentRoot = new GeneratedPackageFragmentRoot();
	}

	/**
	 * Add a non-Java-source entry to the build-time dependency maps. Java source files
	 * are added to the maps when they are generated, as by
	 * {@link #generateFileDuringBuild(IFile, String, String, IProgressMonitor)}, but
	 * files of other types must be added explicitly by the code that creates the file.
	 */
	public void addGeneratedFileDependency(IFile parentFile, IFile generatedFile)
	{
		addBuiltFileToMaps(parentFile, generatedFile);
	}

	/**
	 * Called at the start of build in order to cache our package fragment root
	 */
	public void compilationStarted()
	{
		try {
			// clear out any generated source folder config markers
			IMarker[] markers = _jProject.getProject().findMarkers(AptPlugin.APT_CONFIG_PROBLEM_MARKER, true,
					IResource.DEPTH_INFINITE);
			if (markers != null) {
				for (IMarker marker : markers)
					marker.delete();
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
		return _parentToGenWorkingCopies.containsKey(f);
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
	 * @return the set of files that were actually deleted, or an empty set
	 */
	public Set<IFile> deleteObsoleteFilesAfterBuild(IFile parentFile, Set<IFile> newlyGeneratedFiles)
	{
		Set<IFile> deleted;
		deleted = calculateDeletedFiles(parentFile, newlyGeneratedFiles);

		for (IFile toDelete : deleted) {
			if (AptPlugin.DEBUG_GFM) AptPlugin.trace(
					"deleted obsolete file during build: " + toDelete); //$NON-NLS-1$
			deletePhysicalFile(toDelete);
		}

		return deleted;
	}

	/**
	 * Invoked at the end of a reconcile to get rid of any files that are no longer being
	 * generated. If the file existed on disk, we can't actually delete it, we can only
	 * create a blank WorkingCopy to hide it. Therefore, we can only remove Java source
	 * files, not arbitrary files. If the file was generated during reconcile and exists
	 * only in memory, we can actually remove it altogether.
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

		List<ICompilationUnit> toSetBlank;
		List<ICompilationUnit> toDiscard;
		synchronized (this) {
			if (GENERATE_TYPE_DURING_RECONCILE) {
				toSetBlank = calculateHiddenTypes(parentFile, newlyGeneratedFiles, _CUHELPER);
			}
			else {
				toSetBlank = Collections.emptyList();
			}
			toDiscard = calculateObsoleteWorkingCopies(parentFile, newlyGeneratedFiles);
		}

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
	 * Clear all records of the generated types in this project: empty the maps, discard
	 * the working copies, and get rid of serialized file dependency information. This is
	 * invoked when a project is deleted (rather than merely closed).
	 */
	public void discardAllState()
	{
		discardWorkingCopyState();

		// Clear the generated file maps and delete any serialized build state
		_parentToGenFiles.clearState();
		if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace(
				"cleared build file dependencies"); //$NON-NLS-1$
	}

	/**
	 * Call ICompilationUnit.discardWorkingCopy() on all working copies of generated
	 * files, and clear the working copy maps. This is invoked when a project is closed,
	 * but not deleted: we need to forget about any in-memory types, but the generated
	 * files and serialized dependency information on disk are still valid.
	 */
	public void discardWorkingCopyState()
	{
		if (AptPlugin.DEBUG_GFM) AptPlugin.trace("discarding working copy state"); //$NON-NLS-1$
		List<ICompilationUnit> toDiscard;
		toDiscard = clearWorkingCopyMaps();
		for (ICompilationUnit wc : toDiscard) {
			_CUHELPER.discardWorkingCopy(wc);
		}
	}

	/**
	 * Called by the resource change listener when a file is deleted (eg by the user).
	 * Removes any files and working copies parented by this file, and removes the file
	 * from dependency maps if it is generated. If the file has a working copy associated,
	 * the working copy is discarded and removed from the dependency maps.
	 * 
	 * @param f
	 */
	public void fileDeleted(IFile f)
	{
		Set<ICompilationUnit> toDiscard = new HashSet<ICompilationUnit>();
		Set<IFile> toDelete = new HashSet<IFile>();

		removeFileFromMaps(f, toDiscard, toDelete);

		for (IFile fileToDelete : toDelete) {
			deletePhysicalFile(fileToDelete);
		}
		for (ICompilationUnit wcToDiscard : toDiscard) {
			_CUHELPER.discardWorkingCopy(wcToDiscard);
		}

	}

	/**
	 * Invoked when a file is generated during a build. The generated file and
	 * intermediate directories will be created if they don't exist. This method takes
	 * file-system locks, and assumes that the calling method has at some point acquired a
	 * workspace-level resource lock.
	 * 
	 * @param parentFile
	 *            the parent of the type being generated
	 * @param typeName
	 *            the dot-separated java type name of the type being generated
	 * @param contents
	 *            the java code contents of the new type .
	 * @param progressMonitor
	 *            a progress monitor. This may be null.
	 * @return - the newly created IFile along with whether it was modified
	 * @throws CoreException
	 */
	public FileGenerationResult generateFileDuringBuild(IFile parentFile, String typeName, String contents,
			IProgressMonitor progressMonitor) throws CoreException
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
			marker.setAttributes(new String[] { IMarker.MESSAGE, IMarker.SEVERITY }, new Object[] { message,
					IMarker.SEVERITY_ERROR });
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
	
				// Mark all newly created folders (but not pre-existing ones) as derived.  
				for (IContainer folder : newFolders) {
					try {
						folder.setDerived(true);
					} catch (CoreException e) {
						AptPlugin.logWarning(e, "Unable to mark generated type folder as derived: " + folder.getName()); //$NON-NLS-1$
						break;
					}
				}
	
				// Save the compilation unit to disk.  How this is done depends on current state.
				saveCompilationUnit(pkgFrag, cuName, contents, progressMonitor);
			}

			// during a batch build, parentFile will be null.
			// Only keep track of ownership in iterative builds
			if (parentFile != null) {
				addBuiltFileToMaps(parentFile, file);
			}

			// Mark the file as derived. Note that certain user actions may have
			// deleted this file before we get here, so if the file doesn't
			// exist, marking it derived throws a ResourceException.
			if (file.exists()) {
				file.setDerived(true);
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
	 * type may not exist on disk. Likewise, the corresponding package directories of
	 * type-name may not exist on disk.
	 * 
	 * TODO: figure out how to create a working copy with a client-specified character set
	 * 
	 * @param parentCompilationUnit the parent compilation unit.
	 * @param typeName the dot-separated java type name for the new type
	 * @param contents the contents of the new type
	 * @return The FileGenerationResult. This will return null if the generated source
	 *         folder is not configured, or if there is some other error during type
	 *         generation.
	 * 
	 */
	public FileGenerationResult generateFileDuringReconcile(ICompilationUnit parentCompilationUnit, String typeName,
			String contents) throws CoreException
	{
		if (!GENERATE_TYPE_DURING_RECONCILE)
			return null;

		IFile parentFile = (IFile) parentCompilationUnit.getResource();
		
		ICompilationUnit workingCopy = getWorkingCopyForGeneratedFile(parentFile, typeName, _CUHELPER);

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
		return _parentToGenFiles.getValues(parent);
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
		return _parentToGenFiles.containsValue(f);
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
		return _parentToGenFiles.containsKey(f);
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
	 * @throws CoreException
	 */
	public void workingCopyDiscarded(ICompilationUnit wc) throws CoreException
	{
		Set<ICompilationUnit> toDiscard = removeWcChildrenFromMaps(wc);
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
		_parentToGenFiles.writeState();
	}

	/**
	 * Add a file dependency at build time. This updates the build dependency map but does
	 * not affect the reconcile-time dependencies.
	 * <p>
	 * This method only affects maps; it does not touch disk or modify working copies.
	 */
	private synchronized void addBuiltFileToMaps(IFile parentFile, IFile generatedFile)
	{
		boolean added = _parentToGenFiles.put(parentFile, generatedFile);
		if (AptPlugin.DEBUG_GFM_MAPS) {
			if (added)
				AptPlugin.trace("build file dependency added: " + parentFile + " -> " + generatedFile); //$NON-NLS-1$//$NON-NLS-2$
			else
				AptPlugin.trace("build file dependency already exists: " + parentFile + " -> " + generatedFile); //$NON-NLS-1$//$NON-NLS-2$
		}
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
	 * @return a list of files which the caller should delete, ie by calling
	 * {@link #deletePhysicalFile(IFile)}.
	 */
	private synchronized Set<IFile> calculateDeletedFiles(
			IFile parentFile, Set<IFile> newlyGeneratedFiles)
	{
		Set<IFile> deleted = new HashSet<IFile>();
		Set<IFile> obsoleteFiles = _parentToGenFiles.getValues(parentFile);
		// spare all the newly generated files
		obsoleteFiles.removeAll(newlyGeneratedFiles);
		for (IFile generatedFile : obsoleteFiles) {
			_parentToGenFiles.remove(parentFile, generatedFile);
			if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace(
					"removed build file dependency: " + parentFile + " -> " + generatedFile); //$NON-NLS-1$ //$NON-NLS-2$
			// If the file is still parented by any other parent, spare it
			if (!_parentToGenFiles.containsValue(generatedFile)) {
				deleted.add(generatedFile);
			}
		}
		assert checkIntegrity();
		return deleted;
	}

	/**
	 * Perform the map manipulations necessary to hide types during reconcile that were
	 * generated during build and thus exist on disk. The basic idea is that the caller
	 * passes in a set of files generated on the most recent reconcile, and then this
	 * method hides all the files that were generated on the previous build and that are
	 * not in the set passed in.
	 * <p>
	 * This method does not touch the disk nor create, modify, or discard working copies;
	 * however, it may call {@link CompilationUnitHelper#createWorkingCopy(IFile)}. 
	 * <p>
	 * This method is atomic with regard to data structure integrity.
	 * 
	 * @param parentFile
	 *            only this parent's generated file will be hidden; generated files with
	 *            more than one parent will be spared.
	 * @param newlyGeneratedFiles
	 *            will be spared from being hidden
	 * @param cuHelper
	 *            may be used to create a new working copy for a generated file
	 * @return a list of working copies which need to have their content set to empty by
	 *         {@link CompilationUnitHelper#updateWorkingCopyContents(String, 
	 *         ICompilationUnit, WorkingCopyOwner)}.
	 */
	private synchronized List<ICompilationUnit> calculateHiddenTypes(IFile parentFile, Set<IFile> newlyGeneratedFiles,
			CompilationUnitHelper cuHelper)
	{
		IPackageFragmentRoot root = _generatedPackageFragmentRoot.get().root;
		List<ICompilationUnit> toSetBlank = new ArrayList<ICompilationUnit>();

		// Hide types that were generated during build and thus exist on disk.
		// Only hide them if they have no other parents.
		Set<IFile> generatedFromBuild = _parentToGenFiles.getValues(parentFile);
		for (IFile generatedFile : generatedFromBuild) {
			// spare types generated in the last round
			if (!newlyGeneratedFiles.contains(generatedFile)) {
				Set<IFile> parentsOfGeneratedFile = _parentToGenFiles.getKeys(generatedFile);
				if (parentsOfGeneratedFile.size() == 1 && parentsOfGeneratedFile.contains(parentFile)) {
					ICompilationUnit workingCopy = _workingCopies.get(generatedFile);
					if (null != workingCopy) {
						// move existing WC from _workingCopies to _hidden
						_workingCopies.remove(generatedFile);
						boolean removed = _parentToGenWorkingCopies.remove(parentFile, workingCopy);
						assert removed : "Working copy found in list but not in dependency map: " + workingCopy.getElementName(); //$NON-NLS-1$
					} else {
						if (AptPlugin.DEBUG_GFM) AptPlugin.trace( 
								"creating blank working copy to hide type: " + generatedFile); //$NON-NLS-1$
						String typeName = getTypeNameForDerivedFile(generatedFile);
						workingCopy = cuHelper.createWorkingCopy(typeName, root);
					}
					if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace(
							"adding working copy to hidden types list: " + generatedFile); //$NON-NLS-1$
					assert workingCopy.isWorkingCopy() : 
						"Attempted to add a non-working copy to hidden types list"; //$NON-NLS-1$
					_hiddenBuiltTypes.put(generatedFile, workingCopy);

					ICompilationUnit wc = workingCopy;
					toSetBlank.add(wc);
				}
			}
		}
		assert checkIntegrity();
		return toSetBlank;
	}

	/**
	 * Prepare a list of working copies which are no longer being generated and can be
	 * discarded.
	 * <p>
	 * This method does not touch the disk and does not create, update, or discard working
	 * copies. This method is atomic with regard to data structure integrity.
	 * 
	 * @param parentFile
	 *            only children generated solely by this parent will be added to the list
	 *            to be discarded.
	 * @param newlyGeneratedFiles
	 *            a list of files which will be preserved; typically these are the files
	 *            that were generated on the most recent reconcile.
	 * 
	 * @return a list of working copies which are no longer in use and which should be
	 *         discarded by calling
	 *         {@link CompilationUnitHelper#discardWorkingCopy(ICompilationUnit)}.
	 */
	private synchronized List<ICompilationUnit> calculateObsoleteWorkingCopies(IFile parentFile,
			Set<IFile> newlyGeneratedFiles)
	{
		// Discard generated types that were never built and thus exist only
		// as in-memory working copies.
		// Only discard them if they have no other parents.
		List<ICompilationUnit> toDiscard = new ArrayList<ICompilationUnit>();
		Set<ICompilationUnit> generatedFromReconcile = _parentToGenWorkingCopies.getValues(parentFile);
		for (ICompilationUnit wc : generatedFromReconcile) {
			// spare types generated in the last round
			IFile generatedFile = (IFile) wc.getResource();
			if (!newlyGeneratedFiles.contains(generatedFile)) {
				Set<IFile> parentsOfGeneratedWC = _parentToGenWorkingCopies.getKeys(wc);
				if (parentsOfGeneratedWC.size() == 1 && parentsOfGeneratedWC.contains(parentFile)) {
					_parentToGenWorkingCopies.remove(parentFile, wc);
					assert !_parentToGenWorkingCopies.containsValue(wc) : "Working copy unexpectedly remains in dependency map: " + //$NON-NLS-1$
					wc.getElementName() + " <- " + _parentToGenWorkingCopies.getKeys(wc); //$NON-NLS-1$
					_workingCopies.remove(generatedFile);
					toDiscard.add(wc);
				}
			}
		}
		assert checkIntegrity();
		return toDiscard;
	}

	/**
	 * Check integrity of data structures.
	 * @return true always, so that it can be called within an assert to turn it off at runtime
	 */
	private synchronized boolean checkIntegrity() throws IllegalStateException
	{
		if (ENABLE_INTEGRITY_CHECKS) {
			// Every working copy in the working copy dependency map should be
			// in the
			// _workingCopies list and should not be in the _hiddenBuiltTypes
			// list.
			for (ICompilationUnit wc : _parentToGenWorkingCopies.getValueSet()) {
				if (!_workingCopies.containsValue(wc)) {
					String s = "Dependency map contains a working copy that is not in the regular list: " + //$NON-NLS-1$
							wc.getElementName();
					AptPlugin.log(new IllegalStateException(s), s);
				}
				if (_hiddenBuiltTypes.containsValue(wc)) {
					String s = "Dependency map contains a working copy that is on the hidden list: " + //$NON-NLS-1$
							wc.getElementName();
					AptPlugin.log(new IllegalStateException(s), s);
				}
			}
			// Every entry in the hidden type list should be a working copy
			for (ICompilationUnit hidden : _hiddenBuiltTypes.values()) {
				if (!hidden.isWorkingCopy()) {
					String s = "Hidden list contains a compilation unit that is not a working copy: " + //$NON-NLS-1$
							hidden.getElementName();
					AptPlugin.log(new IllegalStateException(s), s);
				}
			}
		}
		return true;
	}

	/**
	 * Clear the working copy maps, that is, the reconcile-time dependency information.
	 * Returns a list of working copies that are no longer referenced and should be
	 * discarded.
	 * <p>
	 * This method affects maps only; it does not touch disk nor create, modify, nor
	 * discard any working copies. This method is atomic with respect to data structure
	 * integrity.
	 * 
	 * @return a list of working copies which must be discarded by the caller
	 */
	private synchronized List<ICompilationUnit> clearWorkingCopyMaps()
	{
		int size = _hiddenBuiltTypes.size() + _workingCopies.size();
		List<ICompilationUnit> toDiscard = new ArrayList<ICompilationUnit>(size);
		toDiscard.addAll(_hiddenBuiltTypes.values());
		toDiscard.addAll(_workingCopies.values());
		_workingCopies.clear();
		_hiddenBuiltTypes.clear();
		_parentToGenWorkingCopies.clear();

		if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace( 
				"cleared working copy dependencies"); //$NON-NLS-1$

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
	 * Prepare to discard a working copy, presumably of a generated type.  
	 * Removes the working copy from the maps, removes its generated children
	 * if it had any, and returns a list of working copies which must now
	 * be discarded with {@link CompilationUnitHelper#discardWorkingCopy(ICompilationUnit)}.
	 * <p>
	 * This does not itself touch disk nor create, modify, or discard any working copies.
	 * This is atomic with respect to data structure integrity.
	 * @param wc
	 * @return a list of compilation units to discard, including <code>wc</code> itself.
	 */
	private synchronized Set<ICompilationUnit> discardWorkingCopy(ICompilationUnit wc) {
		Set<ICompilationUnit> toDiscard;
		ICompilationUnit hidingWc = _hiddenBuiltTypes.remove(wc);
		if (null != hidingWc) {
			toDiscard = Collections.singleton(hidingWc);
		}
		else {
			toDiscard = removeWcChildrenFromMaps(wc);
			IFile file = (IFile) wc.getResource();
			_parentToGenWorkingCopies.removeKey(file);
			_workingCopies.remove(wc);
			toDiscard.add(wc);
		}
		assert checkIntegrity();
		return toDiscard;
	}

	/**
	 * Given a typename a.b.c, this will return the IFile for the type name, where the
	 * IFile is in the GENERATED_SOURCE_FOLDER_NAME.
	 * <p>
	 * This does not affect or refer to the dependency maps.
	 */
	private IFile getIFileForTypeName(String typeName)
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
	 * Get the IFolder handles for any additional folders needed to 
	 * contain a type in package <code>pkgName</code> under root
	 * <code>parent</code>.  This does not actually create the folders
	 * on disk, it just gets resource handles.
	 * 
	 * @return a set containing all the newly created folders.
	 */
	private Set<IFolder> computeNewPackageFolders(String pkgName, IFolder parent)
	{
		Set<IFolder> newFolders = new HashSet<IFolder>();
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
	 * given file f, return the typename corresponding to the file.  This assumes
	 * that derived files use java naming rules (i.e., type "a.b.c" will be file 
	 * "a/b/c.java".
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
	 * one cached, use that; if not, create a new one.
	 * This method does not touch disk, nor does it update or discard any working
	 * copies.  However, it may call CompilationUnitHelper to get a new working copy.
	 * <p>
	 * This method is atomic with respect to data structures.
	 * 
	 * @param parentFile the IFile whose processing is causing the new type to be generated
	 * @param typeName the name of the type to be generated
	 * @param cuh the CompilationUnitHelper utility object
	 * @return a working copy ready to be updated with the new type's contents
	 */
	private synchronized ICompilationUnit getWorkingCopyForGeneratedFile(IFile parentFile, String typeName, CompilationUnitHelper cuh)
	{
		IPackageFragmentRoot root = _generatedPackageFragmentRoot.get().root;
		IFile generatedFile = getIFileForTypeName(typeName);
		ICompilationUnit workingCopy;
		
		workingCopy = _hiddenBuiltTypes.get(generatedFile);
		if (null != workingCopy) {
			// file is currently hidden with a blank WC. Move that WC to the regular list.
			if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace(
					"move working copy from hidden to regular list: " + generatedFile); //$NON-NLS-1$
			_hiddenBuiltTypes.remove(generatedFile);
			_workingCopies.put(generatedFile, workingCopy);
		} else {
			workingCopy = _workingCopies.get(generatedFile);
			if (null == workingCopy) {
				// we've not yet created a working copy for this file, so make one now.
				workingCopy = cuh.createWorkingCopy(typeName, root);
				_workingCopies.put(generatedFile, workingCopy);
				if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace( 
						"added new working copy to regular list: " + generatedFile); //$NON-NLS-1$
			} else {
				if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace(
						"obtained existing working copy from regular list: " + workingCopy.getElementName()); //$NON-NLS-1$
			}
		}

		// Add it to the dependency map (a no-op if it's already there)
		boolean added = _parentToGenWorkingCopies.put(parentFile, workingCopy);
		if (AptPlugin.DEBUG_GFM_MAPS) {
			if (added)
				AptPlugin.trace("working copy association added: " + parentFile + " -> " + workingCopy.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
			else
				AptPlugin.trace("working copy association already present: " + parentFile + " -> " + workingCopy.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		assert checkIntegrity();

		return workingCopy;
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
	 * Remove a file from the build-time and reconcile-time dependency maps, and calculate
	 * the consequences of the removal. This is called in response to a file being deleted
	 * by the environment.
	 * <p>
	 * This operation affects the maps only. This operation is atomic with respect to map
	 * integrity. This operation does not touch the disk nor create, update, or discard
	 * any working copies.
	 * 
	 * @param f
	 *            can be a parent, generated, both, or neither.
	 * @param toDiscard
	 *            this set will be populated with a list of working copies that are no
	 *            longer in the maps and must be discarded. This operation must be done by
	 *            the caller without holding any locks.
	 * @param toDelete
	 *            this set will be populated with a list of generated files that are no
	 *            longer relevant and must be deleted. This operation must be done by the
	 *            caller without holding any locks.
	 */
	private synchronized void removeFileFromMaps(IFile f, Set<ICompilationUnit> toDiscard, Set<IFile> toDelete)
	{
		// Is this file the sole parent of files generated during build?
		// If so, add them to the deletion list. Then remove the file from
		// the build dependency list.
		Set<IFile> childFiles = _parentToGenFiles.getValues(f);
		for (IFile childFile : childFiles) {
			Set<IFile> parentFiles = _parentToGenFiles.getKeys(childFile);
			if (parentFiles.size() == 1 && parentFiles.contains(f)) {
				toDelete.add(childFile);
			}
		}
		boolean removed = _parentToGenFiles.removeKey(f);
		if (removed) {
			if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace( 
					"removed parent file from build dependencies: " + f); //$NON-NLS-1$
		}

		// Is this file the sole parent of types generated during reconcile?
		// If so, add them to the discard list and remove them from the working
		// copy list. Then remove the file (and its solely parented children)
		// from the reconcile dependency list.
		Set<ICompilationUnit> childWCs = _parentToGenWorkingCopies.getValues(f);
		for (ICompilationUnit childWC : childWCs) {
			Set<IFile> parentFiles = _parentToGenWorkingCopies.getKeys(childWC);
			if (parentFiles.size() == 1 && parentFiles.contains(f)) {
				toDiscard.add(childWC);
				ICompilationUnit removedWC = _workingCopies.remove(childWC.getResource());
				assert removedWC != null && removedWC.equals(childWC) :
					"Working copy list: get(f).getResource() != f, for wc " +  //$NON-NLS-1$
					childWC.getElementName();
			}
		}
		removed = _parentToGenWorkingCopies.removeKey(f);
		if (removed) {
			if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace( 
					"removed parent file from working copy dependencies: " + f); //$NON-NLS-1$
		}

		// Is this file being hidden by a blank working copy?  If so, remove that.
		ICompilationUnit wc = _hiddenBuiltTypes.remove(f);
		if (null != wc) {
			if (AptPlugin.DEBUG_GFM_MAPS) AptPlugin.trace( 
					"removed working copy from hidden types list: " + f); //$NON-NLS-1$
		}
		if (null != wc) {
			toDiscard.add(wc);
		}

		assert checkIntegrity();
	}

	/**
	 * Remove the generated children of a working copy from the working copy dependency maps and
	 * lists.  Typically invoked when a working copy of a parent file has been discarded by the
	 * editor; in this case we want to remove any generated working copies that it parented.
	 * <p>
	 * This method does not touch disk nor create, modify, or discard working copies.  This
	 * method is atomic with regard to data structure integrity.
	 * 
	 * @param wc a compilation unit that is not necessarily a parent or generated file
	 * @return a list of generated working copies that are no longer referenced and should
	 * be discarded by calling {@link CompilationUnitHelper#discardWorkingCopy(ICompilationUnit)}
	 */
	private synchronized Set<ICompilationUnit> removeWcChildrenFromMaps(ICompilationUnit wc)
	{
		Set<ICompilationUnit> toDiscard = new HashSet<ICompilationUnit>();
		IFile file = (IFile)wc.getResource();
		// remove all the solely parented children
		Set<ICompilationUnit> genWCs = _parentToGenWorkingCopies.getValues(file);
		for (ICompilationUnit genWC : genWCs) {
			if (!_parentToGenWorkingCopies.valueHasOtherKeys(genWC, file)) {
				toDiscard.add(genWC);
				_parentToGenWorkingCopies.remove(file, genWC);
				ICompilationUnit removedWC = _workingCopies.remove(genWC.getResource());
				assert removedWC != null && removedWC.equals(genWC) : 
					"working copy in dependency map should also be in working copies list"; //$NON-NLS-1$
			}
		}
		
		assert checkIntegrity();
		return toDiscard;
	}

	/**
	 * Write <code>contents</code> to disk in the form of a compilation unit named
	 * <code>name</code> under package fragment <code>pkgFrag</code>. The way in
	 * which the write is done depends on whether there is an existing file, and on
	 * whether the compilation unit is a working copy.
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
	 * @param progressMonitor
	 */
	private void saveCompilationUnit(IPackageFragment pkgFrag, final String cuName, String contents,
			IProgressMonitor progressMonitor)
	{
		ICompilationUnit unit = pkgFrag.getCompilationUnit(cuName);
		boolean isWorkingCopy = unit.isWorkingCopy();
		if (isWorkingCopy && unit.getResource().exists()) {
			// If we have a working copy and it has a file, all we
			// need to do is update its contents and commit it.
			_CUHELPER.commitNewContents(unit, contents, progressMonitor);
			if (AptPlugin.DEBUG_GFM) AptPlugin.trace( 
					"Committed existing working copy during build: " + unit.getElementName()); //$NON-NLS-1$
		}
		else {
			if (isWorkingCopy) {
				// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=163906 -
				// commitWorkingCopy() fails if file does not already exist.
				Set<ICompilationUnit> toDiscard = discardWorkingCopy(unit);
				for (ICompilationUnit cuToDiscard : toDiscard) {
					_CUHELPER.discardWorkingCopy(cuToDiscard);
					if (AptPlugin.DEBUG_GFM) AptPlugin.trace( 
							"Discarded working copy during build: " + unit.getElementName()); //$NON-NLS-1$
				}
			}
			try {
				unit = pkgFrag.createCompilationUnit(cuName, contents, true, progressMonitor);
			} catch (JavaModelException e) {
				AptPlugin.log(e, "Unable to create compilation unit on disk: " +  //$NON-NLS-1$
						cuName + " in pkg fragment: " + pkgFrag.getElementName()); //$NON-NLS-1$
			}
			if (AptPlugin.DEBUG_GFM) AptPlugin.trace( 
					"Created compilation unit during build: " + unit.getElementName()); //$NON-NLS-1$
		}
	}

}
