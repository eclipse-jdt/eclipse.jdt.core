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

		private static final int STATIC_TYPE_CATEGORY = 0;
		private static final int STATIC_FIELD_CATEGORY = 1;
		private static final int STATIC_INITIALIZER_CATEGORY = 2;
		private static final int STATIC_METHOD_CATEGORY = 3;
		private static final int TYPE_CATEGORY = 4;
		private static final int FIELD_CATEGORY = 5;
		private static final int INITIALIZER_CATEGORY = 6;
		private static final int CONSTRUCTOR_CATEGORY = 7;
		private static final int METHOD_CATEGORY = 8;
		
		private Collator collator;
		
		private int[] categories;
		
		public DefaultJavaElementComparator() {
			// initialize default categories
			this.categories = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
			this.collator = Collator.getInstance();
		}

		/**
		 * This constructor is used to specify customized values for the different categories. They are a convinient way to
		 * distinguish AST nodes. The lower a value is, the higher the node will appear in the sorted compilation unit.
		 * 
		 * There are nine categories with theirs default values:
		 * <ol>
		 * <li>static types (1)</li>
		 * <li>static fields (2)</li>
		 * <li>static initializers (3)</li>
		 * <li>static methods (4)</li>
		 * <li>types (5)</li>
		 * <li>fields (6) </li>
		 * <li>initializers (7)</li>
		 * <li>constructors (8)</li>
		 * <li>methods (9)</li>
		 * </ol>
		 * 
		 * @param staticTypeCategory the given value for the static type category
		 * @param staticFieldCategory the given value for the static field category
		 * @param staticInitializerCategory the given value for the static initializer category
		 * @param staticMethodCategory the given value for static the method category
		 * @param typeCategory the given value for the type category
		 * @param fieldCategory the given value for field category
		 * @param initializerCategory the given value for initializer category
		 * @param constructorCategory the given value for constructor category
		 * @param methodCategory the given value for method category
		 */
		public DefaultJavaElementComparator(
			int staticTypeCategory,
			int staticFieldCategory,
			int staticInitializerCategory,
			int staticMethodCategory,
			int typeCategory,
			int fieldCategory,
			int initializerCategory,
			int constructorCategory,
			int methodCategory) {
				this.categories = new int[] {
					staticTypeCategory,
					staticFieldCategory,
					staticInitializerCategory,
					staticMethodCategory,
					typeCategory,
					fieldCategory,
					initializerCategory,
					constructorCategory,
					methodCategory
				};
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
						return this.categories[CONSTRUCTOR_CATEGORY];
					}
					if (Flags.isStatic(methodDeclaration.getModifiers())) {
						return this.categories[STATIC_METHOD_CATEGORY];
					} else {
						return this.categories[METHOD_CATEGORY];
					}
				case ASTNode.FIELD_DECLARATION :
					FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
					if (Flags.isStatic(fieldDeclaration.getModifiers())) {
						return this.categories[STATIC_FIELD_CATEGORY];
					} else {
						return this.categories[FIELD_CATEGORY];
					}
				case ASTNode.TYPE_DECLARATION :
					TypeDeclaration typeDeclaration = (TypeDeclaration) node;
					if (Flags.isStatic(typeDeclaration.getModifiers())) {
						return this.categories[STATIC_TYPE_CATEGORY];
					} else {
						return this.categories[TYPE_CATEGORY];
					}
				case ASTNode.INITIALIZER :
					Initializer initializer = (Initializer) node;
					if (Flags.isStatic(initializer.getModifiers())) {
						return this.categories[STATIC_INITIALIZER_CATEGORY];
					} else {
						return this.categories[INITIALIZER_CATEGORY];
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
			String node1Signature = buildSignature(node1);
			String node2Signature = buildSignature(node2);
			if (node1Signature == null || node2Signature == null) {
				return 0;
			} else if (node1Signature.length() != 0 && node2Signature.length() != 0) {
				int compare = this.collator.compare(node1Signature, node2Signature);
				if (compare != 0) {
					return compare;
				}
			}
			int sourceStart1 = ((Integer) node1.getProperty(CompilationUnitSorter.SOURCE_START)).intValue();
			int sourceStart2 = ((Integer) node2.getProperty(CompilationUnitSorter.SOURCE_START)).intValue();
			return sourceStart1 - sourceStart2;
		}

		private String buildSignature(BodyDeclaration node) {
			switch(node.getNodeType()) {
				case ASTNode.METHOD_DECLARATION :
					MethodDeclaration methodDeclaration = (MethodDeclaration) node;
					StringBuffer buffer = new StringBuffer();
					buffer.append(methodDeclaration.getName().getIdentifier());
					final List parameters = methodDeclaration.parameters();
					int length1 = parameters.size();
					for (int i = 0; i < length1; i++) {
						buffer.append(((SingleVariableDeclaration) parameters.get(i)).getName().getIdentifier());
					}
					return buffer.toString();
				case ASTNode.FIELD_DECLARATION :
					FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
					return ((VariableDeclarationFragment) fieldDeclaration.fragments().get(0)).getName().getIdentifier();
				case ASTNode.INITIALIZER :
					return ((Integer) node.getProperty(CompilationUnitSorter.SOURCE_START)).toString();
				case ASTNode.TYPE_DECLARATION :
					TypeDeclaration typeDeclaration = (TypeDeclaration) node;
					return typeDeclaration.getName().getIdentifier();
			}
			return null;
		}
	}

	/**
	 * This field is used to retrieve a property of the AST node used by the compare method. This
	 * property returns an integer which is the corresponding source start of the node.
	 * 		(Integer) node.getProperty(CompilationUnitSorter.SOURCE_START)
	 * 
	 * @since 2.1
	 */
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
