/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

public class DOMCompletionUtil {

	/**
	 * Returns the first parent node that is one of the given types, or null if there is no matching parent node.
	 *
	 * @param nodeToSearch the node whose parents should be searched
	 * @param kindsToFind array of node types that count as matches. See the constants in {@link ASTNode}
	 * @return the first parent node that is one of the given types, or null if there is no matching parent node
	 */
	public static ASTNode findParent(ASTNode nodeToSearch, int[] kindsToFind) {
		ASTNode cursor = nodeToSearch;
		while (cursor != null) {
			for (int kindToFind : kindsToFind) {
				if (cursor.getNodeType() == kindToFind) {
					return cursor;
				}
			}
			cursor = cursor.getParent();
		}
		return null;
	}

	/**
	 * Returns the first parent type declaration (class, enum, record, annotation, etc), or null if there is no parent type declaration.
	 *
	 * @param nodeToSearch the node whose parents should be searched
	 * @return the first parent type declaration (class, enum, record, annotation, etc), or null if there is no parent type declaration
	 */
	public static AbstractTypeDeclaration findParentTypeDeclaration(ASTNode nodeToSearch) {
		return (AbstractTypeDeclaration) DOMCompletionUtil.findParent(nodeToSearch, new int[] { ASTNode.TYPE_DECLARATION, ASTNode.ENUM_DECLARATION, ASTNode.RECORD_DECLARATION, ASTNode.ANNOTATION_TYPE_DECLARATION });
	}

}
