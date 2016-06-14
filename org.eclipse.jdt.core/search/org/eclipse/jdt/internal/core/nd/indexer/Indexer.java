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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
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

public final class Indexer {
	private Nd nd;
	private IWorkspaceRoot root;

	private static Indexer indexer;
	public static boolean DEBUG;
	public static boolean DEBUG_ALLOCATIONS;
	public static boolean DEBUG_TIMING;
	private static final Object mutex = new Object();
	private static final long MS_TO_NS = 1000000;

	/**
	 * Amount of time (milliseconds) unreferenced files are allowed to sit in the index before they are discarded.
	 * Making this too short will cause some operations (classpath modifications, closing/reopening projects, etc.)
	 * to become more expensive. Making this too long will waste space in the database.
	 */
	private static final long GARBAGE_CLEANUP_TIMEOUT = 1000 * 60 * 60 * 24;

	/**
	 * Amount of time (milliseconds) before we update the "used" timestamp on a file in the index. We don't update
	 * the timestamps every update since doing so would be unnecessarily inefficient... but if any of the timestamps
	 * is older than this update period, we refresh it.
	 */
	private static final long USAGE_TIMESTAMP_UPDATE_PERIOD = GARBAGE_CLEANUP_TIMEOUT / 4;

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

	protected void rescan(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

		long startTimeNs = System.nanoTime();
		long currentTimeMs = System.currentTimeMillis();
		if (DEBUG) {
			Package.logInfo("Indexer running rescan"); //$NON-NLS-1$
		}

		// Gather all the IPackageFragmentRoots in the workspace
		List<IJavaElement> unfilteredRoots = getAllIndexableObjectsInWorkspace(subMonitor.split(3));

		int totalRoots = unfilteredRoots.size();
		// Remove all duplicate roots (jars which are referenced by more than one project)
		Map<IPath, List<IJavaElement>> allRoots = removeDuplicatePaths(unfilteredRoots);

		long startGarbageCollectionNs = System.nanoTime();

		// Remove all files in the index which aren't referenced in the workspace
		int gcFiles = cleanGarbage(currentTimeMs, allRoots.keySet(), subMonitor.split(4));

		long startFingerprintTestNs = System.nanoTime();

		Map<IPath, FingerprintTestResult> fingerprints = testFingerprints(allRoots.keySet(), subMonitor.split(7));
		Set<IPath> rootsWithChanges = new HashSet<>(getRootsThatHaveChanged(allRoots.keySet(), fingerprints));

		long startIndexingNs = System.nanoTime();

		int classesIndexed = 0;
		SubMonitor loopMonitor = subMonitor.split(80).setWorkRemaining(rootsWithChanges.size());
		for (IPath next : rootsWithChanges) {
			classesIndexed += rescanArchive(currentTimeMs, next, allRoots.get(next), fingerprints.get(next).getNewFingerprint(),
					loopMonitor.split(1));
		}

		long endIndexingNs = System.nanoTime();

		Map<IPath, List<IJavaElement>> pathsToUpdate = new HashMap<>();

		for (IPath next : allRoots.keySet()) {
			if (!rootsWithChanges.contains(next)) {
				pathsToUpdate.put(next, allRoots.get(next));
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

		fireDelta(rootsWithChanges, subMonitor.split(1));

		long endResourceMappingNs = System.nanoTime();

		long fingerprintTimeMs = (startIndexingNs - startFingerprintTestNs) / MS_TO_NS;
		long locateRootsTimeMs = (startGarbageCollectionNs - startTimeNs) / MS_TO_NS;
		long garbageCollectionMs = (startFingerprintTestNs - startGarbageCollectionNs) / MS_TO_NS;
		long indexingTimeMs = (endIndexingNs - startIndexingNs) / MS_TO_NS;
		long resourceMappingTimeMs = (endResourceMappingNs - endIndexingNs) / MS_TO_NS;

		double averageGcTimeMs = gcFiles == 0 ? 0 : (double)garbageCollectionMs / (double)gcFiles;
		double averageIndexTimeMs = classesIndexed == 0 ? 0 : (double)indexingTimeMs / (double)classesIndexed;
		double averageFingerprintTimeMs = allRoots.size() == 0 ? 0 : (double)fingerprintTimeMs / (double)allRoots.size();
		double averageResourceMappingMs = pathsToUpdate.size() == 0 ? 0 : (double)resourceMappingTimeMs / (double)pathsToUpdate.size();

		if (DEBUG_TIMING) {
			Package.logInfo(
					"Indexing done.\n" //$NON-NLS-1$
					+ "  Located " + totalRoots + " roots in " + locateRootsTimeMs + "ms\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ "  Collected garbage from " + gcFiles + " files in " +  garbageCollectionMs + "ms, average time = " + averageGcTimeMs + "ms\n" //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
					+ "  Tested " + allRoots.size() + " fingerprints in " + fingerprintTimeMs + "ms, average time = " + averageFingerprintTimeMs + "ms\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ "  Indexed " + classesIndexed + " classes in " + indexingTimeMs + "ms, average time = " + averageIndexTimeMs + "ms\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ "  Updated " + pathsToUpdate.size() + " paths in " + resourceMappingTimeMs + "ms, average time = " + averageResourceMappingMs + "ms\n"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
		}

		if (DEBUG_ALLOCATIONS) {
			try (IReader readLock = this.nd.acquireReadLock()) {
				this.nd.getDB().getMemoryStats().printMemoryStats(this.nd.getTypeRegistry());
			}
		}
	}

	private void fireDelta(Set<IPath> rootsWithChanges, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
		IProject[] projects = this.root.getProjects();

		List<IProject> projectsToScan = new ArrayList<>();

		for (IProject next : projects) {
			if (next.isOpen()) {
				projectsToScan.add(next);
			}
		}
		JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
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

							if (rootsWithChanges.contains(location)) {
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

		fireChange(IndexerEvent.createChange(delta));
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

	private int cleanGarbage(long currentTimeMillis, Collection<IPath> allRootLocations, IProgressMonitor monitor) {
		// TODO: lazily clean up unneeded files here... but only do so if we're under heavy space pressure
		// or it's been a long time since the file was last scanned. Being too eager about removing old files
		// means that operations which temporarily cause a file to become unreferenced will run really slowly

		// We should also eagerly clean up any partially-indexed files we discover during the scan. That is,
		// if we discover a file with a timestamp of 0, it indicates that the indexer or all of Eclipse crashed
		// midway through indexing the file. Such garbage should be cleaned up as soon as possible, since it
		// will never be useful.

		JavaIndex index = JavaIndex.getIndex(this.nd);

		int result = 0; 
		HashSet<IPath> paths = new HashSet<>();
		paths.addAll(allRootLocations);
		SubMonitor subMonitor = SubMonitor.convert(monitor, 3);

		List<NdResourceFile> garbage = new ArrayList<>();
		List<NdResourceFile> needsUpdate = new ArrayList<>();

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
						if (timeSinceLastUsed > USAGE_TIMESTAMP_UPDATE_PERIOD) {
							needsUpdate.add(next);
						}
					} else {
						if (timeSinceLastUsed > GARBAGE_CLEANUP_TIMEOUT) {
							garbage.add(next);
						}
					}
				}
			}
		}

		SubMonitor deleteMonitor = subMonitor.split(1).setWorkRemaining(garbage.size());
		for (NdResourceFile next : garbage) {
			this.nd.acquireWriteLock(deleteMonitor.split(1));
			try {
				if (next.isInIndex()) {
					next.delete();
				}
			} finally {
				this.nd.releaseWriteLock();
			}
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

	private Map<IPath, List<IJavaElement>> removeDuplicatePaths(List<IJavaElement> allRoots) {
		Map<IPath, List<IJavaElement>> paths = new HashMap<>();

		HashSet<IPath> workspacePaths = new HashSet<IPath>();
		for (IJavaElement next : allRoots) {
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

	private Map<IPath, FingerprintTestResult> testFingerprints(Collection<IPath> allRoots,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, allRoots.size());
		Map<IPath, FingerprintTestResult> result = new HashMap<>();

		for (IPath next : allRoots) {
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
		int result = addElement(resourceFile, element, subMonitor.split(90));

		// Now update the timestamp and delete all older versions of this resource that exist in the index
		this.nd.acquireWriteLock(subMonitor.split(5));
		try {
			if (resourceFile.isInIndex()) {
				resourceFile.setFingerprint(fingerprint);
				this.nd.markPathAsModified(resourceFile.getLocalFile());
				List<NdResourceFile> resourceFiles = javaIndex.findResourcesWithPath(pathString);

				for (NdResourceFile next : resourceFiles) {
					if (!next.equals(resourceFile)) {
						next.delete();
					}
				}
			}
		} finally {
			this.nd.releaseWriteLock();
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

		queue.add(input);

		while (!queue.isEmpty()) {
			subMonitor.setWorkRemaining(Math.max(queue.size(), 10)).split(1);

			IJavaElement next = queue.removeFirst();

			if (next.getElementType() == IJavaElement.COMPILATION_UNIT
					|| next.getElementType() == IJavaElement.CLASS_FILE) {
				result.add(next);
			} else if (next instanceof IParent) {
				IParent parent = (IParent) next;

				for (IJavaElement child : parent.getChildren()) {
					queue.add(child);
				}
			}
		}
		return result;
	}

	/**
	 * Adds an archive to the index, under the given NdResourceFile.
	 *
	 * @param resourceFile
	 * @param element
	 * @param monitor
	 * @return the number of classes indexed
	 * @throws JavaModelException
	 */
	private int addElement(NdResourceFile resourceFile, IJavaElement element, IProgressMonitor monitor)
			throws JavaModelException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		List<IJavaElement> bindableElements = getBindableElements(element, subMonitor.split(10));
		List<IClassFile> classFiles = getClassFiles(bindableElements);

		subMonitor.setWorkRemaining(classFiles.size());

		int classesIndexed = 0;
		ClassFileToIndexConverter converter = new ClassFileToIndexConverter(resourceFile);
		for (IClassFile next : classFiles) {
			SubMonitor iterationMonitor = subMonitor.split(1).setWorkRemaining(100);

			this.nd.acquireWriteLock(iterationMonitor.split(5));
			try {
				if (!resourceFile.isInIndex()) {
					return classesIndexed;
				}

				IBinaryType binaryType = ClassFileToIndexConverter.getTypeFromClassFile(next, iterationMonitor.split(50));
				converter.addType(binaryType, iterationMonitor.split(45));
				classesIndexed++;
			} catch (CoreException e) {
				Package.log("Unable to index " + next.toString(), e); //$NON-NLS-1$
			} finally {
				this.nd.releaseWriteLock();
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
		List<IJavaElement> allRoots = new ArrayList<>();
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
								allRoots.add(nextRoot);
							} else {
								collectAllClassFiles(allRoots, nextRoot);
							}
						} else {
							collectAllClassFiles(allRoots, nextRoot);
						}
					}
				}
			} catch (CoreException e) {
				Package.log(e);
			}
		}

		collectAllClassFiles(allRoots, resourcesToScan, subMonitor.split(1));
		return allRoots;
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
	private List<IPath> getRootsThatHaveChanged(Collection<IPath> roots,
			Map<IPath, FingerprintTestResult> fingerprints) {
		List<IPath> rootsWithChanges = new ArrayList<>();
		for (IPath next : roots) {
			FingerprintTestResult testResult = fingerprints.get(next);

			if (!testResult.matches()) {
				rootsWithChanges.add(next);
			}
		}
		return rootsWithChanges;
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
}
