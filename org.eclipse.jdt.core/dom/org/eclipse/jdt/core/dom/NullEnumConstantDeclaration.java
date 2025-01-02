/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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
 * Null EnumConstantDeclaration node.
 *
 * @since 3.41
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("rawtypes")
public class NullEnumConstantDeclaration extends EnumConstantDeclaration {

	/**
	 * Null EnumConstantDeclaration Pattern node.
	 *
	 * @since 3.27
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(1);
		createPropertyList(NullPattern.class, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants

	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.38
	 */
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
	NullEnumConstantDeclaration(AST ast) {
		super(ast);
		supportedOnlyIn21();
	}

	@Override
	ASTNode clone0(AST target) {
		NullPattern result = new NullPattern(target);
		result.setSourceRange(getStartPosition(), getLength());
		return result;
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
