/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.hierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.Openable;

public class RegionBasedHierarchyBuilder extends HierarchyBuilder {
	
	public RegionBasedHierarchyBuilder(TypeHierarchy hierarchy)
		throws JavaModelException {
			
		super(hierarchy);
	}
	
public void build(boolean computeSubtypes) {
		
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	try {
		// optimize access to zip files while building hierarchy
		manager.cacheZipFiles();
				
		if (this.hierarchy.focusType == null || computeSubtypes) {
			IProgressMonitor typeInRegionMonitor = 
				this.hierarchy.progressMonitor == null ? 
					null : 
					new SubProgressMonitor(this.hierarchy.progressMonitor, 30);
			ArrayList allTypesInRegion = determineTypesInRegion(typeInRegionMonitor);
			this.hierarchy.initialize(allTypesInRegion.size());
			IProgressMonitor buildMonitor = 
				this.hierarchy.progressMonitor == null ? 
					null : 
					new SubProgressMonitor(this.hierarchy.progressMonitor, 70);
			createTypeHierarchyBasedOnRegion(allTypesInRegion, buildMonitor);
			((RegionBasedTypeHierarchy)this.hierarchy).pruneDeadBranches();
		} else {
			this.hierarchy.initialize(1);
			this.buildSupertypes();
		}
	} finally {
		manager.flushZipFiles();
	}
}
/**
 * Configure this type hierarchy that is based on a region.
 */
private void createTypeHierarchyBasedOnRegion(ArrayList allTypesInRegion, IProgressMonitor monitor) {
	
	int size = allTypesInRegion.size();
	if (size != 0) {
		this.infoToHandle = new HashMap(size);
	}
	HashSet existingOpenables = new HashSet(size);
	Openable[] openables = new Openable[size];
	int openableIndex = 0;
	for (int i = 0; i < size; i++) {
		IType type = (IType)allTypesInRegion.get(i);
		Openable openable;
		if (type.isBinary()) {
			openable = (Openable)type.getClassFile();
		} else {
			openable = (Openable)type.getCompilationUnit();
		}
		if (existingOpenables.add(openable)) {
			openables[openableIndex++] = openable;
		}
	}
	if (openableIndex < size) {
		System.arraycopy(openables, 0, openables = new Openable[openableIndex], 0, openableIndex);
	}

	try {
		// resolve
		if (monitor != null) monitor.beginTask("", openableIndex * 2/* 1 for build binding, 1 for connect hierarchy*/); //$NON-NLS-1$
		if (openableIndex > 0) {
			IType focusType = this.getType();
			CompilationUnit unitToLookInside = null;
			if (focusType != null) {
				unitToLookInside = (CompilationUnit)focusType.getCompilationUnit();
			}
			if (this.nameLookup != null && unitToLookInside != null) {
				try {
					nameLookup.setUnitsToLookInside(new ICompilationUnit[] {unitToLookInside}); // NB: this uses a PerThreadObject, so it is thread safe
					this.hierarchyResolver.resolve(openables, null, monitor);
				} finally {
					nameLookup.setUnitsToLookInside(null);
				}
			} else {
				this.hierarchyResolver.resolve(openables, null, monitor);
			}
		}
	} finally {
		if (monitor != null) monitor.done();
	}
}
	
	/**
	 * Returns all of the types defined in the region of this type hierarchy.
	 */
	private ArrayList determineTypesInRegion(IProgressMonitor monitor) {

		try {
			ArrayList types = new ArrayList();
			IJavaElement[] roots =
				((RegionBasedTypeHierarchy) this.hierarchy).region.getElements();
			int length = roots.length;
			if (monitor != null) monitor.beginTask("", length); //$NON-NLS-1$
			for (int i = 0; i <length; i++) {
				try {
					IJavaElement root = roots[i];
					switch (root.getElementType()) {
						case IJavaElement.JAVA_PROJECT :
							injectAllTypesForJavaProject((IJavaProject) root, types);
							break;
						case IJavaElement.PACKAGE_FRAGMENT_ROOT :
							injectAllTypesForPackageFragmentRoot((IPackageFragmentRoot) root, types);
							break;
						case IJavaElement.PACKAGE_FRAGMENT :
							injectAllTypesForPackageFragment((IPackageFragment) root, types);
							break;
						case IJavaElement.CLASS_FILE :
							types.add(((IClassFile) root).getType());
							break;
						case IJavaElement.COMPILATION_UNIT :
							IType[] cuTypes = ((ICompilationUnit) root).getAllTypes();
							for (int j = 0; j < cuTypes.length; j++) {
								types.add(cuTypes[j]);
							}
							break;
						case IJavaElement.TYPE :
							types.add(root);
							break;
						default :
							break;
					}
				} catch (JavaModelException e) {
					// just continue
				}
				worked(monitor, 1);
			}
			return types;
		} finally {
			if (monitor != null) monitor.done();
		}
	}
	
	/**
	 * Adds all of the types defined within this java project to the
	 * list.
	 */
	private void injectAllTypesForJavaProject(
		IJavaProject project,
		ArrayList types) {
		try {
			IPackageFragmentRoot[] devPathRoots =
				((JavaProject) project).getPackageFragmentRoots();
			if (devPathRoots == null) {
				return;
			}
			for (int j = 0; j < devPathRoots.length; j++) {
				IPackageFragmentRoot root = devPathRoots[j];
				injectAllTypesForPackageFragmentRoot(root, types);
			}
		} catch (JavaModelException e) {
			// ignore
		}
	}
	
	/**
	 * Adds all of the types defined within this package fragment to the
	 * list.
	 */
	private void injectAllTypesForPackageFragment(
		IPackageFragment packFrag,
		ArrayList types) {
			
		try {
			IPackageFragmentRoot root = (IPackageFragmentRoot) packFrag.getParent();
			int kind = root.getKind();
			if (kind != 0) {
				boolean isSourcePackageFragment = (kind == IPackageFragmentRoot.K_SOURCE);
				if (isSourcePackageFragment) {
					ICompilationUnit[] typeContainers = packFrag.getCompilationUnits();
					injectAllTypesForTypeContainers(typeContainers, types);
				} else {
					IClassFile[] typeContainers = packFrag.getClassFiles();
					injectAllTypesForTypeContainers(typeContainers, types);
				}
			}
		} catch (JavaModelException e) {
			// ignore
		}
	}
	
	/**
	 * Adds all of the types defined within this package fragment root to the
	 * list.
	 */
	private void injectAllTypesForPackageFragmentRoot(
		IPackageFragmentRoot root,
		ArrayList types) {
		try {
			IJavaElement[] packFrags = root.getChildren();
			for (int k = 0; k < packFrags.length; k++) {
				IPackageFragment packFrag = (IPackageFragment) packFrags[k];
				injectAllTypesForPackageFragment(packFrag, types);
			}
		} catch (JavaModelException e) {
			return;
		}
	}
	
	/**
	 * Adds all of the types defined within the type containers (IClassFile).
	 */
	private void injectAllTypesForTypeContainers(
		IClassFile[] containers,
		ArrayList types) {
			
		try {
			for (int i = 0; i < containers.length; i++) {
				IClassFile cf = containers[i];
				types.add(cf.getType());
			}
		} catch (JavaModelException e) {
			// ignore
		}
	}
	
	/**
	 * Adds all of the types defined within the type containers (ICompilationUnit).
	 */
	private void injectAllTypesForTypeContainers(
		ICompilationUnit[] containers,
		ArrayList types) {
			
		try {
			for (int i = 0; i < containers.length; i++) {
				ICompilationUnit cu = containers[i];
				IType[] cuTypes = cu.getAllTypes();
				for (int j = 0; j < cuTypes.length; j++) {
					types.add(cuTypes[j]);
				}
			}
		} catch (JavaModelException e) {
			// ignore
		}
	}
}
