/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
    JDT UI team - Initial implementation copied from internal code in plugin org.eclipse.jdt.ui
**********************************************************************/
package org.eclipse.jdt.core.util;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * Utility methods for the Java Model.
 */
public class JavaModelUtil {
/**
 * Concatenates two names. Uses a dot for separation.
 * If one of the strings is empty, then the other one is retuned and
 * no dot is append. 
 * <code>null</code> is interpreted as an empty string, so if both strings
 * are <code>null</code>, then an empty string is returned.
 */
public static String concatenateName(String name1, String name2) {
	StringBuffer buf;
	if (name1 == null || name1.length() == 0) {
		return name2 == null ? "" : name2; //$NON-NLS-1$
	} else {
		if (name2 == null || name2.length() == 0) {
			return name1;
		} else {
			return name1 + "." + name2; //$NON-NLS-1$
		}
	}
}

/**
 * Returns the first java element that conforms to the given type walking the
 * java element's parent relationship. If the given element already conforms to
 * the given kind, the element is returned.
 * Returns <code>null</code> if no such element exits.
 */
public static IJavaElement findElementOfKind(IJavaElement element, int kind) {
	while (element != null && element.getElementType() != kind)
		element= element.getParent();
	return element;				
}

/** 
 * Finds the element in the given compilation unit which is equal (up to its compilation unit)
 * to the given element.
 * This is done by replacing in the given java element its compilation unit with the given 
 * compilation unit and checking if it exists.
 * Returns <code>null</code> if the resulting java element does not exist
 * or if the original element is not included in a compilation unit,
 * or if it is not a compilation unit. 
 * 
 * @param cu the cu to search in
 * @param element the element to look for
 * @return an element of the given cu "equal up to its compilation unit" to the given element
 */		
/* disable for now as spec is unclear
public static IJavaElement findInCompilationUnit(ICompilationUnit cu, IJavaElement element) {
	ArrayList children = new ArrayList();
	while (element != null && element.getElementType() != IJavaElement.COMPILATION_UNIT) {
		children.add(element);
		element = element.getParent();
	}
	if (element == null) return null;
	IJavaElement currentElement = cu;
	for (int i = children.size()-1; i >= 0; i--) {
		IJavaElement child = (IJavaElement)children.get(i);
		switch (child.getElementType()) {
			case IJavaElement.PACKAGE_DECLARATION:
				currentElement = ((ICompilationUnit)currentElement).getPackageDeclaration(child.getElementName());
				break;
			case IJavaElement.IMPORT_CONTAINER:
				currentElement = ((ICompilationUnit)currentElement).getImportContainer();
				break;
			case IJavaElement.IMPORT_DECLARATION:
				currentElement = ((IImportContainer)currentElement).getImport(child.getElementName());
				break;
			case IJavaElement.TYPE:
				if (currentElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
					currentElement = ((ICompilationUnit)currentElement).getType(child.getElementName());
				} else {
					currentElement = ((IType)currentElement).getType(child.getElementName());
				}
				break;
			case IJavaElement.INITIALIZER:
				currentElement = ((IType)currentElement).getInitializer(((JavaElement)child).getOccurrenceCount());
				break;
			case IJavaElement.FIELD:
				currentElement = ((IType)currentElement).getField(child.getElementName());
				break;
			case IJavaElement.METHOD:
				IMethod method = (IMethod)child;
				currentElement = ((IType)currentElement).getMethod(method.getElementName(), method.getParameterTypes());
				break;
		}
		
	}
	if (currentElement.exists()) {
		return currentElement;
	} else {
		return null;
	}
} */

/**
 * Returns the parent of the supplied java element that conforms to the given 
 * parent type or <code>null</code>, if such a parent doesn't exit.
 */
public static IJavaElement findParentOfKind(IJavaElement element, int kind) {
	if (element == null)
		return null;
	return findElementOfKind(element.getParent(), kind);	
}

/**
 * Gets the primary type of a compilation unit (type with the same name as the
 * compilation unit), or <code>null</code> if not existing.
 */
public static IType findPrimaryType(ICompilationUnit cu) {
	String typeName= Signature.getQualifier(cu.getElementName());
	IType primaryType= cu.getType(typeName);
	if (primaryType.exists()) {
		return primaryType;
	}
	return null;
}

/**
 * Returns the first openable parent. If the given element is openable, the element
 * itself is returned.
 */
public static IOpenable getOpenable(IJavaElement element) {
	while (element != null && !(element instanceof IOpenable)) {
		element = element.getParent();
	}
	return (IOpenable) element;	
}

/**
 * Returns the package fragment root of the given java element. If it is already 
 * a package fragment root, the element itself is returned.
 * Returns <code>null</code> if no package fragment root is found.
 */
public static IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
	return (IPackageFragmentRoot)findElementOfKind(element, IJavaElement.PACKAGE_FRAGMENT_ROOT);
}

/**
 * Returns whether the given element is on the build path of the given project.
 * 
 * @exception JavaModelException if the given project or given element do not exist or if an
 *		exception occurs while accessing their corresponding resources.
 */	
public static boolean isOnBuildPath(IJavaProject jproject, IJavaElement element) throws JavaModelException {
	IPath rootPath;
	if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
		rootPath= ((IJavaProject)element).getProject().getFullPath();
	} else {
		IPackageFragmentRoot root= getPackageFragmentRoot(element);
		if (root == null) {
			return false;
		}
		rootPath= root.getPath();
	}
	return jproject.findPackageFragmentRoot(rootPath) != null;
}
}
