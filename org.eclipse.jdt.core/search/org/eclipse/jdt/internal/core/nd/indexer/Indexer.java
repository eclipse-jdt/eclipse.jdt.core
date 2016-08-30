package org.eclipse.jdt.internal.core.nd.indexer;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.core.JavaElementDelta;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.nd.IReader;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.java.FileFingerprint;
import org.eclipse.jdt.internal.core.nd.java.FileFingerprint.FingerprintTestResult;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;
import org.eclipse.jdt.internal.core.nd.java.NdResourceFile;
import org.eclipse.jdt.internal.core.nd.java.NdWorkspaceLocation;
import org.eclipse.jdt.internal.core.nd.java.model.BinaryTypeDescriptor;
import org.eclipse.jdt.internal.core.nd.java.model.BinaryTypeFactory;
import org.eclipse.jdt.internal.core.search.processing.IJob;

public final class Indexer {
	private Nd nd;
	private IWorkspaceRoot root;

	private static Indexer indexer;
	public static boolean DEBUG;
	public static boolean DEBUG_ALLOCATIONS;
	public static boolean DEBUG_TIMING;
	/**
	 * Enable this to index the content of output folders, in cases where that content exists and
	 * is up-to-date. This is much faster than indexing source files directly.
	 */
	public static boolean EXPERIMENTAL_INDEX_OUTPUT_FOLDERS;
	private static final Object mutex = new Object();
	private static final long MS_TO_NS = 1000000;

	private Object listenersMutex = new Object();
	/**
	 * Listener list. Copy-on-write. Synchronize on "listenersMutex" before accessing.
	 */
	private Set<Listener> listeners = Collections.newSetFromMap(new WeakHashMap<Listener, Boolean>());

	private Job rescanJob = Job.create(Messages.Indexer_updating_index_job_name, new ICoreRunnable() {
		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			rescan(monitor);
		}
	});

	public static interface Listener {
		void consume(IndexerEvent event);
	}

	public static Indexer getInstance() {
		synchronized (mutex) {
			if (indexer == null) {
				indexer = new Indexer(JavaIndex.getGlobalNd(), ResourcesPlugin.getWorkspace().getRoot());
			}
			return indexer;
		}
	}

	/**
	 * Amount of time (milliseconds) unreferenced files are allowed to sit in the index before they are discarded.
	 * Making this too short will cause some operations (classpath modifications, closing/reopening projects, etc.)
	 * to become more expensive. Making this too long will waste space in the database.
	 * <p>
	 * The value of this is stored in the JDT core preference called "garbageCleanupTimeoutMs". The default value
	 * is 3 days.
	 */
	private static long getGarbageCleanupTimeout() {
		return Platform.getPreferencesService().getLong(JavaCore.PLUGIN_ID, "garbageCleanupTimeoutMs", //$NON-NLS-1$
				1000 * 60 * 60 * 24 * 3,
				null);
	}

	/**
	 * Amount of time (milliseconds) before we update the "used" timestamp on a file in the index. We don't update
	 * the timestamps every update since doing so would be unnecessarily inefficient... but if any of the timestamps
	 * is older than this update period, we refresh it.
	 */
	private static long getUsageTimestampUpdatePeriod() {
		return getGarbageCleanupTimeout() / 4;
	}

	public void rescan(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

		long startTimeNs = System.nanoTime();
		long currentTimeMs = System.currentTimeMillis();
		if (DEBUG) {
			Package.logInfo("Indexer running rescan"); //$NON-NLS-1$
		}

		// Gather all the IPackageFragmentRoots in the workspace
		List<IJavaElement> unfilteredIndexables = getAllIndexableObjectsInWorkspace(subMonitor.split(3));

		int totalIndexables = unfilteredIndexables.size();
		// Remove all duplicate indexables (jars which are referenced by more than one project)
		Map<IPath, List<IJavaElement>> allIndexables = removeDuplicatePaths(unfilteredIndexables);

		long startGarbageCollectionNs = System.nanoTime();

		// Remove all files in the index which aren't referenced in the workspace
		int gcFiles = cleanGarbage(currentTimeMs, allIndexables.keySet(), subMonitor.split(4));

		long startFingerprintTestNs = System.nanoTime();

		Map<IPath, FingerprintTestResult> fingerprints = testFingerprints(allIndexables.keySet(), subMonitor.split(7));
		Set<IPath> indexablesWithChanges = new HashSet<>(getIndexablesThatHaveChanged(allIndexables.keySet(), fingerprints));

		long startIndexingNs = System.nanoTime();

		int classesIndexed = 0;
		SubMonitor loopMonitor = subMonitor.split(80).setWorkRemaining(indexablesWithChanges.size());
		for (IPath next : indexablesWithChanges) {
			classesIndexed += rescanArchive(currentTimeMs, next, allIndexables.get(next), fingerprints.get(next).getNewFingerprint(),
					loopMonitor.split(1));
		}

		long endIndexingNs = System.nanoTime();

		Map<IPath, List<IJavaElement>> pathsToUpdate = new HashMap<>();

		for (IPath next : allIndexables.keySet()) {
			if (!indexablesWithChanges.contains(next)) {
				pathsToUpdate.put(next, allIndexables.get(next));
				continue;
			}
		}

		updateResourceMappings(pathsToUpdate, subMonitor.split(5));

		// Flush the database to disk
		this.nd.acquireWriteLock(subMonitor.split(4));
		try {
			this.nd.getDB().flush();
		} finally {
			this.nd.releaseWriteLock();
		}

		fireDelta(indexablesWithChanges, subMonitor.split(1));

		long endResourceMappingNs = System.nanoTime();

		long fingerprintTimeMs = (startIndexingNs - startFingerprintTestNs) / MS_TO_NS;
		long locateIndexablesTimeMs = (startGarbageCollectionNs - startTimeNs) / MS_TO_NS;
		long garbageCollectionMs = (startFingerprintTestNs - startGarbageCollectionNs) / MS_TO_NS;
		long indexingTimeMs = (endIndexingNs - startIndexingNs) / MS_TO_NS;
		long resourceMappingTimeMs = (endResourceMappingNs - endIndexingNs) / MS_TO_NS;

		double averageGcTimeMs = gcFiles == 0 ? 0 : (double)garbageCollectionMs / (double)gcFiles;
		double averageIndexTimeMs = classesIndexed == 0 ? 0 : (double)indexingTimeMs / (double)classesIndexed;
		double averageFingerprintTimeMs = allIndexables.size() == 0 ? 0 : (double)fingerprintTimeMs / (double)allIndexables.size();
		double averageResourceMappingMs = pathsToUpdate.size() == 0 ? 0 : (double)resourceMappingTimeMs / (double)pathsToUpdate.size();

		if (DEBUG_TIMING) {
			Package.logInfo(
					"Indexing done.\n" //$NON-NLS-1$
					+ "  Located " + totalIndexables + " indexables in " + locateIndexablesTimeMs + "ms\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ "  Collected garbage from " + gcFiles + " files in " +  garbageCollectionMs + "ms, average time = " + averageGcTimeMs + "ms\n" //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
					+ "  Tested " + allIndexables.size() + " fingerprints in " + fingerprintTimeMs + "ms, average time = " + averageFingerprintTimeMs + "ms\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ "  Indexed " + classesIndexed + " classes in " + indexingTimeMs + "ms, average time = " + averageIndexTimeMs + "ms\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ "  Updated " + pathsToUpdate.size() + " paths in " + resourceMappingTimeMs + "ms, average time = " + averageResourceMappingMs + "ms\n"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
		}

		if (DEBUG_ALLOCATIONS) {
			try (IReader readLock = this.nd.acquireReadLock()) {
				this.nd.getDB().reportFreeBlocks();
				this.nd.getDB().getMemoryStats().printMemoryStats(this.nd.getTypeRegistry());
			}
		}
	}

	private void fireDelta(Set<IPath> indexablesWithChanges, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
		IProject[] projects = this.root.getProjects();

		List<IProject> projectsToScan = new ArrayList<>();

		for (IProject next : projects) {
			if (next.isOpen()) {
				projectsToScan.add(next);
			}
		}
		JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
		boolean hasChanges = false;
		JavaElementDelta delta = new JavaElementDelta(model);
		SubMonitor projectLoopMonitor = subMonitor.split(1).setWorkRemaining(projectsToScan.size());
		for (IProject project : projectsToScan) {
			projectLoopMonitor.split(1);
			try {
				if (project.isOpen() && project.isNatureEnabled(JavaCore.NATURE_ID)) {
					IJavaProject javaProject = JavaCore.create(project);

					IPackageFragmentRoot[] roots = javaProject.getAllPackageFragmentRoots();

					for (IPackageFragmentRoot next : roots) {
						if (next.isArchive()) {
							IPath location = JavaIndex.getLocationForElement(next);

							if (indexablesWithChanges.contains(location)) {
								hasChanges = true;
								delta.changed(next,
										IJavaElementDelta.F_CONTENT | IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED);
							}
						}
					}
				}
			} catch (CoreException e) {
				Package.log(e);
			}
		}

		if (hasChanges) {
			fireChange(IndexerEvent.createChange(delta));
		}
	}

	private void updateResourceMappings(Map<IPath, List<IJavaElement>> pathsToUpdate, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, pathsToUpdate.keySet().size());

		JavaIndex index = JavaIndex.getIndex(this.nd);

		for (Entry<IPath, List<IJavaElement>> entry : pathsToUpdate.entrySet()) {
			SubMonitor iterationMonitor = subMonitor.split(1).setWorkRemaining(10);

			this.nd.acquireWriteLock(iterationMonitor.split(1));
			try {
				NdResourceFile resourceFile = index.getResourceFile(entry.getKey().toString().toCharArray());
				if (resourceFile == null) {
					continue;
				}

				attachWorkspaceFilesToResource(entry.getValue(), resourceFile);
			} finally {
				this.nd.releaseWriteLock();
			}

		}
	}

	/**
	 * Clean up unneeded files here, but only do so if it's been a long time since the file was last referenced. Being
	 * too eager about removing old files means that operations which temporarily cause a file to become unreferenced
	 * will run really slowly. also eagerly clean up any partially-indexed files we discover during the scan. That is,
	 * if we discover a file with a timestamp of 0, it indicates that the indexer or all of Eclipse crashed midway
	 * through indexing the file. Such garbage should be cleaned up as soon as possible, since it will never be useful.
	 *
	 * @param currentTimeMillis timestamp of the time at which the indexing operation started
	 * @param allIndexables list of all referenced java roots
	 * @param monitor progress monitor
	 * @return the number of indexables in the index, prior to garbage collection
	 */
	private int cleanGarbage(long currentTimeMillis, Collection<IPath> allIndexables, IProgressMonitor monitor) {
		JavaIndex index = JavaIndex.getIndex(this.nd);

		int result = 0; 
		HashSet<IPath> paths = new HashSet<>();
		paths.addAll(allIndexables);
		SubMonitor subMonitor = SubMonitor.convert(monitor, 3);

		List<NdResourceFile> garbage = new ArrayList<>();
		List<NdResourceFile> needsUpdate = new ArrayList<>();

		long usageTimestampUpdatePeriod = getUsageTimestampUpdatePeriod();
		long garbageCleanupTimeout = getGarbageCleanupTimeout();
		// Build up the list of NdResourceFiles that either need to be garbage collected or
		// have their read timestamps updated.
		try (IReader reader = this.nd.acquireReadLock()) {
			List<NdResourceFile> resourceFiles = index.getAllResourceFiles();

			result = resourceFiles.size();
			SubMonitor testMonitor = subMonitor.split(1).setWorkRemaining(resourceFiles.size());
			for (NdResourceFile next : resourceFiles) {
				testMonitor.split(1);
				if (!next.isDoneIndexing()) {
					garbage.add(next);
				} else {
					IPath nextPath = new Path(next.getLocation().toString());
					long timeLastUsed = next.getTimeLastUsed();
					long timeSinceLastUsed = currentTimeMillis - timeLastUsed;

					if (paths.contains(nextPath)) {
						if (timeSinceLastUsed > usageTimestampUpdatePeriod) {
							needsUpdate.add(next);
						}
					} else {
						if (timeSinceLastUsed > garbageCleanupTimeout) {
							garbage.add(next);
						}
					}
				}
			}
		}

		SubMonitor deleteMonitor = subMonitor.split(1).setWorkRemaining(garbage.size());
		for (NdResourceFile next : garbage) {
			deleteResource(next, deleteMonitor.split(1));
		}

		SubMonitor updateMonitor = subMonitor.split(1).setWorkRemaining(needsUpdate.size());
		for (NdResourceFile next : needsUpdate) {
			this.nd.acquireWriteLock(updateMonitor.split(1));
			try {
				if (next.isInIndex()) {
					next.setTimeLastUsed(currentTimeMillis);
				}
			} finally {
				this.nd.releaseWriteLock();
			}
		}

		return result;
	}

	/**
	 * Performs a non-atomic delete of the given resource file. First, it marks the file as being invalid
	 * (by clearing out its timestamp). Then it deletes the children of the resource file, one child at a time.
	 * Once all the children are deleted, the resource itself is deleted. The result on the database is exactly
	 * the same as if the caller had called toDelete.delete(), but doing it this way ensures that a write lock
	 * will never be held for a nontrivial amount of time.
	 */
	protected void deleteResource(NdResourceFile toDelete, IProgressMonitor monitor) {
		SubMonitor deletionMonitor = SubMonitor.convert(monitor, 10);

		this.nd.acquireWriteLock(deletionMonitor.split(1));
		try {
			if (toDelete.isInIndex()) {
				toDelete.markAsInvalid();
			}
		} finally {
			this.nd.releaseWriteLock();
		}

		for (;;) {
			this.nd.acquireWriteLock(deletionMonitor.split(1));
			try {
				if (!toDelete.isInIndex()) {
					break;
				}
		
				int numChildren = toDelete.getBindingCount();
				deletionMonitor.setWorkRemaining(numChildren + 1);
				if (numChildren == 0) {
					break;
				}

				toDelete.getBinding(numChildren - 1).delete();
			} finally {
				this.nd.releaseWriteLock();
			}
		}

		this.nd.acquireWriteLock(deletionMonitor.split(1));
		try {
			if (toDelete.isInIndex()) {
				toDelete.delete();
			}
		} finally {
			this.nd.releaseWriteLock();
		}
	}

	private Map<IPath, List<IJavaElement>> removeDuplicatePaths(List<IJavaElement> allIndexables) {
		Map<IPath, List<IJavaElement>> paths = new HashMap<>();

		HashSet<IPath> workspacePaths = new HashSet<IPath>();
		for (IJavaElement next : allIndexables) {
			IPath nextPath = JavaIndex.getLocationForElement(next);
			IPath workspacePath = getWorkspacePathForRoot(next);

			List<IJavaElement> value = paths.get(nextPath);

			if (value == null) {
				value = new ArrayList<IJavaElement>();
				paths.put(nextPath, value);
			} else {
				if (workspacePath != null) {
					if (workspacePaths.contains(workspacePath)) {
						continue;
					}
					if (!workspacePath.isEmpty()) {
						Package.logInfo("Found duplicate workspace path for " + workspacePath.toString()); //$NON-NLS-1$
					}
					workspacePaths.add(workspacePath);
				}
			}

			value.add(next);
		}

		return paths;
	}

	private IPath getWorkspacePathForRoot(IJavaElement next) {
		IResource resource = next.getResource();
		if (resource != null) {
			return resource.getFullPath();
		}
		return Path.EMPTY;
	}

	private Map<IPath, FingerprintTestResult> testFingerprints(Collection<IPath> allIndexables,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, allIndexables.size());
		Map<IPath, FingerprintTestResult> result = new HashMap<>();

		for (IPath next : allIndexables) {
			result.put(next, testForChanges(next, subMonitor.split(1)));
		}

		return result;
	}

	/**
	 * Rescans an archive (a jar, zip, or class file on the filesystem). Returns the number of classes indexed.
	 * @throws JavaModelException
	 */
	private int rescanArchive(long currentTimeMillis, IPath thePath, List<IJavaElement> elementsMappingOntoLocation,
			FileFingerprint fingerprint, IProgressMonitor monitor) throws JavaModelException {
		if (elementsMappingOntoLocation.isEmpty()) {
			return 0;
		}

		IJavaElement element = elementsMappingOntoLocation.get(0);
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

		String pathString = thePath.toString();
		JavaIndex javaIndex = JavaIndex.getIndex(this.nd);

		File theFile = thePath.toFile();
		if (!(theFile.exists() && theFile.isFile())) {
			if (DEBUG) {
				Package.log("the file " + pathString + " does not exist", null); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return 0;
		}

		NdResourceFile resourceFile;

		this.nd.acquireWriteLock(subMonitor.split(5));
		try {
			resourceFile = new NdResourceFile(this.nd);
			resourceFile.setTimeLastUsed(currentTimeMillis);
			resourceFile.setLocation(pathString);
			IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) element
					.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
			IPath rootPathString = JavaIndex.getLocationForElement(packageFragmentRoot);
			if (!rootPathString.equals(thePath)) {
				resourceFile.setPackageFragmentRoot(rootPathString.toString().toCharArray());
			}
			attachWorkspaceFilesToResource(elementsMappingOntoLocation, resourceFile);
		} finally {
			this.nd.releaseWriteLock();
		}

		if (DEBUG) {
			Package.logInfo("rescanning " + thePath.toString()); //$NON-NLS-1$
		}
		int result;
		try {
			result = addElement(resourceFile, element, subMonitor.split(50));
		} catch (JavaModelException e) {
			if (DEBUG) {
				Package.log("the file " + pathString + " cannot be indexed due to a recoverable error", null); //$NON-NLS-1$ //$NON-NLS-2$
			}
			// If this file can't be indexed due to a recoverable error, delete the NdResourceFile entry for it.
			this.nd.acquireWriteLock(subMonitor.split(5));
			try {
				if (resourceFile.isInIndex()) {
					resourceFile.delete();
				}
			} finally {
				this.nd.releaseWriteLock();
			}
			return 0;
		} catch (RuntimeException e) {
			if (DEBUG) {
				Package.log("A RuntimeException occurred while indexing " + pathString, e); //$NON-NLS-1$
			}
			throw e;
		}

		List<NdResourceFile> allResourcesWithThisPath = Collections.emptyList();
		// Now update the timestamp and delete all older versions of this resource that exist in the index
		this.nd.acquireWriteLock(subMonitor.split(1));
		try {
			if (resourceFile.isInIndex()) {
				resourceFile.setFingerprint(fingerprint);
				allResourcesWithThisPath = javaIndex.findResourcesWithPath(pathString);
			}
		} finally {
			this.nd.releaseWriteLock();
		}

		SubMonitor deletionMonitor = subMonitor.split(40).setWorkRemaining(allResourcesWithThisPath.size() - 1);
		for (NdResourceFile next : allResourcesWithThisPath) {
			if (!next.equals(resourceFile)) {
				deleteResource(next, deletionMonitor.split(1));
			}
		}

		return result;
	}

	private void attachWorkspaceFilesToResource(List<IJavaElement> elementsMappingOntoLocation,
			NdResourceFile resourceFile) {
		for (IJavaElement next : elementsMappingOntoLocation) {
			IResource nextResource = next.getResource();
			if (nextResource != null) {
				new NdWorkspaceLocation(this.nd, resourceFile,
						nextResource.getFullPath().toString().toCharArray());
			}
		}
	}

	/**
	 * Returns the set of IClassFile and ICompilationUnits contained within the given IJavaElement
	 *
	 * @throws JavaModelException
	 */
	private List<IJavaElement> getBindableElements(IJavaElement input, IProgressMonitor monitor)
			throws JavaModelException {
		List<IJavaElement> result = new ArrayList<>();
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		ArrayDeque<IJavaElement> queue = new ArrayDeque<IJavaElement>();

		enqueue(result, queue, input);

		while (!queue.isEmpty()) {
			subMonitor.setWorkRemaining(Math.max(queue.size(), 10)).split(1);

			IJavaElement next = queue.removeFirst();

			enqueue(result, queue, next);
		}
		return result;
	}

	/**
	 * If toEnqueue has children, they are enqueued in the given queue. If it is directly indexable,
	 * it is added to the result.
	 */
	private void enqueue(List<IJavaElement> result, ArrayDeque<IJavaElement> queue,
			IJavaElement toEnqueue) throws JavaModelException {
		if (toEnqueue.getElementType() == IJavaElement.COMPILATION_UNIT
				|| toEnqueue.getElementType() == IJavaElement.CLASS_FILE) {
			result.add(toEnqueue);
		} else if (toEnqueue instanceof IParent) {
			IParent parent = (IParent) toEnqueue;

			for (IJavaElement child : parent.getChildren()) {
				queue.add(child);
			}

			// We need to call isInvalidArchive after the call to getChildren, since the invalid state
			// is only initialized in the call to getChildren.
			if (!JavaModelManager.getJavaModelManager().getArchiveValidity(toEnqueue.getPath()).isValid()) {
				throw new JavaModelException(new RuntimeException(), IJavaModelStatusConstants.IO_EXCEPTION);
			}
		}
	}

	/**
	 * Adds an archive to the index, under the given NdResourceFile.
	 */
	private int addElement(NdResourceFile resourceFile, IJavaElement element, IProgressMonitor monitor)
			throws JavaModelException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		List<IJavaElement> bindableElements = getBindableElements(element, subMonitor.split(10));
		List<IClassFile> classFiles = getClassFiles(bindableElements);

		if (DEBUG && classFiles.isEmpty()) {
			Package.logInfo("The path " + element.getPath() + " contained no class files"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		subMonitor.setWorkRemaining(classFiles.size());

		int classesIndexed = 0;
		ClassFileToIndexConverter converter = new ClassFileToIndexConverter(resourceFile);
		for (IClassFile next : classFiles) {
			SubMonitor iterationMonitor = subMonitor.split(1).setWorkRemaining(100);

			try {
				BinaryTypeDescriptor descriptor = BinaryTypeFactory.createDescriptor(next);
				ClassFileReader binaryType = BinaryTypeFactory.rawReadType(descriptor, true);

				this.nd.acquireWriteLock(iterationMonitor.split(5));
				try {
					if (!resourceFile.isInIndex()) {
						return classesIndexed;
					}

					converter.addType(binaryType, descriptor.fieldDescriptor, iterationMonitor.split(45));
					classesIndexed++;
				} finally {
					this.nd.releaseWriteLock();
				}
			} catch (CoreException | ClassFormatException e) {
				Package.log("Unable to index " + next.toString(), e); //$NON-NLS-1$
			}

//			if (ENABLE_SELF_TEST) {
//				IndexTester.testType(binaryType, new IndexBinaryType(ReferenceUtil.createTypeRef(type)));
//			}
		}

		return classesIndexed;
	}

	private List<IClassFile> getClassFiles(List<IJavaElement> bindableElements) {
		List<IClassFile> result = new ArrayList<>();

		for (IJavaElement next : bindableElements) {
			if (next.getElementType() == IJavaElement.CLASS_FILE) {
				result.add((IClassFile)next);
			}
		}
		return result;
	}

	private List<IJavaElement> getAllIndexableObjectsInWorkspace(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		List<IJavaElement> allIndexables = new ArrayList<>();
		IProject[] projects = this.root.getProjects();

		List<IProject> projectsToScan = new ArrayList<>();

		for (IProject next : projects) {
			if (next.isOpen()) {
				projectsToScan.add(next);
			}
		}

		Set<IPath> scannedPaths = new HashSet<>();
		Set<IResource> resourcesToScan = new HashSet<>();
		SubMonitor projectLoopMonitor = subMonitor.split(1).setWorkRemaining(projectsToScan.size());
		for (IProject project : projectsToScan) {
			SubMonitor iterationMonitor = projectLoopMonitor.split(1);
			try {
				if (project.isOpen() && project.isNatureEnabled(JavaCore.NATURE_ID)) {
					IJavaProject javaProject = JavaCore.create(project);

					IClasspathEntry[] entries = javaProject.getRawClasspath();

					if (EXPERIMENTAL_INDEX_OUTPUT_FOLDERS) {
						IPath defaultOutputLocation = javaProject.getOutputLocation();
						for (IClasspathEntry next : entries) {
							IPath nextOutputLocation = next.getOutputLocation();
	
							if (nextOutputLocation == null) {
								nextOutputLocation = defaultOutputLocation;
							}
	
							IResource resource = this.root.findMember(nextOutputLocation);
							if (resource != null) {
								resourcesToScan.add(resource);
							}
						}
					}

					IPackageFragmentRoot[] projectRoots = javaProject.getAllPackageFragmentRoots();
					SubMonitor rootLoopMonitor = iterationMonitor.setWorkRemaining(projectRoots.length);
					for (IPackageFragmentRoot nextRoot : projectRoots) {
						rootLoopMonitor.split(1);
						if (!nextRoot.exists()) {
							continue;
						}
						IPath filesystemPath = JavaIndex.getLocationForElement(nextRoot);
						if (scannedPaths.contains(filesystemPath)) {
							continue;
						}
						scannedPaths.add(filesystemPath);
						if (nextRoot.getKind() == IPackageFragmentRoot.K_BINARY) {
							if (nextRoot.isArchive()) {
								allIndexables.add(nextRoot);
							} else {
								collectAllClassFiles(allIndexables, nextRoot);
							}
						} else {
							collectAllClassFiles(allIndexables, nextRoot);
						}
					}
				}
			} catch (CoreException e) {
				Package.log(e);
			}
		}

		collectAllClassFiles(allIndexables, resourcesToScan, subMonitor.split(1));
		return allIndexables;
	}

	private void collectAllClassFiles(List<? super IClassFile> result, Collection<? extends IResource> toScan,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor);

		ArrayDeque<IResource> resources = new ArrayDeque<>();
		resources.addAll(toScan);

		while (!resources.isEmpty()) {
			subMonitor.setWorkRemaining(Math.max(resources.size(), 3000)).split(1);
			IResource next = resources.removeFirst();

			if (next instanceof IContainer) {
				IContainer container = (IContainer)next;

				try {
					for (IResource nextChild : container.members()) {
						resources.addLast(nextChild);
					}
				} catch (CoreException e) {
					// If an error occurs in one resource, skip it and move on to the next
					Package.log(e);
				}
			} else if (next instanceof IFile) {
				IFile file = (IFile) next;

				String extension = file.getFileExtension();
				if (Objects.equals(extension, "class")) { //$NON-NLS-1$
					IJavaElement element = JavaCore.create(file);

					if (element instanceof IClassFile) {
						result.add((IClassFile)element);
					}
				}
			}
		}
	}

	private void collectAllClassFiles(List<? super IClassFile> result, IParent nextRoot) throws CoreException {
		for (IJavaElement child : nextRoot.getChildren()) {
			try {
				int type = child.getElementType();
				if (!child.exists()) {
					continue;
				}
				if (type == IJavaElement.COMPILATION_UNIT) {
					continue;
				}

				if (type == IJavaElement.CLASS_FILE) {
					result.add((IClassFile)child);
				} else if (child instanceof IParent) {
					IParent parent = (IParent) child;

					collectAllClassFiles(result, parent);
				}
			} catch (CoreException e) {
				// Log exceptions, then continue with the next child
				Package.log(e);
			}
		}
	}

	/**
	 * Given a list of fragment roots, returns the subset of roots that have changed since the last time they were
	 * indexed.
	 */
	private List<IPath> getIndexablesThatHaveChanged(Collection<IPath> indexables,
			Map<IPath, FingerprintTestResult> fingerprints) {
		List<IPath> indexablesWithChanges = new ArrayList<>();
		for (IPath next : indexables) {
			FingerprintTestResult testResult = fingerprints.get(next);

			if (!testResult.matches()) {
				indexablesWithChanges.add(next);
			}
		}
		return indexablesWithChanges;
	}

	private FingerprintTestResult testForChanges(IPath thePath, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		JavaIndex javaIndex = JavaIndex.getIndex(this.nd);
		String pathString = thePath.toString();

		// Package.log("Indexer testing: " + pathString, null);

		subMonitor.split(50);
		FileFingerprint fingerprint = FileFingerprint.getEmpty();
		this.nd.acquireReadLock();
		try {
			NdResourceFile resourceFile = javaIndex.getResourceFile(pathString.toCharArray());

			if (resourceFile != null) {
				fingerprint = resourceFile.getFingerprint();
			}
		} finally {
			this.nd.releaseReadLock();
		}

		return fingerprint.test(thePath, subMonitor.split(50));
	}

	public Indexer(Nd toPopulate, IWorkspaceRoot workspaceRoot) {
		this.nd = toPopulate;
		this.root = workspaceRoot;
	}

	public void rescanAll() {
		if (DEBUG) {
			Package.logInfo("Scheduling rescanAll now"); //$NON-NLS-1$
		}
		this.rescanJob.schedule();
	}

	/**
	 * Adds the given listener. It will be notified when Nd changes. No strong references
	 * will be retained to the listener.
	 */
	public void addListener(Listener newListener) {
		synchronized (this.listenersMutex) {
			Set<Listener> oldListeners = this.listeners;
			this.listeners = Collections.newSetFromMap(new WeakHashMap<Listener, Boolean>());
			this.listeners.addAll(oldListeners);
			this.listeners.add(newListener);
		}
	}

	public void removeListener(Listener oldListener) {
		synchronized (this.listenersMutex) {
			if (!this.listeners.contains(oldListener)) {
				return;
			}
			Set<Listener> oldListeners = this.listeners;
			this.listeners = Collections.newSetFromMap(new WeakHashMap<Listener, Boolean>());
			this.listeners.addAll(oldListeners);
			this.listeners.remove(oldListener);
		}
	}

	private void fireChange(IndexerEvent event) {
		Set<Listener> localListeners;
		synchronized (this.listenersMutex) {
			localListeners = this.listeners;
		}

		for (Listener next : localListeners) {
			next.consume(event);
		}
	}

	public void waitForIndex(int waitingPolicy, IProgressMonitor monitor) {
		switch (waitingPolicy) {
			case IJob.ForceImmediate: {
				break;
			}
			case IJob.CancelIfNotReady: {
				if (this.rescanJob.getState() != Job.NONE) {
					throw new OperationCanceledException();
				}
				break;
			}
			case IJob.WaitUntilReady: {
				try {
					this.rescanJob.join(0, monitor);
				} catch (InterruptedException e) {
					throw new OperationCanceledException();
				}
				break;
			}
		}
	}
}
