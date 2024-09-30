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

import java.util.Objects;

import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclaration;

public class DOMLocalVariableLocator extends DOMPatternLocator {

	private LocalVariableLocator locator;

	public DOMLocalVariableLocator(LocalVariableLocator lcl) {
		super(lcl.pattern);
		this.locator = lcl;
	}

	@Override
	public int match(Name node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (node.getLocationInParent() instanceof ChildPropertyDescriptor descriptor
				&& (descriptor.getChildType() == Expression.class // local variable refs are either expressions as
																	// children
						|| descriptor == QualifiedName.QUALIFIER_PROPERTY) // or dereferenced names
				&& node instanceof SimpleName simple // local variables cannot be qualified
				&& this.locator.getLocalVariable().getElementName().equals(simple.getIdentifier())) {
			return POSSIBLE_MATCH;
		}
		return IMPOSSIBLE_MATCH;
	}

	@Override
	public int resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (!(binding instanceof IVariableBinding)) {
			return IMPOSSIBLE_MATCH;
		}
		if (Objects.equals(binding.getJavaElement(), this.locator.getLocalVariable())) {
			return ACCURATE_MATCH;
		}
		return INACCURATE_MATCH;
	}

	@Override
	public int match(VariableDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		int referencesLevel = IMPOSSIBLE_MATCH;
		if (this.locator.pattern.findReferences)
			// must be a write only access with an initializer
			if (this.locator.pattern.writeAccess && !this.locator.pattern.readAccess && node.getInitializer() != null)
				if (this.locator.matchesName(this.locator.pattern.name, node.getName().getIdentifier().toCharArray()))
					referencesLevel = this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;

		int declarationsLevel = IMPOSSIBLE_MATCH;
		if (this.locator.pattern.findDeclarations)
			if (this.locator.matchesName(this.locator.pattern.name, node.getName().getIdentifier().toCharArray()))
				if (node.getStartPosition() == this.locator.getLocalVariable().declarationSourceStart)
					declarationsLevel = this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;

		// Use the stronger match
		return nodeSet.addMatch(node, referencesLevel >= declarationsLevel ? referencesLevel : declarationsLevel); 
	}
}
