package org.eclipse.jdt.internal.core.hierarchy;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.HashMap;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.internal.compiler.env.IGenericType;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.*;

public class RegionBasedHierarchyBuilder extends HierarchyBuilder {
	
	public RegionBasedHierarchyBuilder(TypeHierarchy hierarchy)
		throws JavaModelException {
			
		super(hierarchy);
	}
	
	public void build(boolean computeSubtypes) {
		
		if (this.hierarchy.type == null || computeSubtypes) {
			ArrayList allTypesInRegion = determineTypesInRegion();
			this.hierarchy.initialize(allTypesInRegion.size());
			createTypeHierarchyBasedOnRegion(allTypesInRegion);
		} else {
			this.hierarchy.initialize(1);
			this.buildSupertypes();
		}
	}
	/**
	 * Configure this type hierarchy that is based on a region.
	 */
	private void createTypeHierarchyBasedOnRegion(ArrayList allTypesInRegion) {
		
		int size = allTypesInRegion.size();
		if (size != 0) {
			this.infoToHandle = new HashMap(size);
		}
		ArrayList temp = new ArrayList(size);
		types : for (int i = 0; i < size; i++) {
			try {
				IType type = (IType) allTypesInRegion.get(i);
				IGenericType info = (IGenericType) ((JavaElement) type).getRawInfo();
				temp.add(info);
				if (info.isBinaryType()) {
					this.infoToHandle.put(info, type.getParent());
				}
				worked(1);
			} catch (JavaModelException npe) {
				continue types;
			}
		}

		size = temp.size();
		if (size > 0) {
			IGenericType[] genericTypes = new IGenericType[size];
			temp.toArray(genericTypes);
			IType focusType = this.getType();
			CompilationUnit unitToLookInside = null;
			if (focusType != null) {
				unitToLookInside = (CompilationUnit)focusType.getCompilationUnit();
			}
			if (this.nameLookup != null && unitToLookInside != null) {
				synchronized(this.nameLookup) { // prevent 2 concurrent accesses to name lookup while the working copies are set
					try {
						nameLookup.setUnitsToLookInside(new IWorkingCopy[] {unitToLookInside});
						this.hierarchyResolver.resolve(genericTypes);
					} finally {
						nameLookup.setUnitsToLookInside(null);
					}
				}
			} else {
				this.hierarchyResolver.resolve(genericTypes);
			}
		}
	}
	
	/**
	 * Returns all of the types defined in the region of this type hierarchy.
	 */
	private ArrayList determineTypesInRegion() {

		ArrayList types = new ArrayList();
		IJavaElement[] roots =
			((RegionBasedTypeHierarchy) this.hierarchy).fRegion.getElements();
		for (int i = 0; i < roots.length; i++) {
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
		}
		return types;
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
				this.worked(1);
			}
		} catch (JavaModelException e) {
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
				this.worked(1);
			}
		} catch (JavaModelException e) {
		}
	}
}