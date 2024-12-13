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

import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

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

	private static final List<String> JAVA_MODIFIERS = List.of(
				ModifierKeyword.PUBLIC_KEYWORD.toString(),
				ModifierKeyword.PRIVATE_KEYWORD.toString(),
				ModifierKeyword.STATIC_KEYWORD.toString(),
				ModifierKeyword.PROTECTED_KEYWORD.toString(),
				ModifierKeyword.SYNCHRONIZED_KEYWORD.toString(),
				ModifierKeyword.ABSTRACT_KEYWORD.toString(),
				ModifierKeyword.FINAL_KEYWORD.toString(),
				ModifierKeyword.DEFAULT_KEYWORD.toString(),
				ModifierKeyword.NATIVE_KEYWORD.toString(),
				ModifierKeyword.STRICTFP_KEYWORD.toString(),
				ModifierKeyword.TRANSIENT_KEYWORD.toString(),
				ModifierKeyword.VOLATILE_KEYWORD.toString()
			);

	/**
	 * Returns true if the given String is a java modifier for fields and methods and false otherwise.
	 *
	 * In this case, modifiers are that you can include before methods or fields.
	 *
	 * @param potentialModifer the String to check if it's a modifier
	 * @return true if the given String is a java modifier for fields and methods and false otherwise
	 */
	public static boolean isJavaFieldOrMethodModifier(String potentialModifer) {
		return JAVA_MODIFIERS.contains(potentialModifer);
	}

}
