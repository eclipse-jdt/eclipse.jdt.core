package org.eclipse.jdt.internal.core.search;

/* 
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;

import org.eclipse.jdt.internal.core.JavaModel;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipFile;

/**
 * Scope limited to the subtype and supertype hierarchy of a given type.
 */
public class HierarchyScope
	extends AbstractSearchScope
	implements IJavaSearchScope {

	private ITypeHierarchy fHierarchy;
	private IType[] fTypes;
	private HashSet resourcePaths = new HashSet();
	private IPath[] enclosingProjectsAndJars;

	/**
	 * Creates a new hiearchy scope for the given type.
	 */
	public HierarchyScope(IType type) throws JavaModelException {
		fHierarchy = type.newTypeHierarchy(null);
		buildResourceVector();
	}
	private void buildResourceVector() throws JavaModelException {
		HashMap resources = new HashMap();
		HashMap paths = new HashMap();
		fTypes = fHierarchy.getAllTypes();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (int i = 0; i < fTypes.length; i++) {
			IType type = fTypes[i];
			IResource resource = type.getUnderlyingResource();
			if (resource != null && resources.get(resource) == null) {
				resources.put(resource, resource);
				add(resource);
			}
			IPackageFragmentRoot root =
				(IPackageFragmentRoot) type.getPackageFragment().getParent();
			if (root instanceof JarPackageFragmentRoot) {
				// type in a jar
				JarPackageFragmentRoot jar = (JarPackageFragmentRoot) root;
				Object target = JavaModel.getTarget(workspaceRoot, jar.getPath(), true);
				String zipFileName;
				if (target instanceof IFile) {
					zipFileName = ((IFile)target).getLocation().toOSString();
				} else if (target instanceof File) {
					zipFileName = ((File)target).getPath();
				} else {
					continue; // unknown target
				}
				String resourcePath =
					zipFileName
						+ JAR_FILE_ENTRY_SEPARATOR
						+ type.getFullyQualifiedName().replace('.', '/')
						+ ".class";//$NON-NLS-1$
				
				this.resourcePaths.add(resourcePath);
				paths.put(jar.getPath(), type);
			} else {
				// type is a project
				paths.put(type.getJavaProject().getProject().getFullPath(), type);
			}
		}
		this.enclosingProjectsAndJars = new IPath[paths.size()];
		int i = 0;
		for (Iterator iter = paths.keySet().iterator(); iter.hasNext();) {
			this.enclosingProjectsAndJars[i++] = (IPath) iter.next();
		}
	}
	/* (non-Javadoc)
	 * @see IJavaSearchScope#encloses(String)
	 */
	public boolean encloses(String resourcePath) {
		int separatorIndex = resourcePath.indexOf(JAR_FILE_ENTRY_SEPARATOR);
		if (separatorIndex != -1) {
			return this.resourcePaths.contains(resourcePath);
		} else {
			for (int i = 0; i < this.elementCount; i++) {
				if (resourcePath.startsWith(this.elements[i].getFullPath().toString())) {
					return true;
				}
			}
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see IJavaSearchScope#encloses(IJavaElement)
	 */
	public boolean encloses(IJavaElement element) {
		if (element instanceof IType) {
			return fHierarchy.contains((IType) element);
		} else if (element instanceof IMember) {
			return fHierarchy.contains(((IMember) element).getDeclaringType());
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see IJavaSearchScope#enclosingProjectsAndJars()
	 * @deprecated
	 */
	public IPath[] enclosingProjectsAndJars() {
		return this.enclosingProjectsAndJars;
	}
	/* (non-Javadoc)
	 * @see IJavaSearchScope#includesBinaries()
	 * @deprecated
	 */
	public boolean includesBinaries() {
		return true;
	}
	/* (non-Javadoc)
	 * @see IJavaSearchScope#includesClasspaths()
	 * @deprecated
	 */
	public boolean includesClasspaths() {
		return true;
	}
	/* (non-Javadoc)
	 * @see IJavaSearchScope#setIncludesBinaries(boolean)
	 * @deprecated
	 */
	public void setIncludesBinaries(boolean includesBinaries) {
	}
	/* (non-Javadoc)
	 * @see IJavaSearchScope#setIncludesClasspaths(boolean)
	 * @deprecated
	 */
	public void setIncludesClasspaths(boolean includesClasspaths) {
	}
}