/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
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
import org.eclipse.jdt.core.JavaModelException;
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
 * Operation for sorting the members of a compilation unit.
 * <p>
 * This class provides all functionality via static members; it is not
 * intended to be instantiated or subclassed.
 * </p>
 * 
 * @since 2.1
 */
public final class CompilationUnitSorter {
	
 	/**
 	 * Private constructor to prevent instantiation.
 	 */
	private CompilationUnitSorter() {
	} 
	
	/**
	 * The class <code>DefaultJavaElementComparator</code> is a standard
	 * implementation of a comparator.
	 * <p>
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
	 */
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
		 * Creates an instance that sorts the various categories of body
		 * declarations in the following order:
		 * <ol>
		 * <li>static types</li>
		 * <li>static fields </li>
		 * <li>static initializers</li>
		 * <li>non-static fields</li>
		 * <li>instance initializers</li>
		 * <li>types</li>
		 * <li>static methods</li>
		 * <li>constructors</li>
		 * <li>non-static methods</li>
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
		 * Creates an instance that arranges the various categories of body
		 * declarations.
		 * This constructor is used to specify customized values for the different categories.
		 * They are a convinient way to distinguish AST nodes. 
		 * The lower a value is, the higher the node will appear in the sorted
		 * compilation unit.
		 * <p>
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
		 * </p>
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
		 * The <code>DefaultJavaElementComparator</code> implementation of this
		 * <code>java.util.Comparator</code> method can only be used to compare
		 * instances of <code>org.eclipse.jdt.core.dom.BodyDeclaration</code>.
		 * <p>
		 * The categories of each body declaration are compared. If they are
		 * in different categories, they are ordered based on their category.
		 * Body declarations within the same category are ordered by signature
		 * string. Body declarations with the same signature string are ordered
		 * by their original relative positions.
		 * </p>
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
			// TODO: (olivier) remove unnecessary null checks
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
	 * Name of auxillary property whose value can be used to determine the
	 * original relative order of two body declarations. This allows a
	 * comparator to preserve the relative positions of certain kinds of
	 * body declarations when required.
	 * <p>
	 * All body declarations passed to the comparator's <code>compare</code>
	 * method by <code>CompilationUnitSorter.sort</code> carry an
	 * Integer-valued property. The body declaration with the lower value
	 * comes before the one with the higher value. The exact numeric value
	 * of these properties is unspecified.
	 * </p>
	 * <p>
	 * Example usage:
	 * <pre>
	 *      BodyDeclaration b1 = (BodyDeclaration) object1;
	 *      BodyDeclaration b2 = (BodyDeclaration) object2;
	 * 		Integer i1 = (Integer) b1.getProperty(RELATIVE_ORDER);
	 * 		Integer i2 = (Integer) b2.getProperty(RELATIVE_ORDER);
	 * 		return i1.intValue() - i2.intValue(); // preserve original order
	 * </pre>
	 * </p>
	 * 
	 * @see #sort
	 * @see org.eclipse.jdt.core.dom.BodyDeclaration
	 * @since 2.1
	 */
	public static final String RELATIVE_ORDER = "relativeOrder"; //$NON-NLS-1$

	/**
	 * Reorders the declarations in the given compilation unit. The caller is
	 * responsible for arranging in advance that the given compilation unit is
	 * a working copy, and for saving the changes afterwards.
	 * <p>
	 * The optional <code>positions</code> array contains a non-decreasing 
	 * ordered list of character-based source positions within the compilation
	 * unit's source code string. Upon return from this method, the positions in
	 * the array reflect the corresponding new locations in the modified source
	 * code string, Note that this operation modifies the given array in place.
	 * </p>
	 * <p>
	 * The <code>compare</code> method of the given comparator is passed pairs
	 * of AST body declarations (subclasses of <code>BodyDeclaration</code>) 
	 * representing body declarations at the same level. The comparator is
	 * called on body declarations of nested classes, including anonymous and
	 * local classes, but always at the same level. The class 
	 * <code>DefaultJavaElementComparator</code> is the standard comparator, 
	 * but clients are free to provide their own comparator implementations.
	 * </p>
	 * <p>
	 * The body declarations passed as parameters to the comparator carry 
	 * signature information only (that is, they are not complete ASTs).
	 * Clients cannot rely on the AST nodes being properly parented or on
	 * having source range information.
	 * The following table describes the information available for each kind
	 * of body declaration:
	 * <br>
	 * <table border="1" width="80%" cellpadding="5">
	 *	  <tr>
	 *	    <td width="20%"><code>TypeDeclaration</code></td>
	 *	    <td width="50%"><code>modifiers, isInterface, name, superclass,
	 *	      superInterfaces<br>
     *		  No bodyDeclarations<br>
     *		  RELATIVE_ORDER property</code></td>
	 *	  </tr>
	 *	  <tr>
	 *	    <td width="20%"><code>FieldDeclaration</code></td>
	 *	    <td width="50%"><code>modifiers, type, fragments
	 *        (VariableDeclarationFragments
	 *	      with name only)<br>
     *		  RELATIVE_ORDER property</code></td>
	 *	  </tr>
	 *	  <tr>
	 *	    <td width="20%"><code>MethodDeclaration</code></td>
	 *	    <td width="50%"><code>modifiers, isConstructor, returnType, name,
	 *		  parameters
	 *	      (SingleVariableDeclarations with name and type only),
	 *		  thrownExceptions<br>
	 *	      No body<br>
     *		  RELATIVE_ORDER property</code></td>
	 *	  </tr>
	 *	  <tr>
	 *	    <td width="20%"><code>Initializer</code></td>
	 *	    <td width="50%"><code>modifiers<br>
	 *	      No body<br>
     *		  RELATIVE_ORDER property</code></td>
	 *	  </tr>
	 * </table>
	 * </p>
	 *
	 * @param compilationUnit the given compilation unit, which must be a 
	 * working copy
	 * @param positions an array of source positions to map, or 
	 * <code>null</code> if none. If supplied, the positions must 
	 * character-based source positions within the original source code for
	 * the given compilation unit, arranged in non-decreasing order.
	 * The array is updated in place when this method returns to reflect the
	 * corresponding source positions in the permuted source code string.
	 * @param comparator the comparator capable of ordering 
	 * <code>BodyDeclaration</code>s
	 * @param monitor the progress monitor to notify, or <code>null</code> if
	 * none
	 * @exception JavaModelException if the compilation unit could not be
	 * sorted. Reasons include:
	 * <ul>
	 * <li> The given compilation unit does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> The given compilation unit is not a working copy</li>
	 * <li> A <code>CoreException</code> occurred while accessing the underlying
	 * resource
	 * </ul>
	 * @see org.eclipse.jdt.core.dom.BodyDeclaration
	 * @see #DefaultJavaElementComparator
	 * @see #RELATIVE_ORDER
	 * @since 2.1
	 */
	public static void sort(ICompilationUnit compilationUnit, int[] positions, Comparator comparator, IProgressMonitor monitor) throws JavaModelException {
		if (compilationUnit == null || comparator == null) {
			throw new IllegalArgumentException();
		}
		// TODO: (olivier) Remove extra level of array
		ICompilationUnit[] compilationUnits = new ICompilationUnit[] { compilationUnit };
		int[][] positionsList = new int[][] {positions};
		SortElementsOperation operation = new SortElementsOperation(compilationUnits, positionsList, comparator);
		try {
			JavaCore.run(operation, monitor);
		} catch(CoreException e) {
			throw new JavaModelException(e);
		}
	}

}
