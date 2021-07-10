/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
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
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.util.CompilationUnitSorter;

import com.ibm.icu.text.Collator;

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
@SuppressWarnings("rawtypes")
class DefaultJavaElementComparator implements Comparator {

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
				}
				return this.categories[METHOD_CATEGORY];
			case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
				if (Flags.isStatic(node.getModifiers())) {
					return this.categories[STATIC_METHOD_CATEGORY];
				}
				return this.categories[METHOD_CATEGORY];
			case ASTNode.FIELD_DECLARATION :
				FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
				if (Flags.isStatic(fieldDeclaration.getModifiers())) {
					return this.categories[STATIC_FIELD_CATEGORY];
				}
				return this.categories[FIELD_CATEGORY];
			case ASTNode.ENUM_CONSTANT_DECLARATION :
				return this.categories[STATIC_FIELD_CATEGORY];
			case ASTNode.TYPE_DECLARATION :
			case ASTNode.ENUM_DECLARATION :
			case ASTNode.ANNOTATION_TYPE_DECLARATION :
				AbstractTypeDeclaration abstractTypeDeclaration = (AbstractTypeDeclaration) node;
				if (Flags.isStatic(abstractTypeDeclaration.getModifiers())) {
					return this.categories[STATIC_TYPE_CATEGORY];
				}
				return this.categories[TYPE_CATEGORY];
			case ASTNode.INITIALIZER :
				Initializer initializer = (Initializer) node;
				if (Flags.isStatic(initializer.getModifiers())) {
					return this.categories[STATIC_INITIALIZER_CATEGORY];
				}
				return this.categories[INITIALIZER_CATEGORY];
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
	@Override
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
		if (node1Signature.length() != 0 && node2Signature.length() != 0) {
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
				return String.valueOf(buffer);
			case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION :
				AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration = (AnnotationTypeMemberDeclaration) node;
				return annotationTypeMemberDeclaration.getName().getIdentifier();
			case ASTNode.FIELD_DECLARATION :
				FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
				return ((VariableDeclarationFragment) fieldDeclaration.fragments().get(0)).getName().getIdentifier();
			case ASTNode.ENUM_CONSTANT_DECLARATION :
				EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) node;
				return enumConstantDeclaration.getName().getIdentifier();
			case ASTNode.INITIALIZER :
				return ((Integer) node.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).toString();
			case ASTNode.TYPE_DECLARATION :
			case ASTNode.ENUM_DECLARATION :
			case ASTNode.ANNOTATION_TYPE_DECLARATION :
				AbstractTypeDeclaration abstractTypeDeclaration = (AbstractTypeDeclaration) node;
				return abstractTypeDeclaration.getName().getIdentifier();
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
				StringBuilder buffer = new StringBuilder();
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
		}
		QualifiedName qualifiedName = (QualifiedName) name;
		return buildSignature(qualifiedName.getQualifier()) + "." + buildSignature(qualifiedName.getName()); //$NON-NLS-1$
	}
}
