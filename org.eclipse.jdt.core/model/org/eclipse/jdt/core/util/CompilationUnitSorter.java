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

import java.text.Collator;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.core.SortElementsOperation;
//TODO: (olivier) should explain somewhere why DOM-ASTs are used, with IJavaElement positions
/**
 * @since 2.1
 */
public class CompilationUnitSorter {

	public static class DefaultJavaElementComparator implements Comparator {
	
		Collator collator;
		
		int staticTypeCategory;
		int staticInitializerCategory;
		int staticMethodCategory;
		int staticFieldCategory;
		int typeCategory;
		int initializerCategory;
		int methodCategory;
		int constructorCategory;
		int fieldCategory;
		
		public DefaultJavaElementComparator() {
			// initialize default categories
			this.staticTypeCategory = 1;
			this.staticFieldCategory = 2;
			this.staticInitializerCategory = 3;
			this.staticMethodCategory = 4;
			this.typeCategory = 5;
			this.fieldCategory = 6;
			this.initializerCategory = 7;
			this.constructorCategory = 8;
			this.methodCategory = 9;

			this.collator = Collator.getInstance();
		}

		/**
		 * This method is used to retrieve the category for a body declaration node according to the 
		 * preferences passed at the creation of the comparator.
		 * 
		 * @param node the given node
		 * @return the category corresponding to the given node
		 * 
		 * @since 2.1
		 */
		private int getCategory(BodyDeclaration node) {
			switch(node.getNodeType()) {
				case ASTNode.METHOD_DECLARATION :
					MethodDeclaration methodDeclaration = (MethodDeclaration) node;
					if (methodDeclaration.isConstructor()) {
						return this.constructorCategory;
					}
					if (Flags.isStatic(methodDeclaration.getModifiers())) {
						return this.staticMethodCategory;
					} else {
						return this.methodCategory;
					}
				case ASTNode.FIELD_DECLARATION :
					FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
					if (Flags.isStatic(fieldDeclaration.getModifiers())) {
						return this.staticFieldCategory;
					} else {
						return this.fieldCategory;
					}
				case ASTNode.TYPE_DECLARATION :
					TypeDeclaration typeDeclaration = (TypeDeclaration) node;
					if (Flags.isStatic(typeDeclaration.getModifiers())) {
						return this.staticTypeCategory;
					} else {
						return this.typeCategory;
					}
				case ASTNode.INITIALIZER :
					Initializer initializer = (Initializer) node;
					if (Flags.isStatic(initializer.getModifiers())) {
						return this.staticInitializerCategory;
					} else {
						return this.initializerCategory;
					}
			}
			return 0;
		}

	
		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			if (!(o1 instanceof BodyDeclaration) && !(o2 instanceof BodyDeclaration)) {
				throw new ClassCastException();
			}
			BodyDeclaration node1 = (BodyDeclaration) o1;
			BodyDeclaration node2 = (BodyDeclaration) o2;
			int category1 = getCategory(node1);
			int category2 = getCategory(node2);
			
			if (category1 != category2) {
				return category1 - category2;
			}
			if (o1 == o2) {
				return 0;
			}
			switch(node1.getNodeType()) {
				case ASTNode.METHOD_DECLARATION :
					MethodDeclaration method1 = (MethodDeclaration) node1;
					MethodDeclaration method2 = (MethodDeclaration) node2;
					
					if (method1.isConstructor()) {
						return compareParams(method1, method2);
					}
					int compare = this.collator.compare(method1.getName().getIdentifier(), method2.getName().getIdentifier());
					if (compare != 0) {
						return compare;
					}
					return compareParams(method1, method2);
				case ASTNode.FIELD_DECLARATION :
					FieldDeclaration fieldDeclaration1 = (FieldDeclaration) node1;
					FieldDeclaration fieldDeclaration2 = (FieldDeclaration) node2;
					VariableDeclarationFragment fragment1 = (VariableDeclarationFragment) fieldDeclaration1.fragments().get(0);
					VariableDeclarationFragment fragment2 = (VariableDeclarationFragment) fieldDeclaration2.fragments().get(0);
					return this.collator.compare(fragment1.getName().getIdentifier(), fragment2.getName().getIdentifier());
				case ASTNode.INITIALIZER :
					return ((Integer) node1.getProperty(CompilationUnitSorter.SOURCE_START)).intValue() - ((Integer) node2.getProperty(CompilationUnitSorter.SOURCE_START)).intValue();
				case ASTNode.TYPE_DECLARATION :
					TypeDeclaration typeDeclaration1 = (TypeDeclaration) node1;
					TypeDeclaration typeDeclaration2 = (TypeDeclaration) node2;
					return this.collator.compare(typeDeclaration1.getName().getIdentifier(), typeDeclaration2.getName().getIdentifier());
			}
			return 0;
		}
	
		int compareParams(
			MethodDeclaration method1,
			MethodDeclaration method2) {
			int compare;
			final List parameters1 = method1.parameters();
			final List parameters2 = method2.parameters();
			int length1 = parameters1.size();
			int length2 = parameters2.size();
			int len= Math.min(length1, length2);
			for (int i = 0; i < len; i++) {
				compare = this.collator.compare(((SingleVariableDeclaration) parameters1.get(i)).getName().getIdentifier(), ((SingleVariableDeclaration) parameters2.get(i)).getName().getIdentifier());
				if (compare != 0) {
					return compare;
				}
			}
			return length1 - length2;
		}
	}

	public static final String SOURCE_START = "sourceStart"; //$NON-NLS-1$

	/**
	 * This method is used to sort elements within a compilation unit.
	 * 
	 * @param compilationUnits compilation units to process
	 * @param comparator the comparator to use for the sorting
	 * @param monitor the given progress monitor
	 * 
	 * @since 2.1
	 */
	public static void sort(ICompilationUnit compilationUnit, int[] positions, Comparator comparator, IProgressMonitor monitor) throws CoreException {
		if (comparator == null || compilationUnit == null) {
			return;
		}
		SortElementsOperation operation = new SortElementsOperation(new ICompilationUnit[] { compilationUnit }, positions, comparator);
		JavaCore.run(operation, monitor);
	}

	/**
	 * This method is used to sort elements within a compilation unit.
	 * 
	 * @param compilationUnits compilation units to process
	 * @param comparator the comparator to use for the sorting
	 * @param monitor the given progress monitor
	 * 
	 * @since 2.1
	 */
	public static void sort(ICompilationUnit[] compilationUnits, int[] positions, Comparator comparator, IProgressMonitor monitor) throws CoreException {
		if (comparator == null || compilationUnits == null) {
			return;
		}
		SortElementsOperation operation = new SortElementsOperation(compilationUnits , positions, comparator);
		JavaCore.run(operation, monitor);
	}	
}
