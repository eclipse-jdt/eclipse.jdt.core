package org.eclipse.jdt.internal.core.hierarchy;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.internal.compiler.env.IGenericType;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.*;

import java.util.Hashtable;
import java.util.Vector;

public class RegionBasedHierarchyBuilder extends HierarchyBuilder {
	public RegionBasedHierarchyBuilder(TypeHierarchy hierarchy)
		throws JavaModelException {
		super(hierarchy);
	}

	public void build(boolean computeSubtypes) {
		if (this.hierarchy.fType == null || computeSubtypes) {
			Vector allTypesInRegion = determineTypesInRegion();
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
	private void createTypeHierarchyBasedOnRegion(Vector allTypesInRegion) {
		int size = allTypesInRegion.size();
		if (size != 0) {
			this.infoToHandle = new Hashtable(size);
		}
		Vector temp = new Vector(size);
		types : for (int i = 0; i < size; i++) {
			try {
				IType type = (IType) allTypesInRegion.elementAt(i);
				IGenericType info = (IGenericType) ((JavaElement) type).getRawInfo();
				temp.addElement(info);
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
			temp.copyInto(genericTypes);
			IType focusType = this.getType();
			if (focusType != null) {
				this.searchableEnvironment.unitToLookInside =
					(CompilationUnit) focusType.getCompilationUnit();
			}
			this.hierarchyResolver.resolve(genericTypes);
			if (focusType != null) {
				this.searchableEnvironment.unitToLookInside = null;
			}

		}
	}

	/**
	 * Returns all of the types defined in the region of this type hierarchy.
	 */
	private Vector determineTypesInRegion() {

		Vector types = new Vector();
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
						types.addElement(((IClassFile) root).getType());
						break;
					case IJavaElement.COMPILATION_UNIT :
						IType[] cuTypes = ((ICompilationUnit) root).getAllTypes();
						for (int j = 0; j < cuTypes.length; j++) {
							types.addElement(cuTypes[j]);
						}
						break;
					case IJavaElement.TYPE :
						types.addElement(root);
						break;
					default :
						break;
				}
			} catch (JavaModelException npe) {
				// just continue
			}
		}
		return types;
	}

	/**
	 * Adds all of the types defined within this java project to the
	 * vector.
	 */
	private void injectAllTypesForJavaProject(IJavaProject project, Vector types) {
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
	 * vector.
	 */
	private void injectAllTypesForPackageFragment(
		IPackageFragment packFrag,
		Vector types) {
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
		} catch (JavaModelException npe) {
		}
	}

	/**
	 * Adds all of the types defined within this package fragment root to the
	 * vector.
	 */
	private void injectAllTypesForPackageFragmentRoot(
		IPackageFragmentRoot root,
		Vector types) {
		try {
			IJavaElement[] packFrags = root.getChildren();
			for (int k = 0; k < packFrags.length; k++) {
				IPackageFragment packFrag = (IPackageFragment) packFrags[k];
				injectAllTypesForPackageFragment(packFrag, types);
			}
		} catch (JavaModelException npe) {
			return;
		}
	}

	/**
	 * Adds all of the types defined within the type containers (IClassFile).
	 */
	private void injectAllTypesForTypeContainers(
		IClassFile[] containers,
		Vector types) {
		try {
			for (int i = 0; i < containers.length; i++) {
				IClassFile cf = containers[i];
				types.addElement(cf.getType());
				this.worked(1);
			}
		} catch (JavaModelException npe) {
		}
	}

	/**
	 * Adds all of the types defined within the type containers (ICompilationUnit).
	 */
	private void injectAllTypesForTypeContainers(
		ICompilationUnit[] containers,
		Vector types) {
		try {
			for (int i = 0; i < containers.length; i++) {
				ICompilationUnit cu = containers[i];
				IType[] cuTypes = cu.getAllTypes();
				for (int j = 0; j < cuTypes.length; j++) {
					types.addElement(cuTypes[j]);
				}
				this.worked(1);
			}
		} catch (JavaModelException npe) {
		}
	}

}
