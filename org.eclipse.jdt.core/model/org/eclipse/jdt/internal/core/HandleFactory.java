package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.index.impl.*;

/**
 * Creates java element handles.
 */
public class HandleFactory {

	private JavaModelManager manager;

	/**
	 * Cache package fragment root information to optimize speed performance.
	 */
	private String lastPkgFragmentRootPath;
	private IPackageFragmentRoot lastPkgFragmentRoot;

	/**
	 * Cache package handles to optimize memory.
	 */
	private Hashtable packageHandles;

	private IWorkspaceRoot workspaceRoot;

	public HandleFactory(IWorkspaceRoot workspaceRoot, JavaModelManager manager) {
		this.workspaceRoot = workspaceRoot;
		this.manager = manager;
	}

	/**
	 * Creates an Openable handle from the given resource path.
	 * The resource path can be a path to a file in the workbench (eg. /Proj/com/ibm/jdt/core/HandleFactory.java)
	 * or a path to a file in a jar file - it then contains the path to the jar file and the path to the file in the jar
	 * (eg. c:/jdk1.2.2/jre/lib/rt.jar|java/lang/Object.class or c:/workbench/Proj/rt.jar|java/lang/Object.class)
	 * NOTE: This assumes that the resource path is the toString() of an IPath, 
	 *       i.e. it uses the IPath.SEPARATOR for file path
	 *            and it uses '/' for entries in a zip file.
	 */
	public Openable createOpenable(String resourcePath) {
		int separatorIndex;
		if ((separatorIndex =
			resourcePath.indexOf(JarFileEntryDocument.JAR_FILE_ENTRY_SEPARATOR))
			> -1) {
			// path to a class file inside a jar
			String jarPath = resourcePath.substring(0, separatorIndex);

			// Optimization: cache package fragment root handle and package handles
			if (!jarPath.equals(this.lastPkgFragmentRootPath)) {
				this.lastPkgFragmentRootPath = jarPath;
				this.lastPkgFragmentRoot = this.getJarPkgFragmentRoot(jarPath);
				this.packageHandles = new Hashtable(5);
			}

			// create handle
			String classFilePath = resourcePath.substring(separatorIndex + 1);
			int lastSlash = classFilePath.lastIndexOf('/');
			String packageName =
				lastSlash > -1
					? classFilePath.substring(0, lastSlash).replace('/', '.')
					: IPackageFragment.DEFAULT_PACKAGE_NAME;
			IPackageFragment pkgFragment =
				(IPackageFragment) this.packageHandles.get(packageName);
			if (pkgFragment == null) {
				pkgFragment = this.lastPkgFragmentRoot.getPackageFragment(packageName);
				this.packageHandles.put(packageName, pkgFragment);
			}
			IClassFile classFile =
				pkgFragment.getClassFile(classFilePath.substring(lastSlash + 1));
			return (Openable) classFile;
		} else {
			// path to a file in a directory

			// Optimization: cache package fragment root handle and package handles
			int length = -1;
			if (this.lastPkgFragmentRootPath == null
				|| !(resourcePath.startsWith(this.lastPkgFragmentRootPath)
					&& (length = this.lastPkgFragmentRootPath.length()) > 0
					&& resourcePath.charAt(length) == '/')) {

				IPackageFragmentRoot root = this.getPkgFragmentRoot(resourcePath);
				if (root == null)
					return null; // match is outside classpath
				this.lastPkgFragmentRoot = root;
				this.lastPkgFragmentRootPath = this.lastPkgFragmentRoot.getPath().toString();
				this.packageHandles = new Hashtable(5);
			}

			// create handle
			int lastSlash = resourcePath.lastIndexOf(IPath.SEPARATOR);
			String packageName =
				lastSlash > (length = this.lastPkgFragmentRootPath.length())
					? resourcePath.substring(length + 1, lastSlash).replace(IPath.SEPARATOR, '.')
					: IPackageFragment.DEFAULT_PACKAGE_NAME;
			IPackageFragment pkgFragment =
				(IPackageFragment) this.packageHandles.get(packageName);
			if (pkgFragment == null) {
				pkgFragment = this.lastPkgFragmentRoot.getPackageFragment(packageName);
				this.packageHandles.put(packageName, pkgFragment);
			}
			String simpleName = resourcePath.substring(lastSlash + 1);
			if (simpleName.endsWith(".java")) {
				ICompilationUnit unit = pkgFragment.getCompilationUnit(simpleName);
				return (Openable) unit;
			} else {
				IClassFile classFile = pkgFragment.getClassFile(simpleName);
				return (Openable) classFile;
			}
		}
	}

	/**
	 * Returns the package fragment root that corresponds to the given jar path.
	 * See createOpenable(...) for the format of the jar path string.
	 */
	private IPackageFragmentRoot getJarPkgFragmentRoot(String jarPathString) {

		IPath jarPath = new Path(jarPathString);
		JavaModel javaModel =
			this.manager.getJavaModel(this.workspaceRoot.getWorkspace());
		IResource jarFile = this.workspaceRoot.findMember(jarPath);
		if (jarFile != null) {
			// internal jar
			return javaModel.getJavaProject(jarFile).getPackageFragmentRoot(jarFile);
		} else {
			// external jar: walk all projects and find the first one that has the given jar path in its classpath
			try {
				IProject[] projects = this.workspaceRoot.getProjects();
				for (int i = 0, projectCount = projects.length; i < projectCount; i++) {
					IProject project = projects[i];
					if (project.isAccessible()) {
						IJavaProject javaProject = javaModel.getJavaProject(project);
						IClasspathEntry[] classpathEntries = javaProject.getResolvedClasspath(true);
						for (int j = 0, entryCount = classpathEntries.length; j < entryCount; j++) {
							if (classpathEntries[j].getPath().equals(jarPath)) {
								return javaProject.getPackageFragmentRoot(jarPathString);
							}
						}
					}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * Returns the package fragment root that contains the given resource path.
	 */
	private IPackageFragmentRoot getPkgFragmentRoot(String pathString) {

		IPath path = new Path(pathString);
		try {
			JavaModel javaModel =
				this.manager.getJavaModel(this.workspaceRoot.getWorkspace());
			IProject[] projects = this.workspaceRoot.getProjects();
			for (int i = 0, max = projects.length; i < max; i++) {
				IJavaProject javaProject = javaModel.getJavaProject(projects[i]);
				IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
				for (int j = 0, rootCount = roots.length; j < rootCount; j++) {
					IPackageFragmentRoot root = roots[j];
					if (root.getPath().isPrefixOf(path)) {
						return root;
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}

}
