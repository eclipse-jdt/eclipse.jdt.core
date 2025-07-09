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

import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypePattern;
import org.eclipse.jdt.internal.SignatureUtils;

public class DOMCompletionUtils {

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
	 * Returns the first parent type declaration (class, enum, record, annotation, anonymous), or null if there is no parent type declaration.
	 *
	 * @param nodeToSearch the node whose parents should be searched
	 * @return the first parent type declaration (class, enum, record, annotation, anonymous), or null if there is no parent type declaration
	 */
	public static ASTNode findParentTypeDeclaration(ASTNode nodeToSearch) {
		return DOMCompletionUtils.findParent(nodeToSearch, new int[] { ASTNode.TYPE_DECLARATION, ASTNode.ENUM_DECLARATION, ASTNode.RECORD_DECLARATION, ASTNode.ANNOTATION_TYPE_DECLARATION, ASTNode.ANONYMOUS_CLASS_DECLARATION });
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

	/**
	 * Represents collections of bindings that might be accessible depending on whether a boolean expression is true or false.
	 *
	 * @param trueBindings the bindings that are accessible when the expression is true
	 * @param falseBindings the bindings that are accessible when the expression is false
	 */
	public record TrueFalseBindings(List<IVariableBinding> trueBindings, List<IVariableBinding> falseBindings) {}

	/**
	 * Represents collections of type casts that might be safe depending on whether a boolean expression is true or false.
	 *
	 * @param trueBindings the bindings that are accessible when the expression is true
	 * @param falseBindings the bindings that are accessible when the expression is false
	 */
	public record TrueFalseCasts(List<ITypeBinding> trueCasts, List<ITypeBinding> falseCasts) {}

	/**
	 * Returns a list of variable bindings defined by type patterns in the given boolean expression.
	 *
	 * The list is separated into variables declared when the expression is true and variables declared when the expression is false.
	 *
	 * @param e the expression to collect the bindings for
	 * @return a list of variable bindings defined by type patterns in the given boolean expression
	 */
	public static TrueFalseBindings collectTrueFalseBindings(Expression e) {
		if (e instanceof PrefixExpression prefixExpression && prefixExpression.getOperator() == PrefixExpression.Operator.NOT) {
			TrueFalseBindings notBindings = collectTrueFalseBindings(prefixExpression.getOperand());
			return new TrueFalseBindings(notBindings.falseBindings(), notBindings.trueBindings());
		} else if (e instanceof InfixExpression infixExpression && (infixExpression.getOperator() == InfixExpression.Operator.CONDITIONAL_AND || infixExpression.getOperator() == InfixExpression.Operator.AND )) {
			TrueFalseBindings left = collectTrueFalseBindings(infixExpression.getLeftOperand());
			TrueFalseBindings right = collectTrueFalseBindings(infixExpression.getRightOperand());
			List<IVariableBinding> combined = new ArrayList<>();
			combined.addAll(left.trueBindings());
			combined.addAll(right.trueBindings());
			return new TrueFalseBindings(combined, Collections.emptyList());
		} else if (e instanceof InfixExpression infixExpression && (infixExpression.getOperator() == InfixExpression.Operator.CONDITIONAL_OR || infixExpression.getOperator() == InfixExpression.Operator.OR)) {
			TrueFalseBindings left = collectTrueFalseBindings(infixExpression.getLeftOperand());
			TrueFalseBindings right = collectTrueFalseBindings(infixExpression.getRightOperand());
			List<IVariableBinding> combined = new ArrayList<>();
			combined.addAll(left.falseBindings());
			combined.addAll(right.falseBindings());
			return new TrueFalseBindings(Collections.emptyList(), combined);
		} else {
			List<IVariableBinding> typePatternBindings = new ArrayList<>();
			DOMCompletionUtils.visitChildren(e, ASTNode.TYPE_PATTERN, (TypePattern patt) -> {
				typePatternBindings.add(patt.getPatternVariable().resolveBinding());
			});
			return new TrueFalseBindings(typePatternBindings, Collections.emptyList());
		}
	}

	/**
	 * Returns a list of safe casts to the given variable based on the type checks in the given boolean expression.
	 *
	 * The list is separated into the casts that are safe when the expression is true and the casts that are safe when the expression is false.
	 *
	 * @param e the expression to check for type checks in
	 * @param castedBinding the binding that will be casted
	 * @return a list of safe casts to the given variable based on the type checks in the given boolean expression
	 */
	public static TrueFalseCasts collectTrueFalseCasts(Expression e, IVariableBinding castedBinding) {
		if (e instanceof PrefixExpression prefixExpression && prefixExpression.getOperator() == PrefixExpression.Operator.NOT) {
			TrueFalseCasts notBindings = collectTrueFalseCasts(prefixExpression.getOperand(), castedBinding);
			return new TrueFalseCasts(notBindings.falseCasts(), notBindings.trueCasts());
		} else if (e instanceof InfixExpression infixExpression && (infixExpression.getOperator() == InfixExpression.Operator.CONDITIONAL_AND || infixExpression.getOperator() == InfixExpression.Operator.AND )) {
			TrueFalseCasts left = collectTrueFalseCasts(infixExpression.getLeftOperand(), castedBinding);
			TrueFalseCasts right = collectTrueFalseCasts(infixExpression.getRightOperand(), castedBinding);
			List<ITypeBinding> combined = new ArrayList<>();
			combined.addAll(left.trueCasts());
			combined.addAll(right.trueCasts());
			return new TrueFalseCasts(combined, Collections.emptyList());
		} else if (e instanceof InfixExpression infixExpression && (infixExpression.getOperator() == InfixExpression.Operator.CONDITIONAL_OR || infixExpression.getOperator() == InfixExpression.Operator.OR)) {
			TrueFalseCasts left = collectTrueFalseCasts(infixExpression.getLeftOperand(), castedBinding);
			TrueFalseCasts right = collectTrueFalseCasts(infixExpression.getRightOperand(), castedBinding);
			List<ITypeBinding> combined = new ArrayList<>();
			combined.addAll(left.falseCasts());
			combined.addAll(right.falseCasts());
			return new TrueFalseCasts(Collections.emptyList(), combined);
		} else {
			List<ITypeBinding> castedTypes = new ArrayList<>();
			DOMCompletionUtils.visitChildren(e, ASTNode.INSTANCEOF_EXPRESSION, (InstanceofExpression expr) -> {
				Expression leftOperand= expr.getLeftOperand();
				if (leftOperand instanceof Name name && name.resolveBinding() != null && name.resolveBinding().getKey().equals(castedBinding.getKey())) {
					castedTypes.add(expr.getRightOperand().resolveBinding());
				} else if (leftOperand instanceof FieldAccess fieldAccess && fieldAccess.resolveFieldBinding() != null && fieldAccess.resolveFieldBinding().getKey().equals(castedBinding.getKey())) {
					castedTypes.add(expr.getRightOperand().resolveBinding());
				}
			});
			return new TrueFalseCasts(castedTypes, Collections.emptyList());
		}
	}

}
