/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
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
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * Null Type node.
 * @since 3.39
 * @noinstantiate This class is not intended to be instantiated by clients.
 *
 */
@SuppressWarnings("rawtypes")
public class NullType extends Type {
	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(1);
		createPropertyList(NullType.class, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}

	/**
	 * Creates a new unparented null literal node owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	NullType(AST ast) {
		super(ast);
		supportedOnlyIn21();
	}

	@Override
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	@Override
	final int getNodeType0() {
		return NULL_TYPE;
	}

	@Override
	ASTNode clone0(AST target) {
		NullType result = new NullType(target);
		result.setSourceRange(getStartPosition(), getLength());
		return result;
	}

	@Override
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	@Override
	void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE;
	}

	@Override
	int treeSize() {
		return memSize();
	}
}
