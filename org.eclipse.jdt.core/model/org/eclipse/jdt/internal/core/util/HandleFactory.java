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
package org.eclipse.jdt.internal.core.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.index.impl.JarFileEntryDocument;

/**
 * Creates java element handles.
 */
public class HandleFactory {

	/**
	 * Cache package fragment root information to optimize speed performance.
	 */
	private String lastPkgFragmentRootPath;
	private IPackageFragmentRoot lastPkgFragmentRoot;

	/**
	 * Cache package handles to optimize memory.
	 */
	private Map packageHandles;

	private JavaModel javaModel;

	public HandleFactory() {
		this.javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
	}
	
	/**
	 * Creates an Openable handle from the given resource path.
	 * The resource path can be a path to a file in the workbench (eg. /Proj/com/ibm/jdt/core/HandleFactory.java)
	 * or a path to a file in a jar file - it then contains the path to the jar file and the path to the file in the jar
	 * (eg. c:/jdk1.2.2/jre/lib/rt.jar|java/lang/Object.class or /Proj/rt.jar|java/lang/Object.class)
	 * NOTE: This assumes that the resource path is the toString() of an IPath, 
	 *       in other words, it uses the IPath.SEPARATOR for file path
	 *            and it uses '/' for entries in a zip file.
	 * If not null, uses the given scope as a hint for getting Java project handles.
	 */
	public Openable createOpenable(String resourcePath, IJavaSearchScope scope) {
		int separatorIndex;
		if ((separatorIndex= resourcePath.indexOf(JarFileEntryDocument.JAR_FILE_ENTRY_SEPARATOR)) > -1) {
			// path to a class file inside a jar
			String jarPath= resourcePath.substring(0, separatorIndex);
			// Optimization: cache package fragment root handle and package handles
			if (!jarPath.equals(this.lastPkgFragmentRootPath)) {
				IPackageFragmentRoot root= this.getJarPkgFragmentRoot(jarPath, scope);
				if (root == null)
					return null; // match is outside classpath
				this.lastPkgFragmentRootPath= jarPath;
				this.lastPkgFragmentRoot= root;
				this.packageHandles= new HashMap(5);
			}
			// create handle
			String classFilePath= resourcePath.substring(separatorIndex + 1);
			int lastSlash= classFilePath.lastIndexOf('/');
			String packageName= lastSlash > -1 ? classFilePath.substring(0, lastSlash).replace('/', '.') : IPackageFragment.DEFAULT_PACKAGE_NAME;
			IPackageFragment pkgFragment= (IPackageFragment) this.packageHandles.get(packageName);
			if (pkgFragment == null) {
				pkgFragment= this.lastPkgFragmentRoot.getPackageFragment(packageName);
				this.packageHandles.put(packageName, pkgFragment);
			}
			IClassFile classFile= pkgFragment.getClassFile(classFilePath.substring(lastSlash + 1));
			return (Openable) classFile;
		} else {
			// path to a file in a directory
			// Optimization: cache package fragment root handle and package handles
			int length = -1;
			if (this.lastPkgFragmentRootPath == null 
				|| !(resourcePath.startsWith(this.lastPkgFragmentRootPath) 
					&& (length = this.lastPkgFragmentRootPath.length()) > 0
					&& resourcePath.charAt(length) == '/')) {
				IPackageFragmentRoot root= this.getPkgFragmentRoot(resourcePath);
				if (root == null)
					return null; // match is outside classpath
				this.lastPkgFragmentRoot= root;
				this.lastPkgFragmentRootPath= this.lastPkgFragmentRoot.getPath().toString();
				this.packageHandles= new HashMap(5);
			}
			// create handle
			int lastSlash= resourcePath.lastIndexOf(IPath.SEPARATOR);
			String packageName= lastSlash > (length= this.lastPkgFragmentRootPath.length()) ? resourcePath.substring(length + 1, lastSlash).replace(IPath.SEPARATOR, '.') : IPackageFragment.DEFAULT_PACKAGE_NAME;
			IPackageFragment pkgFragment= (IPackageFragment) this.packageHandles.get(packageName);
			if (pkgFragment == null) {
				pkgFragment= this.lastPkgFragmentRoot.getPackageFragment(packageName);
				this.packageHandles.put(packageName, pkgFragment);
			}
			String simpleName= resourcePath.substring(lastSlash + 1);
			if (org.eclipse.jdt.internal.core.Util.isJavaFileName(simpleName)) {
				ICompilationUnit unit= pkgFragment.getCompilationUnit(simpleName);
				return (Openable) unit;
			} else {
				IClassFile classFile= pkgFragment.getClassFile(simpleName);
				return (Openable) classFile;
			}
		}
	}
	
	/*
	 * Returns an element handle corresponding to the given AstNode in the given parsed unit.
	 * Returns null if the given AstNode could not be found.
	 */
	public IJavaElement createElement(final AstNode toBeFound, CompilationUnitDeclaration parsedUnit, final Openable openable) {
		class EndVisit extends RuntimeException {
			// marker to stop traversing ast
		}
		class Visitor extends AbstractSyntaxTreeVisitorAdapter {
			IJavaElement currentElement = openable;
			HashSet knownElements = new HashSet();
			
			public boolean visit(AnonymousLocalTypeDeclaration node, BlockScope scope) {
				currentElement = updateOccurenceCount(((IMember)currentElement).getType("", 1)); //$NON-NLS-1$
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(AnonymousLocalTypeDeclaration node, BlockScope scope) {
				currentElement = currentElement.getParent();
			}
			public boolean visit(ConstructorDeclaration node, ClassScope scope) {
				currentElement = ((IType)currentElement).getMethod(currentElement.getElementName(), getParameterTypeSignatures(node));
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(ConstructorDeclaration node, ClassScope scope) {
				currentElement = currentElement.getParent();
			}

			public boolean visit(FieldDeclaration node, MethodScope scope) {
				currentElement = ((IType)currentElement).getField(currentElement.getElementName());
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(FieldDeclaration node, MethodScope scope) {
				currentElement = currentElement.getParent();
			}

			public boolean visit(Initializer node, MethodScope scope) {
				currentElement = updateOccurenceCount(((IType)currentElement).getInitializer(1));
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(Initializer node, MethodScope scope) {
				currentElement = currentElement.getParent();
			}

			public boolean visit(LocalTypeDeclaration node, BlockScope scope) {
				currentElement = updateOccurenceCount(((IMember)currentElement).getType(new String(node.name), 1));
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(LocalTypeDeclaration node, BlockScope scope) {
				currentElement = currentElement.getParent();
			}

			public boolean visit(MemberTypeDeclaration node, ClassScope scope) {
				currentElement = ((IType)currentElement).getType(new String(node.name));
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(MemberTypeDeclaration node, ClassScope scope) {
				currentElement = currentElement.getParent();
			}

			public boolean visit(MethodDeclaration node, ClassScope scope) {
				currentElement = ((IType)currentElement).getMethod(new String(node.selector), getParameterTypeSignatures(node));
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(MethodDeclaration node, ClassScope scope) {
				currentElement = currentElement.getParent();
			}

			public boolean visit(TypeDeclaration node, CompilationUnitScope scope) {
				if (currentElement instanceof ICompilationUnit) {
					currentElement = ((ICompilationUnit)currentElement).getType(new String(node.name));
				} else {
					try {
						currentElement = ((IClassFile)currentElement).getType();
					} catch (JavaModelException e) {
						// class file doesn't exit: ignore
					}
				}
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(TypeDeclaration node, CompilationUnitScope scope) {
				currentElement = currentElement.getParent();
			}

			private String[] getParameterTypeSignatures(AbstractMethodDeclaration method) {
				Argument[] args = method.arguments;
				if (args != null) {
					int length = args.length;
					String[] signatures = new String[length];
					for (int i = 0; i < args.length; i++) {
						Argument arg = args[i];
						signatures[i] = org.eclipse.jdt.internal.core.Util.typeSignature(arg.type);
					}
					return signatures;
				}
				return new String[0];
			}
			private IJavaElement updateOccurenceCount(IJavaElement element) {
				while (knownElements.contains(element)) {
					((JavaElement)element).occurrenceCount++;
				}
				knownElements.add(element);
				return element;
			}
		}
		Visitor visitor = new Visitor();
		try {
			parsedUnit.traverse(visitor, parsedUnit.scope);
		} catch (EndVisit e) {
			return visitor.currentElement;
		}
		return null;
	}

	/**
	 * Returns the package fragment root that corresponds to the given jar path.
	 * See createOpenable(...) for the format of the jar path string.
	 * If not null, uses the given scope as a hint for getting Java project handles.
	 */
	private IPackageFragmentRoot getJarPkgFragmentRoot(String jarPathString, IJavaSearchScope scope) {

		IPath jarPath= new Path(jarPathString);
		
		Object target = JavaModel.getTarget(ResourcesPlugin.getWorkspace().getRoot(), jarPath, false);
		if (target instanceof IFile) {
			// internal jar: is it on the classpath of its project?
			//  e.g. org.eclipse.swt.win32/ws/win32/swt.jar 
			//        is NOT on the classpath of org.eclipse.swt.win32
			IFile jarFile = (IFile)target;
			IJavaProject javaProject = this.javaModel.getJavaProject(jarFile);
			IClasspathEntry[] classpathEntries;
			try {
				classpathEntries = javaProject.getResolvedClasspath(true);
				for (int j= 0, entryCount= classpathEntries.length; j < entryCount; j++) {
					if (classpathEntries[j].getPath().equals(jarPath)) {
						return javaProject.getPackageFragmentRoot(jarFile);
					}
				}
			} catch (JavaModelException e) {
				// ignore and try to find another project
			}
		}
		
		// walk projects in the scope and find the first one that has the given jar path in its classpath
		IJavaProject[] projects;
		if (scope != null) {
			IPath[] enclosingProjectsAndJars = scope.enclosingProjectsAndJars();
			int length = enclosingProjectsAndJars.length;
			projects = new IJavaProject[length];
			int index = 0;
			for (int i = 0; i < length; i++) {
				IPath path = enclosingProjectsAndJars[i];
				if (!org.eclipse.jdt.internal.core.Util.isArchiveFileName(path.lastSegment())) {
					projects[index++] = this.javaModel.getJavaProject(path.segment(0));
				}
			}
			if (index < length) {
				System.arraycopy(projects, 0, projects = new IJavaProject[index], 0, index);
			}
			IPackageFragmentRoot root = getJarPkgFragmentRoot(jarPath, target, projects);
			if (root != null) {
				return root;
			}
		} 
		
		// not found in the scope, walk all projects
		try {
			projects = this.javaModel.getJavaProjects();
		} catch (JavaModelException e) {
			// java model is not accessible
			return null;
		}
		return getJarPkgFragmentRoot(jarPath, target, projects);
	}
	
	private IPackageFragmentRoot getJarPkgFragmentRoot(
		IPath jarPath,
		Object target,
		IJavaProject[] projects) {
		for (int i= 0, projectCount= projects.length; i < projectCount; i++) {
			try {
				JavaProject javaProject= (JavaProject)projects[i];
				IClasspathEntry[] classpathEntries= javaProject.getResolvedClasspath(true);
				for (int j= 0, entryCount= classpathEntries.length; j < entryCount; j++) {
					if (classpathEntries[j].getPath().equals(jarPath)) {
						if (target instanceof IFile) {
							// internal jar
							return javaProject.getPackageFragmentRoot((IFile)target);
						} else {
							// external jar
							return javaProject.getPackageFragmentRoot0(jarPath);
						}
					}
				}
			} catch (JavaModelException e) {
				// JavaModelException from getResolvedClasspath - a problem occured while accessing project: nothing we can do, ignore
			}
		}
		return null;
	}

	/**
	 * Returns the package fragment root that contains the given resource path.
	 */
	private IPackageFragmentRoot getPkgFragmentRoot(String pathString) {

		IPath path= new Path(pathString);
		IProject[] projects= ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i= 0, max= projects.length; i < max; i++) {
			try {
				IProject project = projects[i];
				if (!project.isAccessible() 
					|| !project.hasNature(JavaCore.NATURE_ID)) continue;
				IJavaProject javaProject= this.javaModel.getJavaProject(project);
				IPackageFragmentRoot[] roots= javaProject.getPackageFragmentRoots();
				for (int j= 0, rootCount= roots.length; j < rootCount; j++) {
					PackageFragmentRoot root= (PackageFragmentRoot)roots[j];
					if (root.getPath().isPrefixOf(path) && !org.eclipse.jdt.internal.core.Util.isExcluded(path, root.fullExclusionPatternChars())) {
						return root;
					}
				}
			} catch (CoreException e) {
				// CoreException from hasNature - should not happen since we check that the project is accessible
				// JavaModelException from getPackageFragmentRoots - a problem occured while accessing project: nothing we can do, ignore
			}
		}
		return null;
	}
}
