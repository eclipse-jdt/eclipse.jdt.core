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
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.core.SortElementsOperation;
/**
 * The comparator used to sort the elements inside a compilation unit needs to follow the 
 * following constraints:
 * <ol>
 * <li>The comparator compare(Object, Object) methods parameters are instances of 
 * org.eclipse.jdt.core.dom.BodyDeclaration.</li>
 * </ol>
 * 
 * These nodes will have the following initalizations:
 * <table border="1">
 * <tr>
 * <th>Node type</th>
 * <th>Initializations</th>
 * </tr>
 * <tr>
 * <td>org.eclipse.jdt.core.dom.TypeDeclaration</td>
 * <td><ul>
 * <li>its name</li>
 * <li>its superclass if it is specified in the source code</li>
 * <li>its superinterfaces</li>
 * <li>its modifier</li>
 * <li>its RELATIVE_ORDER property</li>
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td>org.eclipse.jdt.core.dom.Initializer</td>
 * <td><ul>
 * <li>its modifier</li>
 * <li>its RELATIVE_ORDER property</li>
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td>org.eclipse.jdt.core.dom.FieldDeclaration</td>
 * <td><ul>
 * <li>its modifier</li>
 * <li>its type</li>
 * <li>its variable declaration fragments (name only, they don't have a RELATIVE_ORDER property set)</li>
 * <li>its RELATIVE_ORDER property</li>
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td>org.eclipse.jdt.core.dom.MethodDeclaration</td>
 * <td><ul>
 * <li>its modifier</li>
 * <li>its constructor's bit (answer true to isConstructor() for a constructor declaration)</li>
 * <li>its return type if not a constructor</li>
 * <li>its name (the name is the class name for a constructor)</li>
 * <li>its arguments (name and type)</li>
 * <li>its thrown exceptions</li>
 * <li>its RELATIVE_ORDER property</li>
 * </ul>
 * </td>
 * </tr>
 * </table>
 * A default implementation of such a comparator is provided.
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
		
		/**
		 * This constructor uses the default values for the different categories.
		 * 
		 * There are nine categories with theirs default values:
		 * <ol>
		 * <li>static types (1)</li>
		 * <li>static fields (2)</li>
		 * <li>static initializers (3)</li>
		 * <li>fields (4) </li>
		 * <li>initializers (5)</li>
		 * <li>types (6)</li>
		 * <li>static methods (7)</li>
		 * <li>constructors (8)</li>
		 * <li>methods (9)</li>
		 * </ol>
		 */
		public DefaultJavaElementComparator() {
			// initialize default categories
			this.categories = new int[] {
				1, // static type
				2, // static field
				3, // static initializer
				7, // static method
				6, // type
				4, // field
				5, // initializer
				8, // constructor
				9  // method
			};
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
		 * <li>fields (4) </li>
		 * <li>initializers (5)</li>
		 * <li>types (6)</li>
		 * <li>static methods (7)</li>
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
			int sourceStart1 = ((Integer) node1.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).intValue();
			int sourceStart2 = ((Integer) node2.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).intValue();
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
						SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(i);
						buffer.append(parameter.getName().getIdentifier());
						Type type = parameter.getType();
						buffer.append(buildSignature(type));
					}
					return buffer.toString();
				case ASTNode.FIELD_DECLARATION :
					FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
					return ((VariableDeclarationFragment) fieldDeclaration.fragments().get(0)).getName().getIdentifier();
				case ASTNode.INITIALIZER :
					return ((Integer) node.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).toString();
				case ASTNode.TYPE_DECLARATION :
					TypeDeclaration typeDeclaration = (TypeDeclaration) node;
					return typeDeclaration.getName().getIdentifier();
			}
			return null;
		}

		private String buildSignature(Type type) {
			switch(type.getNodeType()) {
				case ASTNode.PRIMITIVE_TYPE :
					PrimitiveType.Code code = ((PrimitiveType) type).getPrimitiveTypeCode();
					return code.toString();
				case ASTNode.ARRAY_TYPE :
					ArrayType arrayType = (ArrayType) type;
					StringBuffer buffer = new StringBuffer();
					buffer.append(buildSignature(arrayType.getElementType()));
					int dimensions = arrayType.getDimensions();
					for (int j = 0; j < dimensions; j++) {
						buffer.append("[]"); //$NON-NLS-1$
					}
					return buffer.toString();
				case ASTNode.SIMPLE_TYPE :
					SimpleType simpleType = (SimpleType) type;
					return buildSignature(simpleType.getName());
			}
			return null; // should never happen
		}
		
		private String buildSignature(Name name) {
			if (name.isSimpleName()) {
				return ((SimpleName) name).getIdentifier();
			} else {
				QualifiedName qualifiedName = (QualifiedName) name;
				return buildSignature(qualifiedName.getQualifier()) + "." + buildSignature(qualifiedName.getName()); //$NON-NLS-1$
			}
		}
	}

	/**
	 * This field is used to retrieve a property of the AST node used by the compare method. This
	 * property returns an integer which corresponds to a position that preceeds the starting position
	 * of the node. The exact value of this property is not important. What matters is that if node a
	 * is created before node b, then this property for node a will be lower than the same property for
	 * node b. To be brief, this property should be used if the syntactical order matters.
	 * <pre>
	 * 		(Integer) astNode.getProperty(CompilationUnitSorter.RELATIVE_ORDER)
	 * </pre>
	 * 
	 * @since 2.1
	 */
	public static final String RELATIVE_ORDER = "relativeOrder"; //$NON-NLS-1$

	/**
	 * Reorders the declarations in this compilation unit according to the given
	 * comparator.
	 * <p>
	 * The <code>compare</code> method of the given comparator is passed pairs
	 * of AST body declarations (subclasses of <code>BodyDeclaration</code>) 
	 * representing body declarations at the same level. The comparator is
	 * called on body declarations of nested classes, including anonymous and
	 * local classes, but always at the same level.
	 * </p>
	 * <p>
	 * The <code>positions</code> array contains character-based source
	 * positions within the source code for the compilation unit. As the
	 * declarations are rearranged, the positions in this array are updated to
	 * reflect the corresponding position in the modified source code.
	 * </p>
	 * <p>
	 * Clients cannot rely on the AST nodes being properly parented nor on their
	 * usual source ranges. The starting position of the source range for each
	 * body declaration is available as the <code>RELATIVE_ORDER</code> property
	 * of the body declaration passed to the comparator.
	 * </p>
	 * <p>
	 * <code>DefaultJavaElementComparator</code> is a standard implementation of
	 * a comparator.
	 * <ul>
	 * <li>static fields, arranged alphabetically by name and access modifier
	 * (public, protected, private, default)</li>
	 * <li>static initializers in order of appearance</li>
	 * <li>instance fields, arranged alphabetically by name and access modifier
	 * (public, protected, private, default)</li>
	 * <li>instance initializers in order of appearance</li>
	 * <li>type declarations, arranged alphabetically by name and access modifier
	 * (public, protected, private, default)</li>
	 * <li>constructors, arranged by parameter order and access modifier
	 * (public, protected, private, default)</li>
	 * <li>methods, arranged by alphabetically by name and parameter order and 
	 * access modifier (public, protected, private, default)</li>
	 * </p>
	 *
	 * @param compilationUnit the compilation unit
	 * @param positions an array of increasing positions to map. These are
	 * character-based source positions inside the original source, for which
	 * corresponding positions in the modified source will be computed (so as to
	 * relocate elements associated with the original source). It updates the
	 * positions array with updated positions. If set to <code>null</code>, then
	 * no positions are mapped.
	 * @param comparator the comparator to use for the sorting
	 * @param monitor the progress monitor to notify, or <code>null</code> if
	 * none
	 * @exception JavaModelException if the compilation unit could not be sorted.
	 * Reasons include:
	 * <ul>
	 * <li> The given compilation unit does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> The given compilation unit is not a working copy</li>
	 * <li> A <code>CoreException</code> occurred while updating the underlying
	 * resource
	 * </ul>
	 * @exception CoreException a Core exception is thrown if the supplied compilation unit is <code>null</code></li>,
	 * the supplied compilation unit is not an instance of IWorkingCopy
	 * @see org.eclipse.jdt.core.dom.BodyDeclaration
	 * @see #RELATIVE_ORDER
	 * @see #DefaultJavaElementComparator
	 * @since 2.1
	 * TODO: (olivier) Should throw JavaModelException rather than CoreException
	 */
	public static void sort(ICompilationUnit compilationUnit, int[] positions, Comparator comparator, IProgressMonitor monitor) throws CoreException {
	    // TODO: (olivier) Should throw IllegalArgumentException if compilationUnit == null
	    // TODO: (olivier) Should throw IllegalArgumentException if comparator == null
		if (comparator == null || compilationUnit == null) {
			return;
		}
		SortElementsOperation operation = new SortElementsOperation(new ICompilationUnit[] { compilationUnit }, new int[][] {positions}, comparator);
		JavaCore.run(operation, monitor);
	}

	/**
	 * This method is used to sort elements within each compilation unit inside this compilationUnits array.
	 * The positions are mapped to the new positions once the sorting is done. This should be used to
	 * update the positions of markers within compilation units.
	 * The sizes of positions and compilationUnits array have to be the same.
	 * 
	 * @param compilationUnits compilation units to process
	 * @param positions positions to map
	 * @param comparator the comparator to use for the sorting
	 * @param monitor the given progress monitor
	 * 
	 * @exception CoreException a Core exception is thrown if one of the supplied compilation units is <code>null</code></li>,
	 * one of the supplied elements are not an instance of IWorkingCopy, or the size of the given positions and of the given
	 * compilationUnits arrays are not equal.
	 * 
	 * @since 2.1
	 */
	public static void sort(ICompilationUnit[] compilationUnits, int[][] positions, Comparator comparator, IProgressMonitor monitor) throws CoreException {
		if (comparator == null || compilationUnits == null) {
			return;
		}
		SortElementsOperation operation = new SortElementsOperation(compilationUnits , positions, comparator);
		JavaCore.run(operation, monitor);
	}	
}
