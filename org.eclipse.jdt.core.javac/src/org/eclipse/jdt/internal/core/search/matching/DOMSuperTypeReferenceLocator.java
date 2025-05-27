/*******************************************************************************
 * Copyright (c) 2023, 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.util.Set;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.search.LocatorResponse;

public class DOMSuperTypeReferenceLocator extends DOMPatternLocator {

	private SuperTypeReferenceLocator locator;

	public DOMSuperTypeReferenceLocator(SuperTypeReferenceLocator locator) {
		super(locator.pattern);
		this.locator = locator;
	}

	@Override
	public LocatorResponse match(org.eclipse.jdt.core.dom.LambdaExpression node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (this.locator.pattern.superRefKind != SuperTypeReferencePattern.ONLY_SUPER_INTERFACES)
			return toResponse(IMPOSSIBLE_MATCH);
		nodeSet.setMustResolve(true);
		int level = nodeSet.addMatch(node, POSSIBLE_MATCH);
		return toResponse(level, true);
	}

	@Override
	public LocatorResponse match(Type node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (!Set.of(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY, TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY, ClassInstanceCreation.TYPE_PROPERTY).contains(node.getLocationInParent())) {
			return toResponse(IMPOSSIBLE_MATCH);
		}
		if (node.getLocationInParent() == ClassInstanceCreation.TYPE_PROPERTY && node.getParent() instanceof ClassInstanceCreation newInst && newInst.getAnonymousClassDeclaration() == null) {
			return toResponse(IMPOSSIBLE_MATCH);
		}
		if (node.getParent() instanceof AbstractTypeDeclaration decl && !DOMTypeDeclarationLocator.matchSearchForTypeSuffix(decl, this.locator.pattern.typeSuffix)) {
			return toResponse(IMPOSSIBLE_MATCH);
		}
		if (this.locator.pattern.superSimpleName == null) {
			int level = nodeSet.addMatch(node, this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
			return toResponse(level, true);
		}

		char[] typeRefSimpleName = null;
		if (node instanceof SimpleType simple) {
			if (simple.getName() instanceof SimpleName name) {
				typeRefSimpleName = name.getIdentifier().toCharArray();
			}
			if (simple.getName() instanceof QualifiedName name) {
				typeRefSimpleName = name.getName().getIdentifier().toCharArray();
			}
		} else if (node instanceof QualifiedType qualified) {
			typeRefSimpleName = qualified.getName().getIdentifier().toCharArray();
		}
		if (this.locator.matchesName(this.locator.pattern.superSimpleName, typeRefSimpleName)) {
			int level = nodeSet.addMatch(node, this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
			return toResponse(level, true);
		}

		return toResponse(IMPOSSIBLE_MATCH);
	}

	@Override
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (binding == null)
			return toResponse(INACCURATE_MATCH);
		if (!(binding instanceof ITypeBinding))
			return toResponse(IMPOSSIBLE_MATCH);

		var type = (ITypeBinding) binding;
		int level = IMPOSSIBLE_MATCH;
		if (this.locator.pattern.superRefKind != SuperTypeReferencePattern.ONLY_SUPER_INTERFACES || node.getLocationInParent() == TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY) {
			level = this.resolveLevelForType(this.locator.pattern.superSimpleName, this.locator.pattern.superQualification,
					type);
			if (level == ACCURATE_MATCH)
				return toResponse(ACCURATE_MATCH);
		}

		if (this.locator.pattern.superRefKind != SuperTypeReferencePattern.ONLY_SUPER_CLASSES || node.getLocationInParent() == TypeDeclaration.SUPERCLASS_TYPE_PROPERTY) {
			level = this.resolveLevelForType(this.locator.pattern.superSimpleName, this.locator.pattern.superQualification,
					type);
			if (level == ACCURATE_MATCH)
				return toResponse(ACCURATE_MATCH);
		}
		return toResponse(level);
	}
}
