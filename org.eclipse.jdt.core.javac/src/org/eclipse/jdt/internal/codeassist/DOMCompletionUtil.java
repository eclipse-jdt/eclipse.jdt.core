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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.internal.SignatureUtils;

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

	public static <T extends ASTNode> void visitChildren(ASTNode root, int kind, Consumer<T> consumer) {
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public void preVisit(ASTNode node) {
				if (node.getNodeType() == kind) {
					consumer.accept((T)node);
				}
			}
		};
		root.accept(visitor);
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

	/**
	 * Returns true if toFind is a superclass of root.
	 *
	 * @param root the class to begin searching in
	 * @param toFind the class to find
	 * @return true if toFind is a superclass of root
	 */
	public static boolean findInSupers(ITypeBinding root, ITypeBinding toFind) {
		ITypeBinding superFind = toFind.getErasure();
		if( superFind != null ) {
			String keyToFind = superFind.getKey();
			return findInSupers(root, keyToFind);
		}
		return false;
	}

	/**
	 * Returns true if the type indicated by keyOfTypeToFind is a superclass of root.
	 *
	 * @param root the class to begin searching in
	 * @param keyOfTypeToFind the key of the class to find
	 * @return true if the type indicated by keyOfTypeToFind is a superclass of root
	 */
	public static boolean findInSupers(ITypeBinding root, String keyOfTypeToFind) {
		String keyToFind = keyOfTypeToFind;
		Queue<ITypeBinding> toCheck = new LinkedList<>();
		Set<String> alreadyChecked = new HashSet<>();
		toCheck.add(root.getErasure());
		while (!toCheck.isEmpty()) {
			ITypeBinding current = toCheck.poll();
			if (current == null) {
				continue;
			}
			String currentKey = current.getErasure().getKey();
			if (alreadyChecked.contains(currentKey)) {
				continue;
			}
			alreadyChecked.add(currentKey);
			if (currentKey.equals(keyToFind)) {
				return true;
			}
			for (ITypeBinding superInterface : current.getInterfaces()) {
				toCheck.add(superInterface);
			}
			if (current.getSuperclass() != null) {
				toCheck.add(current.getSuperclass());
			}
		}
		return false;
	}

	/**
	 * Returns true if the type indicated by keyOfTypeToFind is a superclass of root.
	 *
	 * @param root             the class to begin searching in
	 * @param keyOfTypeToFind  the key of the class to find
	 * @param workignCopyOwner the working copy owner
	 * @param hierarchyCache   the cache of existing type hierarchies (rebuilding
	 *                         the type hierarchy is very expensive)
	 * @return true if the type indicated by keyOfTypeToFind is a superclass of root
	 */
	public static boolean findInSupers(IType root, String keyOfTypeToFind, WorkingCopyOwner workingCopyOwner, Map<String, ITypeHierarchy> hierarchyCache) {
		if (root.getKey().equals(keyOfTypeToFind)) {
			return true;
		}
		try {
			String signature = SignatureUtils.getSignatureForTypeKey(keyOfTypeToFind);
			IType typeToFind = root.getJavaProject().findType(Signature.getSignatureQualifier(signature)+ "." + Signature.getSignatureSimpleName(signature));
			if (typeToFind != null) {
				ITypeHierarchy hierarchy;
				if (hierarchyCache.containsKey(keyOfTypeToFind)) {
					hierarchy = hierarchyCache.get(keyOfTypeToFind);
				} else {
					hierarchy = typeToFind.newTypeHierarchy(root.getJavaProject(), workingCopyOwner, new NullProgressMonitor());
					hierarchyCache.put(keyOfTypeToFind, hierarchy);
				}

				for (IType subType : hierarchy.getAllSubtypes(typeToFind)) {
					if (subType.getKey().equals(root.getKey())) {
						return true;
					}
				}
			}
			return false;
		} catch (JavaModelException e) {
			return false;
		}
	}

	/**
	 * Returns true if the given node is in a qualified name and false otherwise.
	 *
	 * @return true if the given node is in a qualified name and false otherwise
	 */
	public static boolean isInQualifiedName(ASTNode node) {
		return Set.of(QualifiedName.NAME_PROPERTY,
				FieldAccess.NAME_PROPERTY,
				ExpressionMethodReference.NAME_PROPERTY,
				TypeMethodReference.NAME_PROPERTY,
				SuperMethodReference.NAME_PROPERTY).contains(node.getLocationInParent())
				|| node instanceof FieldAccess
				|| node instanceof QualifiedName
				|| node instanceof SuperMethodReference
				|| node instanceof TypeMethodReference;
	}

}
