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
	private Hashtable resourcePaths = new Hashtable();
	private IPath[] enclosingProjectsAndJars;

	/**
	 * Creates a new hiearchy scope for the given type.
	 */
	public HierarchyScope(IType type) throws JavaModelException {
		fHierarchy = type.newTypeHierarchy(null);
		buildResourceVector();
	}

	private void buildResourceVector() throws JavaModelException {
		Hashtable resources = new Hashtable();
		Hashtable paths = new Hashtable();
		fTypes = fHierarchy.getAllTypes();
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
				String zipFileName;
				ZipFile zipFile = null;
				try {
					zipFile = jar.getJar();
					zipFileName = zipFile.getName();
				} catch (CoreException e) {
					throw new JavaModelException(e);
				} finally {
					if (zipFile != null) {
						try {
							zipFile.close();
						} catch (IOException e) {
							// ignore 
						}
					}
				}
				String resourcePath =
					zipFileName
						+ JAR_FILE_ENTRY_SEPARATOR
						+ type.getFullyQualifiedName().replace('.', '/')
						+ ".class";
				this.resourcePaths.put(resourcePath, resourcePath);
				paths.put(jar.getPath(), type);
			} else {
				// type is a project
				paths.put(type.getJavaProject().getProject().getFullPath(), type);
			}
		}
		this.enclosingProjectsAndJars = new IPath[paths.size()];
		int i = 0;
		for (Enumeration e = paths.keys(); e.hasMoreElements();) {
			this.enclosingProjectsAndJars[i++] = (IPath) e.nextElement();
		}
	}

	/* (non-Javadoc)
	 * @see IJavaSearchScope#encloses(String)
	 */
	public boolean encloses(String resourcePath) {
		int separatorIndex = resourcePath.indexOf(JAR_FILE_ENTRY_SEPARATOR);
		if (separatorIndex != -1) {
			return this.resourcePaths.get(resourcePath) != null;
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
		} else
			if (element instanceof IMember) {
				return fHierarchy.contains(((IMember) element).getDeclaringType());
			}
		return false;
	}

	/* (non-Javadoc)
	 * @see IJavaSearchScope#enclosingProjectsAndJars()
	 */
	public IPath[] enclosingProjectsAndJars() {
		return this.enclosingProjectsAndJars;
	}

	/* (non-Javadoc)
	 * @see IJavaSearchScope#includesBinaries()
	 */
	public boolean includesBinaries() {
		return true;
	}

	/* (non-Javadoc)
	 * @see IJavaSearchScope#includesClasspaths()
	 */
	public boolean includesClasspaths() {
		return true;
	}

	/* (non-Javadoc)
	 * @see IJavaSearchScope#setIncludesBinaries(boolean)
	 */
	public void setIncludesBinaries(boolean includesBinaries) {
	}

	/* (non-Javadoc)
	 * @see IJavaSearchScope#setIncludesClasspaths(boolean)
	 */
	public void setIncludesClasspaths(boolean includesClasspaths) {
	}

}
