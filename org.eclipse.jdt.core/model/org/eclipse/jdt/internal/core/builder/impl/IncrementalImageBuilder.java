package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.core.builder.IType;
import org.eclipse.jdt.internal.core.builder.NotPresentException;
import org.eclipse.jdt.internal.core.lookup.ReferenceInfo;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.core.*;

import java.util.*;

/**
 * The incremental image builder
 */
public class IncrementalImageBuilder extends AbstractImageBuilder {
	protected IProject fNewProject;
	protected IImageContext fImageContext;

	/**
	 * The source deltas between old and new workspaces
	 * Maps from IProject to IResourceDelta for the project
	 */
	protected Hashtable fSourceDeltas;

	/**
	 * The image delta between old and new states
	 */
	protected IDelta fImageDelta = null;

	/**
	 * The image context of the last call to getImageDelta()
	 */
	protected IImageContext fContextOfLastDelta = null;

	/**
	 * Vector of IPath of source packages or zip files 
	 * that have been added since old state.
	 * The path is relative to new state.
	 */
	protected Vector fAddedPkgOrZips;

	/**
	 * Vector of IPath of source packages or zip files 
	 * that have been removed since old state.
	 * The path is relative of old state.
	 */
	protected Vector fRemovedPkgOrZips;

	/**
	 * Vector of ResourceDeltas representing changed source packages or zip files 
	 * since old state.
	 */
	protected Vector fChangedPkgOrZips;

	/**
	 * Vector of IPackage handles of builder packages 
	 * that have been added since old state.
	 */
	protected Vector fAddedPackageHandles;

	/**
	 * Vector of IPackage handles of builder packages 
	 * that have been removed since old state.
	 */
	protected Vector fRemovedPackageHandles;

	/**
	 * Vector of IPackage handles of builder packages 
	 * that have changes since old state.
	 */
	protected Vector fChangedPackageHandles;

	/**
	 * Set of affected IPackage: added, removed, directly changed or indirectly changed.
	 */
	protected Hashtable fAffectedPackages;

	/**
	 * Vector of SourceEntry representing added classes since old state.
	 */
	protected Vector fAddedClasses;

	/**
	 * Vector of SourceEntry representing removed classes since old state.
	 */
	protected Vector fRemovedClasses;

	/**
	 * Vector of SourceEntry representing changed classes since old state.
	 */
	protected Vector fChangedClasses;

	/**
	 * Vector of IPaths representing changed zips since old state.
	 */
	protected Vector fChangedZips;

	/**
	 * Table of IType -> BuilderType for types that are being compiled,
	 * so that old structure can be compared to new structure during
	 * indictment processing
	 */
	protected Hashtable fBuilderTypeTable;
/**
 * Creates a new incremental image builder on the given new workspace.
 * The builder will build all classes that have changed since the old state,
 * within the given image context.
 */
protected IncrementalImageBuilder(StateImpl oldState, IProject newProject, IImageContext context) {
	fDC = (JavaDevelopmentContextImpl) oldState.getDevelopmentContext();
	fOldState = oldState;
	fNewProject = newProject;
	fImageContext = context;
	fBuilderTypeTable = new Hashtable(11);
}
/**
 * Given an element delta for a changed package, add the package elements
 * for changing elements to the table, keyed by package handle and file name.
 * Added and removed elements are ignored.
 */
protected void addChangedFileNamesFromChangedPackage(IResourceDelta pkgDelta, Hashtable table) {
	IPackage[] pkgs = fNewState.getPathMap().packageHandlesFromPath(pkgDelta.getFullPath());
	for (int p = 0; p < pkgs.length; ++p) {
		IPackage pkg = pkgs[p];
		Hashtable pkgTable = (Hashtable) table.get(pkg);
		if (pkgTable == null) {
			pkgTable = new Hashtable(11);
			table.put(pkg, pkgTable);
		}
		IResourceDelta[] elementDeltas = pkgDelta.getAffectedChildren(IResourceDelta.CHANGED);
		for (int i = 0; i < elementDeltas.length; ++i) {
			IResourceDelta elementDelta = elementDeltas[i];
			// Only add if the contents are changing.
			if ((elementDelta.getFlags() & IResourceDelta.CONTENT) != 0) {
				IPath path = elementDelta.getFullPath();
				String extension = path.getFileExtension();
				if (extension != null) {
					if (extension.equalsIgnoreCase("java"/*nonNLS*/) || extension.equalsIgnoreCase("class"/*nonNLS*/)) {
						SourceEntry entry = new SourceEntry(path, null, null);
						PackageElement element = new PackageElement(pkg, entry);
						pkgTable.put(entry.getFileName(), element);
					}
				}
			}
		}
	}
}
/**
 * Adds the new classes for this build to the state.
 */
protected void addNewClasses() {
	for (Enumeration e = fAddedClasses.elements(); e.hasMoreElements();) {
		addSourceElement((SourceEntry) e.nextElement());
	}
}
/**
 * Adds the given source element to the new state's tables
 * and dependency graph.
 */
protected void addSourceElement(SourceEntry newEntry) {
	if (newEntry.isSource()) {
		PackageElement element = fNewState.packageElementFromSourceEntry(newEntry);
		IPackage pkg = element.getPackage();

		/* the addition must be in a package which is in the class path */
		Assert.isTrue(fNewState.getPackageMap().containsPackage(pkg));
		DependencyGraph graph = fNewState.getInternalDependencyGraph();
		graph.add(element);
		fWorkQueue.add(element);
	}
}
/**
 * Applies the deltas to the old state to build the new state.  
 * The old state and new state have been set.
 * The new state knows its workspace and build context.
 * This is the method that actually does the incremental build
 */
public void applySourceDelta(Hashtable deltas) {
	fNotifier = new BuildNotifier(fDC, false);
	fNotifier.begin();
	fNotifier.subTask(Util.bind("build.preparingBuild"/*nonNLS*/));
	fSourceDeltas = deltas;
	fNewState = fOldState.copy(fNewProject, fImageContext);

	// options might have changed since last builder run, thus refresh them
	fCompilerOptions = JavaModelManager.convertConfigurableOptions(JavaCore.getOptions());
	fNewState.setCompilerOptions(fCompilerOptions);
	
	try {
		/* find out what has changed at the package level */
		fNotifier.subTask(Util.bind("build.analyzingPackage"/*nonNLS*/));
		computeAllPackages();
		checkCancel();

		/* update the package map */
		updatePackageMap();
		fNewState.canonicalizeBuildContext();
		fNotifier.updateProgressDelta(0.05f);
		checkCancel();

		/* Update the source element table and namespace table for the removed and changed packages.
		 * The tables are simply deleted.  They will be rebuilt lazily for changed packages. */
		for (Enumeration e = fRemovedPackageHandles.elements(); e.hasMoreElements();) {
			IPackage pkgHandle = (IPackage) e.nextElement();
			fNewState.getSourceElementTable().removePackage(pkgHandle);
		}
		recomputeSourceEntriesForChangedPackages();
		checkCancel();
		fWorkQueue = new WorkQueue();

		/* rebuild the namespaces and issue indictments for changes */
		computeNamespaceChanges();
		// 1G220B5 - force compilation of all their dependents as well - only one level deeper
		for (Enumeration e = fWorkQueue.getElementsToCompile().elements(); e.hasMoreElements();) {
			markDependentsAsNeedingCompile(e.nextElement());
		}
		
		/* find out what has changed at the package element level */
		fNotifier.subTask(Util.bind("build.analyzingSources"/*nonNLS*/));
		computeAllClasses();
		checkCancel();

		/* All dependents of changed zips will need compiling */
		markDependentsOfChangedZips();

		/* remove old classes and get affected JCUs */
		removeOldClasses();
		checkCancel();

		/* flag changed classes and get compilation units to compile */
		updateChangedClasses();
		checkCancel();

		/* adding new classes might hide (equivalent to delete) old classes */
		addNewClasses();
		checkCancel();
		
		float amountPerIteration = 0.60f; // Approximation of n + (n/4) + (n/16) + ... = 0.85

		/* keep compiling until there is nothing left to compile */
		Vector vToCompile = fWorkQueue.getElementsToCompile();
		while (vToCompile.size() != 0) {
			fNotifier.setProgressPerCompilationUnit(amountPerIteration / vToCompile.size());
			compile(vToCompile);
			vToCompile = fWorkQueue.getElementsToCompile();
			amountPerIteration *= 0.25f;
		}

		//		not using PrincipalStructureByPackageTable
		//		propagatePrincipalStructureByPackageTable();

		// Force all in build context
		/*
		Don't force -- we're not doing lazy builds.
		
		if (fAddedPackageHandles.size() > 0 || fChangedPackageHandles.size() > 0) {
		for (int i = 0; i < fAddedPackageHandles.size(); ++i) {
		IPackage pkg = (IPackage) fAddedPackageHandles.elementAt(i);
		maybeForce(pkg);
		}
		for (int i = 0; i < fChangedPackageHandles.size(); ++i) {
		IPackage pkg = (IPackage) fChangedPackageHandles.elementAt(i);
		maybeForce(pkg);
		}
		}		
		*/
		/* Update resources in binary output */
		IResourceDelta projectDelta = (IResourceDelta) deltas.get(fNewProject);
		if (projectDelta != null) {
			ProjectResourceCopier copier = new ProjectResourceCopier(fNewState.getJavaProject(), fDC, fNotifier, 0.10f);
			copier.updateAffectedResources(projectDelta);
		}
		
		/* Removals and recompilations can leave unused namespace nodes in the 
		 * dependency graph.  Clean them up. */
		cleanupUnusedNamespaceNodes();
		checkCancel();
		
		/* Copy resource to binary output */
		//copyResources(projectDelta, 0.05f);
		
		fNotifier.done();
	} finally {
		cleanUp();
	}
}
/**
 * Applies the delta to the old state
 * to build the new state.  The old state and new state have been set.
 * The new state knows its workspace and build context.
 * This is the method that actually does the incremental build
 */
public void applySourceDelta(IResourceDelta projectDelta) {
	Hashtable deltas = new Hashtable(11);
	deltas.put(fNewProject, projectDelta);
	applySourceDelta(deltas);
}
/**
 * The given source element has changed and will be compiled.
 * Flag the node in the dependency graph, and store all compilation
 * units that will need compiling as a result of the change. 
 */
protected void changedSourceElement(SourceEntry newEntry) {
	PackageElement element = fNewState.packageElementFromSourceEntry(newEntry);
	if (element.isSource()) {
		fWorkQueue.add(element);
	}

	/* remove problems for this source entry */
	SourceEntry oldEntry = fOldState.getSourceEntry(element);
	fNewState.getProblemReporter().removeProblems(oldEntry);
}
/**
 * Since the image builder is given as a result, let go of
 * any unneeded structures.
 */
protected void cleanUp() {
	super.cleanUp();
	// Don't clear package level information because it's needed to compute the image delta.
	fSourceDeltas = null;
	fAddedClasses = fRemovedClasses = fChangedClasses = null;
}
/**
 * Removals and recompilations can leave unused namespace nodes in the 
 * dependency graph.  Clean them up. 
 */
protected void cleanupUnusedNamespaceNodes() {
	if (fRemovedClasses.size() > 0 || fChangedClasses.size() > 0) {
		PackageMap packageMap = fNewState.getPackageMap();
		DependencyGraph graph = fNewState.getInternalDependencyGraph();
		Vector unused = graph.getUnusedNamespaceNodes();
		for (Enumeration e = unused.elements(); e.hasMoreElements();) {
			NamespaceNode node = (NamespaceNode) e.nextElement();
			IPackage pkg = node.getPackage();
			if (!packageMap.containsPackage(pkg)) {
				graph.removePackage(pkg);
			}
		}
	}
}
/**
 * Compare the visibility and gender of the two types.
 * Returns true if equal, false if not.
 */
protected boolean compareVisibilityAndGender(TypeStructureEntry tsEntry, org.eclipse.jdt.core.IType type) throws JavaModelException {
	try {
		/* This relies on visibility bits being the same between the image builder and the java model. */
		IType oldType = (IType) tsEntry.getType().inState(fNewState);
		final int visVlags = IConstants.AccPublic | IConstants.AccPrivate | IConstants.AccProtected;
		int oldVis = oldType.getModifiers() & visVlags;
		int newVis = type.getFlags() & visVlags;
		return oldVis == newVis && oldType.isInterface() == type.isInterface();
	}
	catch (NotPresentException e) {
		// Old state may be missing.  See 1FVQGL1: ITPJCORE:WINNT - SEVERE - Error saving java file
		return false;
	}
}
/**
 * A unit is being (re)compiled.  Save any previous type structure.
 */
protected void compiling(CompilerCompilationUnit unit) {

	/* Save old binaries if they exist */
	SourceEntry sEntry = unit.getSourceEntry();
	PackageElement element = fNewState.packageElementFromSourceEntry(sEntry);
	if (fOldState.getSourceEntry(element) != null) {
		saveBinaryTypes(element);
	}
}
/**
 * Computes the added, removed, changed classes for this incremental build.
 * Vectors contain SourceEntry objects for each source element being added, 
 * removed, or changed.
 *
 * Takes into account package fragments, and only yields elements which are visible
 *
 * It's important to do this computation using the computed namespaces rather than
 * directly from the source element tables, since types may be removed during namespace
 * computation.
 */
protected void computeAllClasses() {
	fAddedClasses = new Vector();
	fRemovedClasses = new Vector();
	fChangedClasses = new Vector();
	fChangedZips = new Vector(1);

	/* do for each added builder package */
	for (Enumeration e = fAddedPackageHandles.elements(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		SourceEntry[] entries = fNewState.getSourceEntries(pkg);
		if (entries != null) {
			for (int i = 0; i < entries.length; ++i) {
				fAddedClasses.addElement(entries[i]);
			}
		}
	}

	/* do for each removed builder package */
	for (Enumeration e = fRemovedPackageHandles.elements(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		// Don't force the package's table.
		// If the package's table was not forced in the old state,
		// there should be no work to do for its classes.
		if (fOldState.getSourceElementTable().containsPackage(pkg)) {
			SourceEntry[] entries = fOldState.getSourceEntries(pkg);
			if (entries != null) {
				for (int i = 0; i < entries.length; ++i) {
					fRemovedClasses.addElement(entries[i]);
				}
			}
		}
	}

	/* build table of changed package elements, keyed by package */
	Hashtable changeTable = new Hashtable(fChangedPkgOrZips.size() * 2 + 1);
	for (Enumeration e = fChangedPkgOrZips.elements(); e.hasMoreElements();) {
		IResourceDelta changedPkgOrZip = (IResourceDelta) e.nextElement();
		IPath path = changedPkgOrZip.getFullPath();

		// ask the state if it is a ZIP file only if it is present in this state
		if (fNewState.isZipElement(path)) {
			/**
			 * Don't do any finer grained change calculation,
			 * all dependents of the zip and its namespaces
			 * will be recompiled
			 */
			fChangedZips.addElement(path);
		} else {
			addChangedFileNamesFromChangedPackage(changedPkgOrZip, changeTable);
		}
	}

	/* do for each changed builder package */
	for (Enumeration e = fChangedPackageHandles.elements(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		Hashtable changesForPkg = (Hashtable) changeTable.get(pkg);
		Hashtable fileNames = new Hashtable();
		SourceEntry[] oldEntries = fOldState.getSourceEntries(pkg);
		if (oldEntries != null) {
			for (int i = 0; i < oldEntries.length; ++i) {
				SourceEntry oldEntry = oldEntries[i];
				fileNames.put(oldEntry.getFileName(), oldEntry);
			}
		}
		SourceEntry[] newEntries = fNewState.getSourceEntries(pkg);
		if (newEntries != null) {
			for (int i = 0; i < newEntries.length; ++i) {
				SourceEntry newEntry = newEntries[i];
				String fileName = newEntry.getFileName();
				SourceEntry oldEntry = (SourceEntry) fileNames.remove(fileName);
				if (oldEntry != null) {
					// Present in old and new.  Has it changed?
					if (!newEntry.equals(oldEntry)) {
						// It has changed path, so treat it as a removal and an addition
						fRemovedClasses.addElement(oldEntry);
						fAddedClasses.addElement(newEntry);
					}
					else if (changesForPkg != null && changesForPkg.containsKey(fileName)) {
						fChangedClasses.addElement(newEntry);
					}
				} else {
					// Present only in new
					fAddedClasses.addElement(newEntry);
				}
			}
		}
		// Remaining ones are removed.
		for (Enumeration ee = fileNames.elements(); ee.hasMoreElements();) {
			fRemovedClasses.addElement(ee.nextElement());
		}
	}
}
/**
 * Computes the added, removed, changed packages and zips 
 * for this incremental build.
 * Looks only in the union of the old and the new classpaths.
 * Folders outside this union are ignored.
 */
protected void computeAllPackages() {
	fAddedPkgOrZips = new Vector();
	fRemovedPkgOrZips = new Vector();
	fChangedPkgOrZips = new Vector();
	IPackageFragmentRoot[] oldRoots = fOldState.getPackageFragmentRootsInClassPath();
	fNewState.readClassPath(); // TBD: Only read it if changed.
	IPackageFragmentRoot[] newRoots = fNewState.getPackageFragmentRootsInClassPath();
	for (Enumeration e = fSourceDeltas.elements(); e.hasMoreElements();) {
		IResourceDelta delta = (IResourceDelta) e.nextElement();
		computeAllPackages(delta, oldRoots, newRoots);
	}
}
protected void computeAllPackages(IResourceDelta delta, IPackageFragmentRoot[] oldRoots, IPackageFragmentRoot[] newRoots) {
	int status = delta.getKind();
	IPath path = delta.getFullPath();
	IResource rootResource = null;
	switch (delta.getKind()) {
		case IResourceDelta.ADDED :
			/* Look for this package only in the new roots */
			for (int i = 0; i < newRoots.length; i++) {
				rootResource = null;
				try {
					rootResource = newRoots[i].getUnderlyingResource();
				} catch (JavaModelException e) {
				}
				if (rootResource != null && rootResource.getFullPath().isPrefixOf(path)) {
					fAddedPkgOrZips.addElement(path);
					break;
				}
			}
			break;
		case IResourceDelta.REMOVED :
			/* Look for this package only in the old roots */
			for (int i = 0; i < oldRoots.length; i++) {
				rootResource = null;
				try {
					rootResource = oldRoots[i].getUnderlyingResource();
				} catch (JavaModelException e) {
				}
				if (rootResource != null && rootResource.getFullPath().isPrefixOf(path)) {
					fRemovedPkgOrZips.addElement(path);
					break;
				}
			}
			break;
		case IResourceDelta.CHANGED :
			/* Look for this package in the union of both sets of roots */
			boolean found = false;
			for (int i = 0; i < newRoots.length; i++) {
				rootResource = null;
				try {
					rootResource = newRoots[i].getUnderlyingResource();
				} catch (JavaModelException e) {
				}
				if (rootResource != null && rootResource.getFullPath().isPrefixOf(path)) {
					found = true;
					break;
				}
			}
			if (!found) {
				for (int i = 0; i < oldRoots.length; i++) {
					rootResource = null;
					try {
						oldRoots[i].getUnderlyingResource();
					} catch (JavaModelException e) {
					}
					if (rootResource != null && rootResource.getFullPath().isPrefixOf(path)) {
						found = true;
						break;
					}
				}
			}
			if (found) {
				/* Only include changes if it's not an archive, or if it's an archive and the contents really changed */
				if (!fNewState.isZipElement(path)
					|| (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
					fChangedPkgOrZips.addElement(delta);
				}
			}
			break;
	}
	IResourceDelta[] children = delta.getAffectedChildren();
	for (int i = 0; i < children.length; ++i) {
		String extension = children[i].getFullPath().getFileExtension();
		if (extension == null
			|| extension.equalsIgnoreCase("zip"/*nonNLS*/)
			|| extension.equalsIgnoreCase("jar"/*nonNLS*/)) {
			// TBD: Currently rely on empty extension indicating folder
			computeAllPackages(children[i], oldRoots, newRoots);
		}
	}
}
/**
 * Computes namespace changes for each added, removed and changed class file or JCU.
 * The appropriate namespace node is informed of the changes, and it
 * may invalidate its dependents where necessary.  JCUs that need compiling
 * as a result of invalidations are stored by the state.
 * Must process removed types here, even though in most cases there will already
 * be an explicit dependency on the removed type, because it is possible for others
 * to have a namespace dependency but not a type dependency (e.g. in the case of errors).
 */
protected void computeNamespaceChanges() {

	for (Enumeration e = fSourceDeltas.elements(); e.hasMoreElements();) {
		IResourceDelta delta = (IResourceDelta) e.nextElement();
		JavaModelManager.getJavaModelManager().closeAffectedElements(delta);
	}

	// Should really only process packages in image context here,
	// but in general other packages may depend on namespaces being added, 
	// not just those being removed and changed.  So for now, process everything.

	// The computations here must be based on the computed namespaces rather than
	// directly off of the namespace contributions of affected source elements
	// since the namespace computation may remove items due to conflicts.

	int numPackages = fAddedPackageHandles.size() + fRemovedPackageHandles.size() + fChangedPackageHandles.size();
	if (numPackages == 0) {
		fNotifier.updateProgressDelta(0.10f);
		return;
	}
	float progressDelta = 0.10f / numPackages;

	// Process changes in the set of package prefixes
	if (fAddedPackageHandles.size() > 0 || fRemovedPackageHandles.size() > 0) {
		computePackagePrefixChanges();
	}

	// Process added packages
	for (Enumeration addedPkgs = fAddedPackageHandles.elements(); addedPkgs.hasMoreElements();) {
		IPackage pkg = (IPackage) addedPkgs.nextElement();
		fNotifier.subTask(Util.bind("build.analyzing"/*nonNLS*/, PackageImpl.readableName(pkg)));

		// Mark all dependents of missing namespace as needing compile.
		markDependentsAsNeedingCompile(pkg);

		// If any types currently exist with the same name as this package,
		// they must be recompiled
		markOverlappingTypesAsNeedingCompile(pkg);
		fNotifier.updateProgressDelta(progressDelta);
		fNotifier.checkCancel();
	}

	// Process removed packages
	for (Enumeration removedPkgs = fRemovedPackageHandles.elements(); removedPkgs.hasMoreElements();) {
		IPackage pkg = (IPackage) removedPkgs.nextElement();
		fNotifier.subTask(Util.bind("build.analyzing"/*nonNLS*/, PackageImpl.readableName(pkg)));

		// Mark all dependents of namespace as needing compile.
		markDependentsAsNeedingCompile(pkg);

		// If any types currently exist with the same name as this package,
		// they must be recompiled
		markOverlappingTypesAsNeedingCompile(pkg);
		fNotifier.updateProgressDelta(progressDelta);
		fNotifier.checkCancel();
	}

	// Process changed packages
	for (Enumeration changedPkgs = fChangedPackageHandles.elements(); changedPkgs.hasMoreElements();) {
		IPackage pkg = (IPackage) changedPkgs.nextElement();
		fNotifier.subTask(Util.bind("build.analyzing"/*nonNLS*/, PackageImpl.readableName(pkg)));
		computeNamespaceChanges(pkg);
		fNotifier.updateProgressDelta(progressDelta);
		fNotifier.checkCancel();
	}
}
/**
 * Computes the names with namespace changes for the given type.
 */
protected void computeNamespaceChanges(Hashtable oldTSEntries, String parentTypeName, org.eclipse.jdt.core.IType type, Vector vTypeNames) throws JavaModelException {
	String typeName = type.getElementName();
	if (parentTypeName != null) {
		int len = parentTypeName.length() + typeName.length() + 1;
		typeName = new StringBuffer(len).append(parentTypeName).append("$"/*nonNLS*/).append(typeName).toString();
	}
	/* Remove it so that only non-matching ones remain in the table. */
	TypeStructureEntry tsEntry = (TypeStructureEntry) oldTSEntries.remove(typeName);
	if (tsEntry == null || !compareVisibilityAndGender(tsEntry, type)) {
		vTypeNames.addElement(typeName);
	}
	org.eclipse.jdt.core.IType[] memberTypes = type.getTypes();
	for (int i = 0; i < memberTypes.length; ++i) {
		computeNamespaceChanges(oldTSEntries, typeName, memberTypes[i], vTypeNames);
	}
}
/**
 * Computes the names with namespace changes for the given element.
 * Handles the cases where the element is added, removed, or changed.
 */
protected void computeNamespaceChanges(PackageElement element, Vector vTypeNames) {
	Hashtable oldTSEntries = new Hashtable(5);
	SourceEntry oldSourceEntry = fOldState.getSourceEntry(element);
	if (oldSourceEntry != null) {
		// Ignore entries from zip files, since zip file changes are handled wholesale elsewhere.
		if (oldSourceEntry.getZipEntryName() == null) {
			org.eclipse.jdt.internal.core.builder.IType[] oldTypes = fOldState.getInternalDependencyGraph().getTypes(element);
			if (oldTypes != null) {
				for (int i = 0; i < oldTypes.length; ++i) {
					TypeStructureEntry tsEntry = fOldState.getTypeStructureEntry(oldTypes[i], false);
					if (tsEntry != null) {
						oldTSEntries.put(tsEntry.getType().getSimpleName(), tsEntry);
					}
				}
			}
		}
	}
	SourceEntry newSourceEntry = fNewState.getSourceEntry(element);
	if (newSourceEntry != null) {
		// Ignore entries from zip files, since zip file changes are handled wholesale elsewhere.
		if (oldSourceEntry.getZipEntryName() == null) {
			/* use this if JavaModel is broken 
			String fileName = element.getFileName();
			fileName = fileName.substring(0, fileName.indexOf('.'));
			FakeType[] types = new FakeType[] {new FakeType(fileName)};
			try {
			computeNamespaceChanges(oldTSEntries, null, types[0], vTypeNames);
			} catch (NotPresentException e) {} // ignore
			*/

			IJavaElement javaElement = fNewState.getJavaElement(newSourceEntry);
			if (javaElement instanceof ICompilationUnit) {
				ICompilationUnit unit = (ICompilationUnit) javaElement;
				try {
					org.eclipse.jdt.core.IType[] types = unit.getTypes();
					for (int i = 0; i < types.length; ++i) {
						computeNamespaceChanges(oldTSEntries, null, types[i], vTypeNames);
					}
				} catch (JavaModelException e) {
					// TBD: ignore
				}
			} else {
				if (javaElement instanceof IClassFile) {
					IClassFile classFile = (IClassFile) javaElement;
					try {
						computeNamespaceChanges(oldTSEntries, null, classFile.getType(), vTypeNames);
					} catch (JavaModelException e) {
						// TBD: ignore
					}
				}
			}
		}
	}
	for (Enumeration e = oldTSEntries.keys(); e.hasMoreElements();) {
		String name = (String) e.nextElement();
		vTypeNames.addElement(name);
	}
}
/**
 * Computes namespace changes for each added, removed and changed class file or JCU
 * in an affected package.
 */
protected void computeNamespaceChanges(IPackage pkg) {

	/**
	 * Must remove syntax problems for all source entries in this package
	 * in the old state, regardless of whether they contributed to the old
	 * state's namespace.  This must be done before computing the new namespace
	 * because the computation may reveal new errors that we don't want to remove.
	 */
	Hashtable oldTable = new Hashtable(21);
	SourceEntry[] oldEntries = fOldState.getSourceEntries(pkg);
	if (oldEntries != null) {
		for (int i = 0; i < oldEntries.length; i++) {
			SourceEntry oldEntry = oldEntries[i];
			fNewState.getProblemReporter().removeSyntaxErrors(oldEntry);
			oldTable.put(oldEntry.getFileName(), oldEntry);
		}
	}
	Vector vTypeNames = new Vector();
	SourceEntry[] newEntries = fNewState.getSourceEntries(pkg);
	if (newEntries != null) {
		Dictionary sourceChanges = getSourceChanges(pkg);
		for (int i = 0; i < newEntries.length; ++i) {
			SourceEntry newEntry = newEntries[i];
			SourceEntry oldEntry = (SourceEntry) oldTable.remove(newEntry.getFileName());
			if (oldEntry == null) {
				/* Added. Issue indictment based only on file name. */
				vTypeNames.addElement(newEntry.getName());
			} else {
				if (!oldEntry.equals(newEntry) || sourceChanges.get(newEntry.getPath()) != null) {
					/* Changed. Issue indictments by comparing source types with previously built types. */
					PackageElement element = new PackageElement(pkg, newEntry);
					computeNamespaceChanges(element, vTypeNames);
				}
			}
		}
	}
	/* Only removed source entries should remain in oldTable now. */
	for (Enumeration e = oldTable.elements(); e.hasMoreElements();) {
		SourceEntry oldEntry = (SourceEntry) e.nextElement();
		/* Removed. Issue indictment based only on file name. */
		vTypeNames.addElement(oldEntry.getName());
	}
	if (vTypeNames.isEmpty()) {
		return;
	}
	IndictmentSet indicts = new IndictmentSet();
	Hashtable nestedIndictsTable = null;
	for (Enumeration e = vTypeNames.elements(); e.hasMoreElements();) {
		String name = (String) e.nextElement();
		int lastDollar = name.lastIndexOf('$');
		if (lastDollar == -1) {
			indicts.add(Indictment.createTypeIndictment(name));
		} else {
			// Nested type.  Issue indictments as if containing type was a package.
			// Dependencies on missing member types look like namespace dependencies on
			// package with same name as enclosing type.
			String qualification = name.substring(0, lastDollar);
			// Convert qualification from $ separated to . separated.
			// For example if name = "A$B$C", typeName = "C" and qualification = "A.B".
			qualification = qualification.replace('$', '.');
			String typeName = name.substring(lastDollar + 1);

			// Issue indictments, not relative to current package.
			// This catches dependencies on missing types in same package (e.g. ref is A.B).
			IPackage nestedPkg = fDC.getImage().getPackageHandle(qualification, false);
			nestedPkg = fNewState.canonicalize(nestedPkg);
			if (nestedIndictsTable == null) {
				nestedIndictsTable = new Hashtable(11);
			}
			IndictmentSet nestedIndicts = (IndictmentSet) nestedIndictsTable.get(nestedPkg);
			if (nestedIndicts == null) {
				nestedIndicts = new IndictmentSet();
				nestedIndictsTable.put(nestedPkg, nestedIndicts);
			}
			nestedIndicts.add(Indictment.createTypeIndictment(typeName));

			// Issue indictments, relative to current package (only if not unnamed package).
			// This catches dependencies on missing types in other packages (e.g. ref is some.other.pkg.A.B).
			if (!pkg.isUnnamed()) {
				nestedPkg = fDC.getImage().getPackageHandle(pkg.getName() + '.' + qualification, false);
				if (nestedIndictsTable == null) {
					nestedIndictsTable = new Hashtable(11);
				}
				nestedIndicts = (IndictmentSet) nestedIndictsTable.get(nestedPkg);
				if (nestedIndicts == null) {
					nestedIndicts = new IndictmentSet();
					nestedIndictsTable.put(nestedPkg, nestedIndicts);
				}
				nestedIndicts.add(Indictment.createTypeIndictment(typeName));
			}
		}
	}
	if (!indicts.isEmpty()) {
		issueIndictments(pkg, indicts, false);
	}
	if (nestedIndictsTable != null) {
		for (Enumeration e = nestedIndictsTable.keys(); e.hasMoreElements();) {
			IPackage nestedPkg = (IPackage) e.nextElement();
			IndictmentSet nestedIndicts = (IndictmentSet) nestedIndictsTable.get(nestedPkg);
			issueIndictments(pkg, nestedIndicts, false);
		}
	}
}
/**
 * Compute the added and removed package prefixes (not actual packages)
 * and issue the appropriate indictments on the namespaces.
 */
protected void computePackagePrefixChanges() {
	Hashtable oldTable = computePackagePrefixes(fOldState.getPackageMap());
	Hashtable newTable = computePackagePrefixes(fNewState.getPackageMap());
	for (Enumeration e = oldTable.elements(); e.hasMoreElements();) {
		String name = (String) e.nextElement();
		if (newTable.remove(name) == null) {
			int lastDot = name.lastIndexOf('.');
			if (lastDot != -1) {
				String parentName = name.substring(0, lastDot);
				String simpleName = name.substring(lastDot + 1);
				IPackage parentPkg = fDC.getImage().getPackageHandle(parentName, false);
				IndictmentSet indictments = new IndictmentSet();
				indictments.add(Indictment.createTypeIndictment(simpleName));
				issueIndictments(parentPkg, indictments, false);
			}
		}
	}
	for (Enumeration e = newTable.elements(); e.hasMoreElements();) {
		String name = (String) e.nextElement();
		int lastDot = name.lastIndexOf('.');
		if (lastDot != -1) {
			String parentName = name.substring(0, lastDot);
			String simpleName = name.substring(lastDot + 1);
			IPackage parentPkg = fDC.getImage().getPackageHandle(parentName, false);
			IndictmentSet indictments = new IndictmentSet();
			indictments.add(Indictment.createTypeIndictment(simpleName));
			issueIndictments(parentPkg, indictments, false);
		}
	}
}
/**
 * Returns a set of strings containing the names of all packages
 * in the image, and all their prefixes.
 */
protected Hashtable computePackagePrefixes(PackageMap packageMap) {
	Hashtable prefixes = new Hashtable(packageMap.size() * 3 + 1);
	for (Enumeration e = packageMap.getAllPackages(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		if (!pkg.isUnnamed()) {
			String name = pkg.getName();
			while (!prefixes.containsKey(name)) {
				prefixes.put(name, name);
				int i = name.lastIndexOf('.');
				if (i == -1)
					break;
				name = name.substring(0, i);
			}
		}
	}
	return prefixes;
}
/**
 * For debugging only.
 */
static void dump(IResourceDelta delta) {
	StringBuffer sb = new StringBuffer();
	IPath path = delta.getFullPath();
	for (int i = path.segmentCount(); --i > 0;) {
		sb.append("  "/*nonNLS*/);
	}
	switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			sb.append('+');
			break;
		case IResourceDelta.REMOVED:
			sb.append('-');
			break;
		case IResourceDelta.CHANGED:
			sb.append('*');
			break;
		case IResourceDelta.NO_CHANGE:
			sb.append('=');
			break;
		default:
			sb.append('?');
			break;
	}
	sb.append(path);
	System.out.println(sb.toString());
	IResourceDelta[] children = delta.getAffectedChildren();
	for (int i = 0; i < children.length; ++i) {
		dump(children[i]);
	}
}
/**
 * Returns an enumeration of the packages that have been affected
 * by the build.
 */
protected Enumeration getAffectedPackages() {
	return fAffectedPackages.keys();
}
/**
 * Returns a builder type for the given old and new type structure entries.
 * Either old or new entries may be null (but not both).
 * This method should only be called if there is no associated builder
 * type already in the builder type table.
 */
protected BuilderType getBuilderType(TypeStructureEntry oldEntry, TypeStructureEntry newEntry) {
	IType handle = null;
	BuilderType type = null;
	if (oldEntry == null) {
		/* must have been added */
		Assert.isNotNull(newEntry);
		type = new NewBuilderType(this, newEntry);
		handle = newEntry.getType();
	} else {
		if (newEntry == null) {
			/* must have been deleted */
			Assert.isNotNull(oldEntry);
			type = new OldBuilderType(this, oldEntry);
			handle = oldEntry.getType();
		} else {
			if (oldEntry.fSourceEntry.equals(newEntry.fSourceEntry)) {
				/* unchanged */
				type = new UnmodifiedBuilderType(this, oldEntry);
				handle = oldEntry.getType();
			} else {
				/* modified */
				IBinaryType oldBinary = fOldState.getBinaryTypeOrNull(oldEntry);
				type = new ModifiedBuilderType(this, oldEntry, oldBinary);
				handle = oldEntry.getType();
			}
		}
	}
	fBuilderTypeTable.put(handle, type);
	return type;
}
/**
 * Returns a builder type for the given type.
 */
protected BuilderType getBuilderType(IType type) {
	BuilderType builderType = (BuilderType) fBuilderTypeTable.get(type);
	if (builderType != null) {
		return builderType;
	}
	return getBuilderType(fOldState.getTypeStructureEntry(type, false), fNewState.getTypeStructureEntry(type, false));
}
/**
 * Returns an object describing the differences between the old 
 * state and the new state, otherwise returns null.  The delta is 
 * restricted to the given ImageContext.  This image delta will 
 * include entries for all program elements that are present in:
 * <pre>
 * (oldState UNION newState) INTERSECT imageContext
 *</pre>
 * That is, it will include each program element that is present in one or the other
 * state and also in the given image context.
 * Any delta objects navigated to from the result are restricted 
 * to the same ImageContext.
 * Note that there is no necessary relationship between the image context
 * supplied and the build contexts of the old and new states.
 */
public IDelta getImageDelta(IImageContext imageContext) {
	if (fImageDelta == null || !Util.equalOrNull(imageContext, fContextOfLastDelta)) {
		fImageDelta = new DeltaImpl(this, imageContext);
		fContextOfLastDelta = imageContext;
	}
	return fImageDelta;
}
/**
 * Returns a set of paths of files which are changing in the given package.
 */
protected Dictionary getSourceChanges(IPackage pkgHandle) {
	Dictionary set = new Hashtable(30);

	/* do for each fragment of this package */
	IPath[] newFrags = fNewState.getPackageMap().getFragments(pkgHandle);
	for (int i = 0; i < newFrags.length; i++) {
		IPath fragPath = newFrags[i];

		/* find the JCUs for this package */
		IResourceDelta[] fileDeltas = null;
		for (Enumeration e = fChangedPkgOrZips.elements(); e.hasMoreElements();) {
			IResourceDelta pkgDelta = (IResourceDelta) e.nextElement();
			if (pkgDelta.getFullPath().equals(fragPath)) {
				/* A zip file is changing.  Don't bother optimizing this case -- too complex. */
				if (!fNewState.isZipElement(fragPath)) {
					fileDeltas = pkgDelta.getAffectedChildren(IResourceDelta.CHANGED);
				}
				break;
			}
		}
		if (fileDeltas != null) {
			/* do for each changed file in this fragment */
			for (int j = 0; j < fileDeltas.length; j++) {
				IResourceDelta fileDelta = fileDeltas[j];
				// See 1FVSC75: ITPJCORE:ALL - SCENARIO B1 - Builder should check F_CONTENT on changed files
				if (fileDelta.getKind() == IResourceDelta.CHANGED && (fileDelta.getFlags() & IResourceDelta.CONTENT) != 0) {
					IPath path = fileDelta.getFullPath();

					/* skip non-java resources */
					String extension = path.getFileExtension();
					if (extension != null && (extension.equalsIgnoreCase("java"/*nonNLS*/) || extension.equalsIgnoreCase("class"/*nonNLS*/))) {
						set.put(path, path);
					}
				}
			}
		}
	}
	return set;
}
protected boolean hasPackageMapChanges() {
	if (!Util.equalArraysOrNull(fOldState.getPackageFragmentRootsInClassPath(), fNewState.getPackageFragmentRootsInClassPath())) {
		return true;
	}

	/* Has package or zip been added / removed ? */
	if (fRemovedPkgOrZips.size() > 0 || fAddedPkgOrZips.size() > 0) {
		return true;
	}

	/* if there is a changed zip file, assume package map changes */
	for (Enumeration e = fChangedPkgOrZips.elements(); e.hasMoreElements();) {
		IResourceDelta changed = (IResourceDelta) e.nextElement();
		String extension = changed.getFullPath().getFileExtension();
		if (extension != null) {
			if (extension.equalsIgnoreCase("zip"/*nonNLS*/) || extension.equalsIgnoreCase("jar"/*nonNLS*/)) {
				return true;
			}
		}
	}
	return false;
}
/**
 * Returns true if there is a change to the list of source entries in this package, false
 * otherwise.  This is meant to be a fast optimization, to determine
 * whether the source entries for this package need to be recomputed.  This
 * method works solely against the source workspace delta.  When in doubt,
 * it is always okay to return true here.
 */
protected boolean hasSourceEntryChanges(IPackage pkgHandle) {
	IPath[] oldFrags = fOldState.getPackageMap().getFragments(pkgHandle);
	IPath[] newFrags = fNewState.getPackageMap().getFragments(pkgHandle);
	if (!Util.equalArraysOrNull(oldFrags, newFrags)) {
		/* The set of package fragments has changed.  Don't bother optimizing this case -- too complex. */
		return true;
	}

	/* do for each fragment of this package */
	for (int i = 0; i < newFrags.length; i++) {
		IPath fragPath = newFrags[i];

		/* find the JCUs for this package */
		IResourceDelta[] fileDeltas = null;
		for (Enumeration e = fChangedPkgOrZips.elements(); e.hasMoreElements();) {
			IResourceDelta pkgDelta = (IResourceDelta) e.nextElement();
			if (pkgDelta.getFullPath().equals(fragPath)) {
				/* A zip file is changing.  Don't bother optimizing this case -- too complex. */
				if (fNewState.isZipElement(fragPath)) {
					return true;
				}
				fileDeltas = pkgDelta.getAffectedChildren();
				break;
			}
		}
		if (fileDeltas == null) {
			/**
			 * It's a more complex interaction between zips, for example
			 * the package may be added in one zip and removed from another.
			 * Don't bother optimizing this case -- too complex.
			 */
			return true;
		}

		/* do for each file in this fragment in the new workspace */
		for (int j = 0; j < fileDeltas.length; j++) {
			IPath path = fileDeltas[j].getFullPath();

			/* skip java resources */
			String extension = path.getFileExtension();
			if (extension != null) {
				if ((extension.equalsIgnoreCase("java"/*nonNLS*/) || extension.equalsIgnoreCase("class"/*nonNLS*/))) {
					/* if there is an added or removed jcu or binary, the source entries have changed */
					int status = fileDeltas[j].getKind();
					if (status == IResourceDelta.ADDED || status == IResourceDelta.REMOVED) {
						return true;
					}
					/* it's a change, but it may be changing local status */
					if (fileDeltas[j].getResource().isLocal(IResource.DEPTH_ZERO) != fOldState.contains(new SourceEntry(path, null, null))) {
						return true;
					}
				}
			}
		}
	}
	return false;
}
/**
 * Issues indictments to the dependents of the specified element.
 * If transitive is true, they are issued to all dependents transitively,
 * otherwise they are issued only to immediate dependents.
 * The trial process is run as the indictments are issued.
 * Any newly-convicted compilation units are added to the to-be-compiled list.
 */
protected void issueIndictments(Object element, IndictmentSet indicts, boolean transitive) {
	DependencyGraph graph = fNewState.getInternalDependencyGraph();
	Object[] dependents = graph.getDependents(element);
	if (dependents.length > 0) {
		Hashtable seen = new Hashtable(11);
		seen.put(element, element); // Don't visit the given element

		for (int i = 0; i < dependents.length; ++i) {
			issueIndictments(graph, dependents[i], indicts, transitive, seen);
		}
	}
}
/**
 * Issues indictments to the specified element.
 * If transitive is true, they are issued to all dependents transitively,
 * otherwise they are issued only to this element.
 * The trial process is run as the indictments are issued.
 * Any newly-convicted compilation units are added to the to-be-compiled list.
 */
protected void issueIndictments(DependencyGraph graph, Object element, IndictmentSet indicts, boolean transitive, Hashtable seen) {
	// Only issue indictments to compilation units.
	if (element instanceof PackageElement) {
		PackageElement pkgElement = (PackageElement) element;
		if (pkgElement.isSource()) {
			// Have we already seen this one?
			if (!seen.containsKey(element)) {
				seen.put(element, element);
				// Is it already in the queue?
				if (!fWorkQueue.contains(pkgElement)) {
					// If it's not being removed, conduct the trial.
					if (fNewState.getSourceEntry(pkgElement) != null) {
						if (tryUnit(pkgElement, indicts)) {
							fWorkQueue.add(pkgElement);
						}
					}
				}

				// Recurse if transitive
				if (transitive) {
					Object[] dependents = graph.getDependents(element);
					for (int i = 0; i < dependents.length; ++i) {
						issueIndictments(graph, dependents[i], indicts, transitive, seen);
					}
				}
			}
		}
	}
}
/**
 * Mark the immediate dependents of the given element
 * as needing to be compiled (if not marked so already),
 * and add any newly marked ones to the list.
 */
protected void markDependentsAsNeedingCompile(Object element) {
	DependencyGraph graph = fNewState.getInternalDependencyGraph();
	Object[] deps = graph.getDependents(element);
	for (int i = 0; i < deps.length; ++i) {
		if (deps[i] instanceof PackageElement) {
			PackageElement pkgElement = (PackageElement) deps[i];
			if (pkgElement.isSource()) {
				if (!fWorkQueue.contains(pkgElement)) {
					// Mark it, if it's not being removed.
					if (fNewState.getSourceEntry(pkgElement) != null) {
						fWorkQueue.add(pkgElement);
					}
				}
			}
		}
	}
}
/**
 * All elements that refer to changed zips, or packages
 * referred to by changed zips, must be recompiled.
 * This is conservative, but since the old zip structure
 * is not available, we can't do much better.
 */
protected void markDependentsOfChangedZips() {

	PathMap oldPathMap = fOldState.getPathMap();
	PathMap newPathMap = fNewState.getPathMap();
	
	/* do for each changed zip */
	for (Enumeration e = fChangedZips.elements(); e.hasMoreElements();) {
		IPath zip = (IPath) e.nextElement();

		/* mark dependents of packages in old zip */
		IPackage[] pkgs = oldPathMap.packageHandlesFromPath(zip);
		for (int i = 0; i < pkgs.length; i++) {
			markDependentsAsNeedingCompile(pkgs[i]);
		}

		/* mark dependents of packages in new zip */
		pkgs = newPathMap.packageHandlesFromPath(zip);
		for (int i = 0; i < pkgs.length; i++) {
			markDependentsAsNeedingCompile(pkgs[i]);
		}

		/* mark dependents of the zip itself */
		markDependentsAsNeedingCompile(zip);
	}

	/* also handle zips which were removed from class path */
	IPath[] paths = oldPathMap.getPaths();
	for (int i = 0; i < paths.length; ++i) {
		IPath zip = paths[i];
		if (fOldState.isZipElement(zip) && !newPathMap.hasPath(zip)) {

			/* mark dependents of packages in old zip */
			IPackage[] pkgs = oldPathMap.packageHandlesFromPath(zip);
			for (int j = 0; j < pkgs.length; j++) {
				markDependentsAsNeedingCompile(pkgs[j]);
			}
				
			/* mark dependents of the zip itself */
			markDependentsAsNeedingCompile(zip);
		}
	}
}
/**
 * The given package is being added or removed. Make sure that no types
 * exist with the same name as this package.  If any such types
 * exist, mark them as needing compilation and add them to the
 * compile vector.
 */
protected void markOverlappingTypesAsNeedingCompile(IPackage pkg) {
	String pkgName = pkg.getName();
	int lastDot;
	while ((lastDot = pkgName.lastIndexOf('.')) > 0) {

		/* try to get a compilation unit for this package name */
		PackageElement element = fNewState.getCompilationUnitFromName(pkgName, fNewState.defaultPackageForProject());
		if (element != null) {
			fWorkQueue.add(element);
		}

		/* strip off a package level */
		pkgName = pkgName.substring(0, lastDot);
	}
}
/**
 * Force compilation of everything in the given package (non-state-specific handle),
 * if indicated by the build context.
 */
protected void maybeForce(IPackage pkg) {
	ImageContextImpl imageContext = (ImageContextImpl) fNewState.getBuildContext();
	if (imageContext == null ? pkg.inState(fNewState).isPresent() : imageContext.containsPackage(pkg)) {
		// fNotifier.subTask("Forcing " + pkg.getName());
		fNewState.getAllTypesForPackage(pkg);
		checkCancel();
	}
}
/**
 * Propagate the principal structure by package table,
 * being sure to exclude any affected packages.
 */
protected void propagatePrincipalStructureByPackageTable() {
	Hashtable newTable = (Hashtable) fOldState.getPrincipalStructureByPackageTable().clone();
	for (Enumeration e = fAffectedPackages.keys(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		newTable.remove(pkg);
	}
	fNewState.setPrincipalStructureByPackageTable(newTable);
}
/**
 * Recomputes the source entries for changed packages. As an optimization, 
 * only recompute the source entries for packages that may actually have
 * changes to their source entries.
 */
protected void recomputeSourceEntriesForChangedPackages() {
	for (Enumeration e = fChangedPackageHandles.elements(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		if (hasSourceEntryChanges(pkg)) {
			fNewState.getSourceElementTable().removePackage(pkg);
			// Now force it.
			fNewState.getSourceEntries(pkg);
		}
	}
}
/**
 * Remove all types and compilation units from the new state 
 * that do not exist in the new workspace.  Keep track of any 
 * types that will need to be compiled as a result of the change.
 */
protected void removeOldClasses() {
	for (Enumeration e = fRemovedClasses.elements(); e.hasMoreElements();) {
		SourceEntry entry = (SourceEntry) e.nextElement();
		removeSourceElement(entry);
	}
}
/**
 * Removes the given source element from the new state's tables
 * and dependency graph.  Marks all JCUs that depend on the removed element. 
 */
protected void removeSourceElement(SourceEntry entry) {
	PackageElement element = fOldState.packageElementFromSourceEntry(entry);
	SourceEntry oldEntry = fOldState.getSourceEntry(element);
	if (oldEntry == null) {
		// It didn't exist in the old state (strange).
		return;
	}

	// delete problems for this entry
	// only delete non-syntax problems, since new syntax problems may have already
	// been generated during namespace computations
	fNewState.getProblemReporter().removeNonSyntaxErrors(oldEntry);

	/* remove type descriptor for types that belong to this source element */
	DependencyGraph graph = fNewState.getInternalDependencyGraph();
	Hashtable structureTable = fNewState.getPrincipalStructureTable();
	IType[] types = graph.getTypes(element);
	if (types != null) {
		for (int i = 0; i < types.length; ++i) {
			IType type = types[i];
			structureTable.remove(type);
			if (!element.isBinary()) {
				fNewState.getBinaryOutput().deleteBinary(type);
			}
		}
	}
	markDependentsAsNeedingCompile(element);
	graph.remove(element);

	// The element has already been removed from the source element table.
	// So don't do any source element table or fragment logic here.

	// Don't delete the namespace for the package here, because it cannot be rebuilt lazily
	// since the package may be in process of being removed.
}
/**
 * Hang onto the binary types for this source, because it will be compiled
 */
protected void saveBinaryTypes(PackageElement element) {
	DependencyGraph graph = fOldState.getInternalDependencyGraph();
	IType[] types = graph.getTypes(element);
	for (int i = 0; i < types.length; i++) {
		IType type = types[i];
		BuilderType builderType = (BuilderType) fBuilderTypeTable.get(type);

		if (builderType == null || !builderType.isAffected()) {
			TypeStructureEntry oldEntry = fOldState.getTypeStructureEntry(type, false);
			Assert.isNotNull(oldEntry);

			/* create and store the builder type */
			// Allow it to be missing.  See 1FW1S0Y: ITPJCORE:ALL - Java builder builds when non-Java files affected
			IBinaryType oldBinary = fOldState.getBinaryTypeOrNull(oldEntry);
			builderType = new ModifiedBuilderType(this, oldEntry, oldBinary);
			fBuilderTypeTable.put(type, builderType);

			/* remove the principal structure entry in the new state */
			fNewState.getPrincipalStructureTable().remove(type);

			/* nuke the binary because it will soon be stale */
			fNewState.getBinaryOutput().deleteBinary(type);
		}
	}
}
/**
 * Sort the compilation units by topological order.
 */
protected void sort(PackageElement[] compileArray) {
	DependencyGraph graph = fNewState.getInternalDependencyGraph();
	int len = compileArray.length;
	int[] sortOrder = new int[len];
	for (int i = 0; i < len; ++i) {
		sortOrder[i] = graph.getOrder(compileArray[i]);
	}
	Util.sort(compileArray, sortOrder);
}
/**
 * Returns a string describe the builder
 * @see IImageBuilder
 */
public String toString() {
	return "incremental image builder for:\n"/*nonNLS*/ + "\tnew state: "/*nonNLS*/ + getNewState() + "\n"/*nonNLS*/ + "\told state: "/*nonNLS*/ + getOldState();
}
/**
 * If this type is a subtype is the originator of an abstract method
 * indictment, it must be compiled
 */
protected boolean tryAbstractMethodIndictments(PackageElement unit, IndictmentSet indictments) {
	final boolean GUILTY = true, INNOCENT = false;	
	IType[] trialTypes = indictments.getAbstractMethodOriginators();
	if (trialTypes.length == 0) {
		return false;
	}
	/* if problems were detected, some innerclasses might not have been generated,
		thus the state would not reflect their presence, and even if guilty could not
		be convicted (also see 1GA6CV7) */
	Vector problemVector = fOldState.getProblemReporter().getProblemVector(fOldState.getSourceEntry(unit));
	if (problemVector != null){
		Enumeration problems = problemVector.elements();
		while (problems.hasMoreElements()){
			IProblemDetail problem = (IProblemDetail) problems.nextElement();
			if ((problem.getSeverity() & IProblemDetail.S_ERROR) != 0) return GUILTY;
		}
	}	
	
	/* do for each type in this package element */
	IType[] types = fOldState.getInternalDependencyGraph().getTypes(unit);
	for (int i = 0, imax = types.length; i < imax; i++) {
		IType type = types[i];
		TypeStructureEntry tsEntry = fOldState.getTypeStructureEntry(type, false);

		/* shouldn't happen, but trust nobody! */
		if (tsEntry == null) {
			continue;
		}
		IType oldType = (IType) type.inState(fOldState);
		int flags = oldType.getModifiers();

		/* interfaces aren't affected by method additions/removals in super interfaces,
		   other than for compatibility checking, which is handled elsewhere */
		if (oldType.isInterface()) {
			continue;
		}

		/* only check superclasses if this class is not abstract, */
		/* because abstract classes aren't affected by abstract method additions/removals */
		/* on their superclasses, only on their superinterfaces */
		boolean checkSuperclasses = (flags & IConstants.AccAbstract) == 0;

		/* do for each abstract method originator */
		BuilderType builderType = getBuilderType(type);
		for (int j = 0, jmax = trialTypes.length; j < jmax; ++j) {
			IType trialType = trialTypes[j];
			if (checkSuperclasses) {
				if (builderType.hasSuperclass(trialType)) {
					return GUILTY;
				}
			}
			if (builderType.hasSuperInterface(trialType)) {
				return GUILTY;
			}
		}
	}

	/* no supertypes convicted this package element, so it must be innocent */
	return INNOCENT;
}
/**
 * If a subtype of the originator of a method indictment redefines
 * the method for which there is an indictment, it must be recompiled.
 * This is also true if any superinterfaces of the subtype defines
 * an indicted method.
 */
protected boolean tryMethodDeclarations(BuilderType builderType, IndictmentSet indictments) {
	final boolean GUILTY = true, INNOCENT = false;

	/* try the methods of this type */
	IBinaryType oldBinary = builderType.getOldBinaryType();
	if (oldBinary == null) return GUILTY;
	
	IBinaryMethod[] methods = oldBinary.getMethods();
	if (methods != null) {
		for (int k = 0; k < methods.length; k++) {
			if (indictments.tryMethodDeclaration(methods[k])) {
				return GUILTY;
			}
		}
	}

	/* recurse on superinterfaces */
	char[][] interfaces = oldBinary.getInterfaceNames();
	if (interfaces != null) {
		for (int i = 0; i < interfaces.length; i++) {
			BuilderType supr = getBuilderType(BinaryStructure.getType(builderType.getNewState(), builderType.getNewTypeStructureEntry(), interfaces[i]));
			if (tryMethodDeclarations(supr, indictments)) {
				return GUILTY;
			}
		}
	}
	return INNOCENT;
}
/**
 * If a subtype of the originator of a method indictment redefines
 * the method for which there is an indictment, it must be recompiled.
 */
protected boolean tryMethodDeclarations(PackageElement unit, IndictmentSet indictments) {
	final boolean GUILTY = true, INNOCENT = false;
	IType[] methodIndictmentOwners = indictments.getMethodIndictmentOwners();
	if (methodIndictmentOwners.length == 0) {
		return INNOCENT;
	}
	/* if problems were detected, some innerclasses might not have been generated,
		thus the state would not reflect their presence, and even if guilty could not
		be convicted (also see 1GA6CV7) */
	Vector problemVector = fOldState.getProblemReporter().getProblemVector(fOldState.getSourceEntry(unit));
	if (problemVector != null){
		Enumeration problems = problemVector.elements();
		while (problems.hasMoreElements()){
			IProblemDetail problem = (IProblemDetail) problems.nextElement();
			if ((problem.getSeverity() & IProblemDetail.S_ERROR) != 0) return GUILTY;
		}
	}	
	IType[] types = fOldState.getInternalDependencyGraph().getTypes(unit);
	for (int i = 0; i < types.length; ++i) {
		boolean found = false;
		BuilderType trialType = getBuilderType(types[i]);
		for (int j = 0, len = methodIndictmentOwners.length; j < len; ++j) {
			//note this is conservative because owners are not matched to their methods
			if (trialType.hasSuperType(methodIndictmentOwners[j])) {
				found = true;
				break;
			}
		}
		if (found) {
			if (tryMethodDeclarations(trialType, indictments)){
				return GUILTY;
			}
		}
	}
	return INNOCENT;
}
/**
 * Conducts a trial on a single compilation unit.  Returns true if the unit
 * is guilty.  The sentence is compilation without possibility of parole.
 */
protected boolean tryUnit(PackageElement unit, IndictmentSet indictments) {

	/* innocent unless proven guilty */
	final boolean GUILTY = true, INNOCENT = false;

	/* quick test to see if there's no indictments */
	if (indictments.isEmpty()) {
		return INNOCENT;
	}

	/* automatically guilty if there is an upstream hierarchy change */
	if (indictments.hasHierarchyIndictment()) {
		return GUILTY;
	}

	/* gather the evidence */
	ReferenceInfo evidence = fNewState.getReferencesForPackageElement(unit);

	/* Unable to index unit, convict and let compiler try it */
	if (evidence == null) {
		return GUILTY;
	}
	if (indictments.tryAllEvidence(evidence)) {
		return GUILTY;
	}

	/**
	 * If a subtype of the originator of a method indictment redefines
	 * the method for which there is an indictment, it must be recompiled.
	 */
	if (tryMethodDeclarations(unit, indictments)) {
		return GUILTY;
	}

	/**
	 * If this type is a subtype of the originator of an abstract method
	 * indictment, it may need to be recompiled.
	 */
	if (tryAbstractMethodIndictments(unit, indictments)) {
		return GUILTY;
	}

	/**
	 * If there have been changes to constructors in the direct superclass,
	 * it must be recompiled.
	 */
	if (tryZeroArgConstructorInSuperclass(unit, indictments)) {
		return GUILTY;
	}

	/* evidence is exhausted and unit has not been convicted */
	return INNOCENT;
}
/**
 * If there have been changes to the zero-arg constructor in the superclass of any types in the CU,
 * the CU must be recompiled.  This handles refs by default constructors and implicit super invocations in
 * constructors, which aren't covered by the normal method indictments since they
 * generate no evidence of refs to the super constructor.
 */
protected boolean tryZeroArgConstructorInSuperclass(PackageElement unit, IndictmentSet indictments) {
	if (!indictments.hasConstructorIndictments()) {
		return false;
	}
	IType[] types = fOldState.getInternalDependencyGraph().getTypes(unit);
	for (int i = 0; i < types.length; ++i) {
		IType superclass = getBuilderType(types[i]).getSuperclass();
		if (superclass != null) {
			String key = '<' + superclass.getDeclaredName() + ">/0"/*nonNLS*/;
			if (indictments.tryMethodEvidence(key.toCharArray())) {
				return true;
			}
		}
	}
	return false;
}
/**
 * Processes changed classes in the state.  Stores compilation
 * units that need to be compiled as a result of the changes.
 */
protected void updateChangedClasses() {
	for (Enumeration e = fChangedClasses.elements(); e.hasMoreElements();) {
		SourceEntry entry = (SourceEntry) e.nextElement();
		changedSourceElement(entry);
	}
}
/**
 * Updates or rebuilds the package map with the affected package fragments.
 * After, the added/removed/changed builder packages are known.
 */
protected void updatePackageMap() {
	// Simply rebuild if adding or removing packages, rather than trying to do this
	// incrementally, which is tricky.  E.g. a package should not really be added if its
	// project does not appear in the class path.
	boolean rebuild = hasPackageMapChanges();
	if (rebuild) {
		fNewState.buildInitialPackageMap();
	}

	/* Set of affected package handles */
	Vector affected = new Vector();

	/* Process changed package fragments (package map not affected). *
	 * Due to changing class paths, a package which is changing in the source *
	 * may actually be added / removed rather than changed.  Figure out which. */
	for (Enumeration e = fChangedPkgOrZips.elements(); e.hasMoreElements();) {
		IResourceDelta delta = (IResourceDelta) e.nextElement();
		IPackage[] pkgHandles = null;

		/* Look in the new state only if the package has not been removed */
		if (delta.getKind() != IResourceDelta.REMOVED) {
			pkgHandles = fNewState.getPathMap().packageHandlesFromPath(delta.getFullPath());
			for (int i = 0; i < pkgHandles.length; i++) {
				if (!affected.contains(pkgHandles[i])) {
					affected.addElement(pkgHandles[i]);
				}
			}
		}
		/* Look in the old state only if the package has not been added */
		if (delta.getKind() != IResourceDelta.ADDED) {
			pkgHandles = fOldState.getPathMap().packageHandlesFromPath(delta.getFullPath());
			for (int i = 0; i < pkgHandles.length; i++) {
				if (!affected.contains(pkgHandles[i])) {
					affected.addElement(pkgHandles[i]);
				}
			}
		}
	}

	/* Partition affected packages into added/removed/changed */
	fAddedPackageHandles = new Vector();
	fRemovedPackageHandles = new Vector();
	fChangedPackageHandles = new Vector();
	PackageMap oldMap = fOldState.getPackageMap();
	PackageMap newMap = fNewState.getPackageMap();
	for (Enumeration e = affected.elements(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		if (oldMap.containsPackage(pkg)) {
			if (newMap.containsPackage(pkg)) {
				fChangedPackageHandles.addElement(pkg);
			} else {
				fRemovedPackageHandles.addElement(pkg);
			}
		} else {
			if (newMap.containsPackage(pkg)) {
				fAddedPackageHandles.addElement(pkg);
			} else {
				// This can occur if there are changes to a package
				// which does not appear in either the old or new class path.
				// Ignore it.
			}
		}
	}

	/* Check for added/removed/changed packages due to added/removed fragments and/or class path changes */
	if (rebuild) {
		for (Enumeration e = oldMap.getAllPackages(); e.hasMoreElements();) {
			IPackage pkg = (IPackage) e.nextElement();
			IPath[] newFragments = newMap.getFragments(pkg);
			if (newFragments == null) {
				// package has been removed due to class path change;
				// may also have been removed due to source change
				if (!fRemovedPackageHandles.contains(pkg)) {
					fRemovedPackageHandles.addElement(pkg);
				}
			} else
				if (!Util.equalArraysOrNull(oldMap.getFragments(pkg), newFragments)) {
					// package has changed package fragments due to class path change;
					// may also have source change
					if (!fChangedPackageHandles.contains(pkg)) {
						fChangedPackageHandles.addElement(pkg);
					}
				}
		}
		for (Enumeration e = newMap.getAllPackages(); e.hasMoreElements();) {
			IPackage pkg = (IPackage) e.nextElement();
			if (!oldMap.containsPackage(pkg)) {
				// package has been added due to class path change;
				// may also have been added due to source change
				if (!fAddedPackageHandles.contains(pkg)) {
					fAddedPackageHandles.addElement(pkg);
				}
			}
		}
	}

	/* Add all affected packages to fAffectedPackages. */
	fAffectedPackages = new Hashtable(11);
	for (Enumeration e = fAddedPackageHandles.elements(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		fAffectedPackages.put(pkg, pkg);
	}
	for (Enumeration e = fRemovedPackageHandles.elements(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		fAffectedPackages.put(pkg, pkg);
	}
	for (Enumeration e = fChangedPackageHandles.elements(); e.hasMoreElements();) {
		IPackage pkg = (IPackage) e.nextElement();
		fAffectedPackages.put(pkg, pkg);
	}
}
/**
 * Stores the results of a compilation in the appropriate state tables.
 * Keeps track of what compilation units need to be compiled as a result
 * of the changes.
 */
protected void updateState(ConvertedCompilationResult[] results) {
	int n = results.length;
	PackageElement[] oldUnits = new PackageElement[n];
	PackageElement[] newUnits = new PackageElement[n];
	IType[][] oldTypeList = new IType[n][];

	// Preparation
	DependencyGraph oldGraph = fOldState.getInternalDependencyGraph();
	DependencyGraph newGraph = fNewState.getInternalDependencyGraph();

	for (int i = 0; i < n; i++) {
		PackageElement element = results[i].getPackageElement();

		// Be sure the package is in the set of affected packages.
		// It may not be if this unit was recompiled in a package
		// other than the ones which have direct changes.
		IPackage pkg = element.getPackage();
		fAffectedPackages.put(pkg, pkg);

		// Be sure to look up the source entries in the old and new state,
		// since they may be different.
		SourceEntry oldSourceEntry = fOldState.getSourceEntry(element);
		oldUnits[i] = (oldSourceEntry == null ? null : fOldState.packageElementFromSourceEntry(oldSourceEntry));
		SourceEntry newSourceEntry = fNewState.getSourceEntry(element);
		newUnits[i] = fNewState.packageElementFromSourceEntry(newSourceEntry);
		if (oldUnits[i] != null) {
			oldTypeList[i] = oldGraph.getTypes(oldUnits[i]);
		}
	}

	// Remove old problems and principal structure from new state before
	// storing new results.
	for (int i = 0; i < n; i++) {
		if (oldUnits[i] != null) {
			SourceEntry sEntry = fOldState.getSourceEntry(oldUnits[i]);
			fNewState.getProblemReporter().removeNonSyntaxErrors(sEntry);
		}
		if (oldTypeList[i] != null) {
			IType[] oldTypes = oldTypeList[i];
			for (int j = 0; j < oldTypes.length; ++j) {
				fNewState.getPrincipalStructureTable().remove(oldTypes[j]);
			}
		}
	}
	
	super.updateState(results);

	// now calculate the changes
	for (int i = 0; i < n; i++) {
		PackageElement unit = newUnits[i];
		if (unit == null) {
			// Unit isn't visible. Shouldn't have gotten this far.  Skip it.
			continue;
		}

		/**
		 * Assumption: namespace changes have been dealt with before
		 * compilation.  Compilation units that were removed have already generated
		 * type collaborator indictments.  Here, we are only concerned
		 * with changes within each compilation unit.
		 */

		TypeStructureEntry[] newTSEntries = results[i].getTypes();
		IndictmentSet indictments = new IndictmentSet();
		 
		/* do for each type generated by unit in old state */
		IType[] oldTypes = oldTypeList[i];
		if (oldTypes != null) {
			for (int j = 0; j < oldTypes.length; ++j) {
				IType oldType = oldTypes[j];
				boolean found = false;
				for (int k = 0; k < newTSEntries.length; ++k) {
					if (newTSEntries[k] != null && newTSEntries[k].getType().equals(oldType)) {
						found = true;
						break;
					}
				}
				if (!found) {
					getBuilderType(oldType).computeIndictments(indictments);
				}
			}
		}
		
		/* do for each type in result */
		for (int j = 0; j < newTSEntries.length; j++) {
			TypeStructureEntry newTSEntry = newTSEntries[j];
			if (newTSEntry != null) {
				/* compute the indictments for this type */
				IType type = newTSEntry.getType();
				BuilderType bType = getBuilderType(type);

				/* the new tsEntry wasn't known at compilation time */
				if (bType.getNewTypeStructureEntry() == null) {
					bType.setNewTypeStructureEntry(newTSEntry);
				}
				bType.computeIndictments(indictments);
			}
		}
		if (!indictments.isEmpty()) {
			issueIndictments(unit, indictments, false);
		}
	}
}
}
