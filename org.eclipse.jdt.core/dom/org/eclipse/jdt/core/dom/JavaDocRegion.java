/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.core.dom.util.DOMASTUtil;

/**
 * JavaDocRegion pattern AST node type.
 *
 * <pre>
 * JavaDocRegion:
 *     [ TagElement { <b>,</b> TagElement } ]
 *     [ ASTNode { [TextElement] [JavaDocRegion] } ]
 * </pre>
 *
 * @since 3.29 BETA_JAVA 18
 */

@SuppressWarnings("rawtypes")
public class JavaDocRegion extends ASTNode implements IDocElement{

	JavaDocRegion(AST ast) {
		super(ast);
		unsupportedBelow18();
	}


	/**
	 * The "tags" structural property of this node type (child type: {@link TagElement}). (added in JEP 413).
	 */
	public static final ChildListPropertyDescriptor TAGS_PROPERTY  =
			new ChildListPropertyDescriptor(JavaDocRegion.class, "tags", TagElement.class, CYCLE_RISK); //$NON-NLS-1$);

	/**
	 * The "texts" structural property of this node type can have Text/Region elements(child type: {@link TextElement}, {@link JavaDocRegion}}). (added in JEP 413).
	 */
	public static final ChildListPropertyDescriptor TEXTS_PROPERTY  =
			new ChildListPropertyDescriptor(JavaDocRegion.class, "texts", ASTNode.class, CYCLE_RISK); //$NON-NLS-1$);


	/**
	 * The "dummy regions" structural property of this node type (added in JEP 413).
	 */
	public static final SimplePropertyDescriptor DUMMY_REGION_PROPERTY  = new SimplePropertyDescriptor(JavaDocRegion.class, "dummyRegion", boolean.class, MANDATORY); //$NON-NLS-1$);


	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(4);
		createPropertyList(JavaDocRegion.class, propertyList);
		addProperty(TAGS_PROPERTY, propertyList);
		addProperty(TEXTS_PROPERTY, propertyList);
		addProperty(DUMMY_REGION_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	/**
	 * The tags list; <code>empty</code> for none;
	 */
	private ASTNode.NodeList tags = null;

	/**
	 * The texts list; <code>empty</code> for none;
	 */
	private ASTNode.NodeList texts = null;

	/**
	 * The property dummyRegion
	 */
	private boolean dummyRegion = Boolean.TRUE;




	@Override
	List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}


	@Override
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean newValue) {
		if (property == DUMMY_REGION_PROPERTY) {
			if (get) {
				return isDummyRegion();
			} else {
				setDummyRegion(newValue);
				return false;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetBooleanProperty(property, get, newValue);
	}

	@Override
	int getNodeType0() {
		return JAVADOC_REGION;
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		return matcher.match(this, other);
	}

	@SuppressWarnings("unchecked")
	@Override
	ASTNode clone0(AST target) {
		JavaDocRegion result = new JavaDocRegion(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setDummyRegion(isDummyRegion());
		result.tags().addAll(
				ASTNode.copySubtrees(target, tags()));
		result.texts().addAll(
				ASTNode.copySubtrees(target, texts()));
		return result;
	}

	@Override
	void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);

	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 1 * 4 ;
	}

	@Override
	int treeSize() {
		return memSize();
	}


	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants

	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 */
	public static List propertyDescriptors(int apiLevel) {
		if (DOMASTUtil.isJavaDocCodeSnippetSupported(apiLevel)) {
			return PROPERTY_DESCRIPTORS;
		}
		return null;
	}

	/**
	 * Returns the list of tag elements in this region, or
	 * <code>empty</code> if there is none.
	 *
	 *  @return the list of tag element nodes
	 *    (element type: {@link TagElement})
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public List tags() {
		unsupportedBelow18();
		return this.tags;
	}

	/**
	 * Returns the list of text elements in this region, or
	 * <code>empty</code> if there is none.
	 *
	 *  @return the list of text element nodes
	 *    (element type: {@link TextElement})
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public List texts() {
		unsupportedBelow18();
		return this.texts;
	}

	/**
	 * Returns <code>true</code> is region is dummy else <code>false</code>.
	 * @return the dummyRegion
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public boolean isDummyRegion() {
		unsupportedBelow18();
		return this.dummyRegion;
	}

	/**
	 * Sets the value of dummyRegiong property.
	 * @param dummyRegion
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public void setDummyRegion(boolean dummyRegion) {
		unsupportedBelow18();
		preValueChange(DUMMY_REGION_PROPERTY);
		this.dummyRegion = dummyRegion;
		postValueChange(DUMMY_REGION_PROPERTY);
	}

}
