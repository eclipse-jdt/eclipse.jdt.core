/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.util.Util;

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
		if ((separatorIndex= resourcePath.indexOf(IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR)) > -1) {
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
			if (org.eclipse.jdt.internal.compiler.util.Util.isJavaFileName(simpleName)) {
				ICompilationUnit unit= pkgFragment.getCompilationUnit(simpleName);
				return (Openable) unit;
			} else {
				IClassFile classFile= pkgFragment.getClassFile(simpleName);
				return (Openable) classFile;
			}
		}
	}
	
	/*
	 * Returns an element handle corresponding to the given ASTNode in the given parsed unit.
	 * Returns null if the given ASTNode could not be found.
	 */
	public IJavaElement createElement(final ASTNode toBeFound, CompilationUnitDeclaration parsedUnit, Openable openable) {
		class EndVisit extends RuntimeException {
			// marker to stop traversing ast
		}
		class Visitor extends ASTVisitor {
		    ASTNode[] nodeStack = new ASTNode[10];
		    int nodeIndex = -1;
			
		    public void push(ASTNode node) {
		    	if (++this.nodeIndex >= this.nodeStack.length) 
		            System.arraycopy(this.nodeStack, 0, this.nodeStack = new ASTNode[this.nodeStack.length*2], 0, this.nodeIndex-1);
	            this.nodeStack[this.nodeIndex] = node;
		    }
		    
		    public void pop(ASTNode node) {
		    	while (this.nodeIndex >= 0 && this.nodeStack[this.nodeIndex--] != node){/*empty*/}
		    }
		    
			public boolean visit(Argument node, BlockScope scope) {
			    push(node);
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(Argument node, BlockScope scope) {
			    pop(node);
            }

			public boolean visit(ConstructorDeclaration node, ClassScope scope) {
			    push(node);
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(ConstructorDeclaration node, ClassScope scope) {
				pop(node);
			}
			
			public boolean visit(FieldDeclaration node, MethodScope scope) {
			    push(node);
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(FieldDeclaration node, MethodScope scope) {
			    pop(node);
			}

			public boolean visit(Initializer node, MethodScope scope) {
			    push(node);
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			// don't pop initializers (used to count how many occurrences are in the type)

			public boolean visit(LocalDeclaration node, BlockScope scope) {
			    push(node);
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(LocalDeclaration node, BlockScope scope) {
			    pop(node);
            }

			public boolean visit(TypeDeclaration node, BlockScope scope) {
			    push(node);
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(TypeDeclaration node, BlockScope scope) {
				if ((node.bits & ASTNode.IsMemberTypeMASK) != 0) {
				    pop(node);
				}
				// don't pop local/anonymous types (used to count how many occurrences are in the method)
			}

			public boolean visit(TypeDeclaration node, ClassScope scope) {
			    push(node);
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(TypeDeclaration node, ClassScope scope) {
				if ((node.bits & ASTNode.IsMemberTypeMASK) != 0) {
				    pop(node);
				}
				// don't pop local/anonymous types (used to count how many occurrences are in the initializer)
			}

			public boolean visit(MethodDeclaration node, ClassScope scope) {
			    push(node);
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(MethodDeclaration node, ClassScope scope) {
				pop(node);
			}
			
			public boolean visit(TypeDeclaration node, CompilationUnitScope scope) {
			    push(node);
				if (node == toBeFound) throw new EndVisit();
				return true;
			}
			public void endVisit(TypeDeclaration node, CompilationUnitScope scope) {
				pop(node);
			}

		}
		Visitor visitor = new Visitor();
		try {
			parsedUnit.traverse(visitor, parsedUnit.scope);
		} catch (EndVisit e) {
		    ASTNode[] nodeStack = visitor.nodeStack;
		    int end = visitor.nodeIndex;
		    int start = 0;
		    
		    // find the inner most type declaration if binary type
		    ASTNode typeDecl = null;
		    if (openable instanceof ClassFile) {
				for (int i = end; i >= 0; i--) {
				    if (nodeStack[i] instanceof TypeDeclaration) {
				        typeDecl = nodeStack[i];
				        start = i;
				        break;
				    }
				}
		    }
			
			// find the openable corresponding to this type declaration
			if (typeDecl != null) {
			    openable = getOpenable(typeDecl, openable);
			}
			
			return createElement(nodeStack, start, end, openable);
		}
		return null;
	}
	private IJavaElement createElement(ASTNode[] nodeStack, int start, int end, IJavaElement parent) {
		if (start > end) return parent;
        ASTNode node = nodeStack[start];
        IJavaElement element = parent;
		switch(parent.getElementType()) {
	        case IJavaElement.COMPILATION_UNIT:
	            String typeName = new String(((TypeDeclaration)node).name);
	        	element = ((ICompilationUnit)parent).getType(typeName);
	        	break;
	        case IJavaElement.CLASS_FILE:
	            try {
                    element = ((IClassFile)parent).getType();
                } catch (JavaModelException e) {
					// class file doesn't exist: ignore
                }
                break;
            case IJavaElement.TYPE:
                IType type = (IType)parent;
                if (node instanceof ConstructorDeclaration) {
	 				element = type.getMethod(
						parent.getElementName(), 
						Util.typeParameterSignatures((ConstructorDeclaration)node));
				} else if (node instanceof MethodDeclaration) {
				    MethodDeclaration method = (MethodDeclaration)node;
					element = type.getMethod(
						new String(method.selector), 
						Util.typeParameterSignatures(method));
				} else if (node instanceof Initializer) {
				    int occurrenceCount = 1;
				    while (start < end) {
				        if (nodeStack[start+1] instanceof Initializer) {
				            start++;
				        	occurrenceCount++;
				    	} else {
				            break;
				    	}
				    }
				    element = type.getInitializer(occurrenceCount);
                } else if (node instanceof FieldDeclaration) {
                    String fieldName = new String(((FieldDeclaration)node).name);
                    element = type.getField(fieldName);
                } else if (node instanceof TypeDeclaration) {
					typeName = new String(((TypeDeclaration)node).name);
                    element = type.getType(typeName);
                }
                break;
			case IJavaElement.FIELD:
			    IField field = (IField)parent;
			    if (field.isBinary()) {
			        return null;
			    } else {
					// child of a field can only be anonymous type
					element = field.getType("", 1); //$NON-NLS-1$
			    }
				break;
			case IJavaElement.METHOD:
			case IJavaElement.INITIALIZER:
				IMember member = (IMember)parent;
				if (node instanceof TypeDeclaration) {
				    if (member.isBinary()) {
				        return null;
				    } else {
					    int typeIndex = start;
					    while (typeIndex <= end) {
					        ASTNode typeDecl = nodeStack[typeIndex+1];
					        if (typeDecl instanceof TypeDeclaration && (typeDecl.bits & ASTNode.AnonymousAndLocalMask) != 0) {
					            typeIndex++;
					    	} else {
					            break;
					    	}
					    }
					    char[] name = ((TypeDeclaration)nodeStack[typeIndex]).name;
					    int occurrenceCount = 1;
						for (int i = start; i < typeIndex; i++) {
						    if (CharOperation.equals(name, ((TypeDeclaration)nodeStack[i]).name)) {
						        occurrenceCount++;
						    }
						}
						start = typeIndex;
						typeName = (node.bits & ASTNode.IsAnonymousTypeMASK) != 0 ? "" : new String(name); //$NON-NLS-1$
						element = member.getType(typeName, occurrenceCount);
				    }
				} else if (node instanceof LocalDeclaration) {
				    if (start == end) {
					    LocalDeclaration local = (LocalDeclaration)node;
						element = new LocalVariable(
								(JavaElement)parent, 
								new String(local.name), 
								local.declarationSourceStart,
								local.declarationSourceEnd,
								local.sourceStart,
								local.sourceEnd,
								Util.typeSignature(local.type));
				    } // else the next node is an anonymous (initializer of the local variable)
				}
				break;
 	    }
	   return createElement(nodeStack, start+1, end, element);
	}
	/**
	 * Returns a handle denoting the class member identified by its scope.
	 */
	public IJavaElement createElement(ClassScope scope, ICompilationUnit unit, HashSet existingElements, HashMap knownScopes) {
		return createElement(scope, scope.referenceContext.sourceStart, unit, existingElements, knownScopes);
	}
	/**
	 * Create handle by adding child to parent obtained by recursing into parent scopes.
	 */
	private IJavaElement createElement(Scope scope, int elementPosition, ICompilationUnit unit, HashSet existingElements, HashMap knownScopes) {
		IJavaElement newElement = (IJavaElement)knownScopes.get(scope);
		if (newElement != null) return newElement;
	
		switch(scope.kind) {
			case Scope.COMPILATION_UNIT_SCOPE :
				newElement = unit;
				break;			
			case Scope.CLASS_SCOPE :
				IJavaElement parentElement = createElement(scope.parent, elementPosition, unit, existingElements, knownScopes);
				switch (parentElement.getElementType()) {
					case IJavaElement.COMPILATION_UNIT :
						newElement = ((ICompilationUnit)parentElement).getType(new String(scope.enclosingSourceType().sourceName));
						break;						
					case IJavaElement.TYPE :
						newElement = ((IType)parentElement).getType(new String(scope.enclosingSourceType().sourceName));
						break;
					case IJavaElement.FIELD :
					case IJavaElement.INITIALIZER :
					case IJavaElement.METHOD :
					    IMember member = (IMember)parentElement;
					    if (member.isBinary()) {
					        return null;
					    } else {
							newElement = member.getType(new String(scope.enclosingSourceType().sourceName), 1);
							// increment occurrence count if collision is detected
							if (newElement != null) {
								while (!existingElements.add(newElement)) ((SourceRefElement)newElement).occurrenceCount++;
							}
					    }
						break;						
				}
				if (newElement != null) {
					knownScopes.put(scope, newElement);
				}
				break;
			case Scope.METHOD_SCOPE :
				IType parentType = (IType) createElement(scope.parent, elementPosition, unit, existingElements, knownScopes);
				MethodScope methodScope = (MethodScope) scope;
				if (methodScope.isInsideInitializer()) {
					// inside field or initializer, must find proper one
					TypeDeclaration type = methodScope.referenceType();
					int occurenceCount = 1;
					for (int i = 0, length = type.fields.length; i < length; i++) {
						FieldDeclaration field = type.fields[i];
						if (field.declarationSourceStart < elementPosition && field.declarationSourceEnd > elementPosition) {
							if (field.isField()) {
								newElement = parentType.getField(new String(field.name));
							} else {
								newElement = parentType.getInitializer(occurenceCount);
							}
							break;
						} else if (!field.isField()) {
							occurenceCount++;
						}
					}
				} else {
					// method element
					AbstractMethodDeclaration method = methodScope.referenceMethod();
					newElement = parentType.getMethod(new String(method.selector), Util.typeParameterSignatures(method));
					if (newElement != null) {
						knownScopes.put(scope, newElement);
					}
				}
				break;				
			case Scope.BLOCK_SCOPE :
				// standard block, no element per se
				newElement = createElement(scope.parent, elementPosition, unit, existingElements, knownScopes);
				break;
		}
		return newElement;
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
			JavaProject javaProject = (JavaProject) this.javaModel.getJavaProject(jarFile);
			IClasspathEntry[] classpathEntries;
			try {
				classpathEntries = javaProject.getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/);
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
				if (!org.eclipse.jdt.internal.compiler.util.Util.isArchiveFileName(path.lastSegment())) {
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
				IClasspathEntry[] classpathEntries= javaProject.getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/);
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

	/*
	 * Returns the openable that contains the given AST node.
	 */
	private Openable getOpenable(ASTNode toBeFound, Openable openable) {
	    if (openable instanceof ClassFile) {
	        try {
	            int sourceStart = toBeFound.sourceStart;
	            int sourceEnd = toBeFound.sourceEnd;
	            ClassFile classFile = (ClassFile)openable;
                ISourceRange sourceRange = classFile.getType().getSourceRange();
                int offset = sourceRange.getOffset();
                if (offset == sourceStart && offset + sourceRange.getLength() != sourceEnd) {
                    return openable;
                } else {
                    String prefix = classFile.getTopLevelTypeName() + '$';
                    IPackageFragment pkg = (IPackageFragment)classFile.getParent();
                    IClassFile[] children = pkg.getClassFiles();
                    for (int i = 0, length = children.length; i < length; i++) {
                        IClassFile child = children[i];
                        if (child.getElementName().startsWith(prefix)) {
			                sourceRange = child.getType().getSourceRange();
			                offset = sourceRange.getOffset();
                        	if (offset == sourceStart && offset + sourceRange.getLength() != sourceEnd) {
                        	    return (Openable)child;
                        	}
                        }
                    }
                }
            } catch (JavaModelException e) {
                // class file doesn't exist: ignore
            }
	    }
	    return openable;
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
					if (root.getPath().isPrefixOf(path) && !Util.isExcluded(path, root.fullInclusionPatternChars(), root.fullExclusionPatternChars(), false)) {
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
