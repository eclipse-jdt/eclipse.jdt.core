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
 * TagProperty pattern AST node type.
 *
 * <pre>
 * TagProperty:
 *      Name && Value
 * </pre>
 *
 * @since 3.29 BETA_JAVA 18
 */

@SuppressWarnings("rawtypes")
public class TagProperty extends ASTNode implements IDocElement{

	TagProperty(AST ast) {
		super(ast);
		supportedOnlyIn18();
	}

	/**
	 * The "name" structural property of this node type (added in JEP 413).
	 */
	public static final SimplePropertyDescriptor NAME_PROPERTY  = new SimplePropertyDescriptor(TagProperty.class, "name", String.class, MANDATORY); //$NON-NLS-1$);

	/**
	 * The "value" structural property of this node type . (added in JEP 413).
	 */
	public static final SimplePropertyDescriptor VALUE_PROPERTY  =
			new SimplePropertyDescriptor(TagProperty.class, "value", String.class, MANDATORY); //$NON-NLS-1$);


	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(3);
		createPropertyList(TagProperty.class, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(VALUE_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	/**
	 * The property name
	 */
	private String name = null;

	/**
	 * The property value
	 */
	private String value = null;




	@Override
	List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}


	@Override
	final Object internalGetSetObjectProperty(SimplePropertyDescriptor property, boolean get, Object newValue) {
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((String)newValue);
				return null;
			}
		} else if (property == VALUE_PROPERTY) {
			if (get) {
				return getValue();
			} else {
				setValue((String)newValue);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetObjectProperty(property, get, newValue);
	}

	@Override
	int getNodeType0() {
		return GUARDED_PATTERN;
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		return matcher.match(this, other);
	}

	@Override
	ASTNode clone0(AST target) {
		TagProperty result = new TagProperty(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setName(getName());
		result.setName(getValue());
		return result;
	}

	@Override
	void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);

	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 2 * 4 + stringSize(this.name) + stringSize(this.value);
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
	 * Returns the name of this tag property.
	 *
	 * @return the name
	 */
	public String getName() {
		unsupportedBelow18();
		return this.name;
	}

	/**
	 * Returns the value of this tag property.
	 * @return the value
	 * @exception UnsupportedOperationException if this operation is used other than JLS18
	 */
	public String getValue() {
		unsupportedBelow18();
		return this.value;
	}

	/**
	 * Sets the name of this tag property.
	 *
	 * @param name
	 * <ul>
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 * </ul>
	 */
	public void setName(String name) {
		unsupportedBelow18();
		preValueChange(NAME_PROPERTY);
		this.name = name;
		postValueChange(NAME_PROPERTY);
	}

	/**
	 * Sets the value of this tag property.
	 * @param value
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public void setValue(String value) {
		unsupportedBelow18();
		preValueChange(VALUE_PROPERTY);
		this.value = value;
		postValueChange(VALUE_PROPERTY);
	}

}
