/*******************************************************************************
 * Copyright (c) 2003 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.util;

import java.util.Comparator;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.core.SortElementsOperation;

/**
 * @since 2.1
 */
public class JavaCompilationUnitSorter {

	public static final String SOURCE_START = "sourceStart"; //$NON-NLS-1$

	public static int getCategory(ASTNode node, Hashtable options) {
		switch(node.getNodeType()) {
			case ASTNode.METHOD_DECLARATION :
				MethodDeclaration methodDeclaration = (MethodDeclaration) node;
				if (methodDeclaration.isConstructor()) {
					return Integer.parseInt((String)options.get(JavaCore.SORTING_CONSTRUCTOR_ORDER));
				}
				if (Flags.isStatic(methodDeclaration.getModifiers())) {
					return Integer.parseInt((String)options.get(JavaCore.SORTING_STATIC_METHOD_ORDER));
				} else {
					return Integer.parseInt((String)options.get(JavaCore.SORTING_METHOD_ORDER));
				}
			case ASTNode.FIELD_DECLARATION :
				FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
				if (Flags.isStatic(fieldDeclaration.getModifiers())) {
					return Integer.parseInt((String)options.get(JavaCore.SORTING_STATIC_FIELD_ORDER));
				} else {
					return Integer.parseInt((String)options.get(JavaCore.SORTING_FIELD_ORDER));
				}
			case ASTNode.TYPE_DECLARATION :
				return Integer.parseInt((String)options.get(JavaCore.SORTING_TYPE_ORDER));
			case ASTNode.INITIALIZER :
				Initializer initializer = (Initializer) node;
				if (Flags.isStatic(initializer.getModifiers())) {
					return Integer.parseInt((String)options.get(JavaCore.SORTING_STATIC_INITIALIZER_ORDER));
				} else {
					return Integer.parseInt((String)options.get(JavaCore.SORTING_INITIALIZER_ORDER));
				}
		}
		return 0;
	}

	public static void sort(ICompilationUnit[] compilationUnits, Comparator comparator, IProgressMonitor monitor) throws CoreException {
		if (comparator == null || compilationUnits == null) {
			return;
		}
		SortElementsOperation operation = new SortElementsOperation(compilationUnits , comparator);
		operation.run(monitor);	
	}
}
