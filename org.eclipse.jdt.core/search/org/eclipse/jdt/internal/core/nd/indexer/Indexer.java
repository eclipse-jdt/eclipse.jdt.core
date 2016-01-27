package org.eclipse.jdt.internal.core.nd.indexer;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.java.FileFingerprint;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;
import org.eclipse.jdt.internal.core.nd.java.NdResourceFile;
import org.eclipse.jdt.internal.core.nd.java.FileFingerprint.FingerprintTestResult;

public final class Indexer {
	private Nd pdom;
	private IWorkspaceRoot root;
	
	private static Indexer indexer;
	private static final Object mutex = new Object();
	private static final long MS_TO_NS = 1000000;

	private Job rescanJob = Job.create("Updating index", new ICoreRunnable() {
		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			rescan(monitor);
		}
	});

	public static Indexer getInstance() {
		synchronized (mutex) {
			if (indexer == null) {
				indexer = new Indexer(JavaIndex.getGlobalPDOM(), ResourcesPlugin.getWorkspace().getRoot());
			}
			return indexer;
		}
	}

	protected void rescan(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

		long startTimeNs = System.nanoTime();
		Package.logInfo("Indexer running rescan");

		// Gather all the IPackageFragmentRoots in the workspace
		List<IJavaElement> allRoots = getAllIndexableObjectsInWorkspace(subMonitor.split(3));

		int totalRoots = allRoots.size();
		// Remove all duplicate roots (jars which are referenced by more than one project)
		allRoots = removeDuplicatePaths(allRoots);

		long startGarbageCollectionNs = System.nanoTime();
		
		// Remove all files in the index which aren't referenced in the workspace
		cleanGarbage(allRoots, subMonitor.split(4));

		long startFingerprintTestNs = System.nanoTime();
		
		Map<IJavaElement, FingerprintTestResult> fingerprints = testFingerprints(allRoots, subMonitor.split(7));
		List<IJavaElement> rootsWithChanges = getRootsThatHaveChanged(allRoots, fingerprints);

		long startIndexingNs = System.nanoTime();
		
		int classesIndexed = 0;
		SubMonitor loopMonitor = subMonitor.split(85).setWorkRemaining(rootsWithChanges.size());
		for (IJavaElement next : rootsWithChanges) {
			classesIndexed += rescanArchive(next, fingerprints.get(next).getNewFingerprint(), loopMonitor.split(1));
		}

		long endIndexingNs = System.nanoTime();
		
		long fingerprintTimeMs = (startIndexingNs - startFingerprintTestNs) / MS_TO_NS;
		long locateRootsTimeMs = (startGarbageCollectionNs - startTimeNs) / MS_TO_NS;
		long indexingTimeMs = (endIndexingNs - startIndexingNs) / MS_TO_NS;

		double averageIndexTimeMs = classesIndexed == 0 ? 0 : (double)indexingTimeMs / (double)classesIndexed;
		double averageFingerprintTimeMs = allRoots.size() == 0 ? 0 : (double)fingerprintTimeMs / (double)allRoots.size(); 

		Package.logInfo(
				"Indexing done.\n"
				+ "  Located " + totalRoots + " roots in " + locateRootsTimeMs + "ms\n"
				+ "  Tested " + allRoots.size() + " fingerprints in " + fingerprintTimeMs + "ms, average time = " + averageFingerprintTimeMs + "ms\n"
				+ "  Indexed " + classesIndexed + " classes in " + indexingTimeMs + "ms, average time = " + averageIndexTimeMs + "ms\n");
	}

	private void cleanGarbage(List<IJavaElement> allRoots, IProgressMonitor monitor) {
		// TODO: lazily clean up unneeded files here... but only do so if we're under heavy space pressure
		// or it's been a long time since the file was last scanned. Being too eager about removing old files
		// means that operations which temporarily cause a file to become unreferenced will run really slowly

		// We should also eagerly clean up any partially-indexed files we discover during the scan. That is,
		// if we discover a file with a timestamp of 0, it indicates that the indexer or all of Eclipse crashed
		// midway through indexing the file. Such garbage should be cleaned up as soon as possible, since it
		// will never be useful.
	}

	private List<IJavaElement> removeDuplicatePaths(List<IJavaElement> allRoots) {
		Set<IPath> paths = new HashSet<>();
		List<IJavaElement> result = new ArrayList<>();

		for (IJavaElement next : allRoots) {
			IPath nextPath = getFilesystemPathForRoot(next);

			if (paths.contains(nextPath)) {
				continue;
			}

			paths.add(nextPath);
			result.add(next);
		}

		return result;
	}

	private Map<IJavaElement, FingerprintTestResult> testFingerprints(List<IJavaElement> allRoots,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, allRoots.size());
		Map<IJavaElement, FingerprintTestResult> result = new HashMap<>();

		for (IJavaElement next : allRoots) {
			result.put(next, testForChanges(next, subMonitor.newChild(1)));
		}

		return result;
	}

	/**
	 * Rescans an archive (a jar, zip, or class file on the filesystem). Returns the number of classes indexed.
	 * 
	 * @param element
	 * @param fingerprint
	 * @param monitor
	 * @return
	 * @throws JavaModelException
	 */
	private int rescanArchive(IJavaElement element, FileFingerprint fingerprint, IProgressMonitor monitor)
			throws JavaModelException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

		IPath thePath = getFilesystemPathForRoot(element);
		String pathString = thePath.toString();
		JavaIndex javaIndex = JavaIndex.getIndex(this.pdom);

		File theFile = thePath.toFile();
		if (!(theFile.exists() && theFile.isFile())) {
			Package.log("the file " + pathString + " does not exist", null);
			return 0;
		}

		NdResourceFile resourceFile;

		this.pdom.acquireWriteLock(subMonitor.newChild(5));
		try {
			resourceFile = new NdResourceFile(this.pdom);
			resourceFile.setFilename(pathString);
		} finally {
			this.pdom.releaseWriteLock();
		}

		Package.logInfo("rescanning " + thePath.toString());
		int result = addElement(resourceFile, element, subMonitor.newChild(90));

		// Now update the timestamp and delete all older versions of this resource that exist in the index
		this.pdom.acquireWriteLock(subMonitor.newChild(5));
		try {
			if (resourceFile.isInIndex()) {
				resourceFile.setFingerprint(fingerprint);
				List<NdResourceFile> resourceFiles = javaIndex.getAllResourceFiles(pathString);
	
				for (NdResourceFile next : resourceFiles) {
					if (!next.equals(resourceFile)) {
						next.delete();
					}
				}
			}
		} finally {
			this.pdom.releaseWriteLock();
		}

		return result;
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
	 * Adds an archive to the index, under the given PDOMResourceFile.
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
		List<IJavaElement> bindableElements = getBindableElements(element, subMonitor.newChild(10));
		List<IClassFile> classFiles = getClassFiles(bindableElements);
		IJavaProject javaProject = element.getJavaProject();

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setProject(javaProject);
		parser.setResolveBindings(true);

		subMonitor.setWorkRemaining(classFiles.size());

		int classesIndexed = 0;
		ClassFileToIndexConverter converter = new ClassFileToIndexConverter(resourceFile);
		for (IClassFile next : classFiles) {
			SubMonitor iterationMonitor = subMonitor.split(1).setWorkRemaining(100);

			this.pdom.acquireWriteLock(iterationMonitor.split(5));
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
				this.pdom.releaseWriteLock();
			}
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

	private List<ICompilationUnit> getCompilationUnits(List<IJavaElement> bindableElements) {
		List<ICompilationUnit> result = new ArrayList<>();

		for (IJavaElement next : bindableElements) {
			if (next.getElementType() == IJavaElement.COMPILATION_UNIT) {
				result.add((ICompilationUnit)next);
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
						IPath filesystemPath = getFilesystemPathForRoot(nextRoot);
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

		JavaCore javaCore = JavaCore.getJavaCore();

		ArrayDeque<IResource> resources = new ArrayDeque<>();
		resources.addAll(toScan);

		while (!resources.isEmpty()) {
			subMonitor.setWorkRemaining(Math.max(resources.size(), 10)).split(1);
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
				if (Objects.equals(extension, "class")) {
					IJavaElement element = javaCore.create(file);

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
	private List<IJavaElement> getRootsThatHaveChanged(List<IJavaElement> roots,
			Map<IJavaElement, FingerprintTestResult> fingerprints) {
		List<IJavaElement> rootsWithChanges = new ArrayList<>();
		for (IJavaElement next : roots) {
			FingerprintTestResult testResult = fingerprints.get(next);

			if (!testResult.matches()) {
				rootsWithChanges.add(next);
			}
		}
		return rootsWithChanges;
	}

	private FingerprintTestResult testForChanges(IJavaElement next, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		JavaIndex javaIndex = JavaIndex.getIndex(this.pdom);
		IPath thePath = getFilesystemPathForRoot(next);
		String pathString = thePath.toString();

		// Package.log("Indexer testing: " + pathString, null);

		subMonitor.split(50);
		FileFingerprint fingerprint = FileFingerprint.getEmpty();
		this.pdom.acquireReadLock();
		try {
			NdResourceFile resourceFile = javaIndex.getResourceFile(pathString);

			if (resourceFile != null) {
				fingerprint = resourceFile.getFingerprint();
			}
		} finally {
			this.pdom.releaseReadLock();
		}

		return fingerprint.test(thePath.toFile(), subMonitor.split(50));
	}

	private IPath getFilesystemPathForRoot(IJavaElement next) {
		IResource resource = next.getResource();

		if (resource != null) {
			return resource.getLocation() == null ? new Path("") : resource.getLocation();
		}

		return next.getPath();
	}

	public Indexer(Nd toPopulate, IWorkspaceRoot workspaceRoot) {
		this.pdom = toPopulate;
		this.root = workspaceRoot;
	}

	public void rescanAll() {
		this.rescanJob.schedule();
	}
}
