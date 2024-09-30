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

import java.util.Arrays;

import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

public class DOMPackageReferenceLocator extends DOMPatternLocator {

	private PackageReferenceLocator locator;

	public DOMPackageReferenceLocator(PackageReferenceLocator locator) {
		super(locator.pattern);
		this.locator = locator;
	}

	@Override
	public int match(org.eclipse.jdt.core.dom.Annotation node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return match(node.getTypeName(), nodeSet, locator);
	}

	@Override
	public int match(org.eclipse.jdt.core.dom.ASTNode node, NodeSetWrapper nodeSet, MatchLocator locator) { // interested
																												// in
																												// ImportReference
		if (node instanceof ImportDeclaration decl) {
			return match(decl.getName(), nodeSet, locator);
		}
		return IMPOSSIBLE_MATCH;
	}

	@Override
	public int match(Name node, NodeSetWrapper nodeSet, MatchLocator locator) {
		// interested in QualifiedNameReference
		char[][] arr = Arrays.stream(node.getFullyQualifiedName().split("\\.")).map(String::toCharArray) //$NON-NLS-1$
				.toArray(char[][]::new);
		return nodeSet.addMatch(node, this.locator.matchLevelForTokens(arr));
	}

	@Override
	public int match(Type node, NodeSetWrapper nodeSet, MatchLocator locator) { // interested in QualifiedTypeReference
																					// only
		if (node instanceof ArrayType att) {
			return match(att.getElementType(), nodeSet, locator);
		}
		Name typePkg = null;
		if (node instanceof SimpleType stt) {
			Name n = stt.getName();
			typePkg = n instanceof QualifiedName qn ? qn.getQualifier() : n;
		} else if (node instanceof QualifiedType qt3) {
			Type t1 = qt3.getQualifier();
			typePkg = t1 instanceof SimpleType sttt ? sttt.getName() : null;
		} else if (node instanceof NameQualifiedType qt) {
			typePkg = qt.getQualifier();
		}
		return typePkg != null ? match(typePkg, nodeSet, locator) : IMPOSSIBLE_MATCH;
	}

	@Override
	public int resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (binding instanceof IPackageBinding ipb) {
			String n = ipb.getName();
			String patternName = new String(this.locator.pattern.pkgName);
			if (patternName.equals(n)) {
				return ACCURATE_MATCH;
			}
		}
		return IMPOSSIBLE_MATCH;
	}

}
