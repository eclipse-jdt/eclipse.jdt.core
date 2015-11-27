/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.pdom.java;

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.db.IndexException;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

/**
 * PDOMTreeNode elements form a tree of nodes rooted at a
 * {@link PDOMResourceFile}. Each node contains a list of children
 * which it declares and has a pointer to the most specific node which
 * declares it.
 * @since 3.12
 */
public abstract class PDOMTreeNode extends PDOMNode {
	public static final FieldManyToOne<PDOMTreeNode> PARENT;
	public static final FieldOneToMany<PDOMTreeNode> CHILDREN;

	public static final StructDef<PDOMTreeNode> type; 

	static {
		type = StructDef.create(PDOMTreeNode.class, PDOMNode.type);
		PARENT = FieldManyToOne.create(type, null);
		CHILDREN = FieldOneToMany.create(type, PARENT, 16);
		type.done();
	}

	public PDOMTreeNode(PDOM dom, long record) {
		super(dom, record);
	}

	protected PDOMTreeNode(PDOM pdom, PDOMTreeNode parent) {
		super(pdom);

		PARENT.put(pdom, this.address, parent == null ? 0 : parent.address);
	}

	/**
	 * Returns the closest ancestor of the given type, or null if none. Note that
	 * this looks for an exact match. It will not return subtypes of the given type.
	 */
	@SuppressWarnings("unchecked")
	public <T extends PDOMTreeNode> T getAncestorOfType(Class<T> type) {
		long targetType = getPDOM().getNodeType(type);

		PDOM pdom = getPDOM();
		long current = PARENT.getAddress(pdom, this.address);

		while (current != 0) {
			short currentType = NODE_TYPE.get(pdom, current);

			if (currentType == targetType) {
				PDOMNode result = load(pdom, current);

				if (type.isInstance(result)) {
					return (T) result;
				} else {
					throw new IndexException("The node at address " + current + 
							" should have been an instance of " + type.getName() + 
							" but was an instance of " + result.getClass().getName());
				}
			}

			current = PARENT.getAddress(pdom, current);
		}

		return null;
	}

	PDOMTreeNode getParentNode() {
		return PARENT.get(getPDOM(), this.address);
	}

	public PDOMBinding getParentBinding() throws IndexException {
		PDOMNode parent= getParentNode();
		if (parent instanceof PDOMBinding) {
			return (PDOMBinding) parent;
		}
		return null;
	}
}
