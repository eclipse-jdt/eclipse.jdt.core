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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.search.LocatorResponse;

public class DOMLocalVariableLocator extends DOMPatternLocator {

	private LocalVariableLocator locator;
	private List<LocalVariable> foundDeclarations = new ArrayList<>();

	public DOMLocalVariableLocator(LocalVariableLocator lcl) {
		super(lcl.pattern);
		this.locator = lcl;
	}

	private LocalVariable getLocalVariable() {
		if( this.locator.pattern instanceof LocalVariablePattern lvp )
			return lvp.localVariable;
		return null;
	}

	@Override
	public LocatorResponse match(Name node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (node.getLocationInParent() instanceof ChildPropertyDescriptor descriptor
				// local variable refs are either expressions as children
				&& (descriptor.getChildType() == Expression.class
						// or dereferenced names
						|| descriptor == QualifiedName.QUALIFIER_PROPERTY)
				// local variables cannot be qualified
				&& node instanceof SimpleName simple
				&& Objects.equals(getLocalVariable() == null ? null : getLocalVariable().getElementName(), simple.getIdentifier())) {
			return toResponse(POSSIBLE_MATCH);
		}
		return toResponse(IMPOSSIBLE_MATCH);
	}

	@Override
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (!(binding instanceof IVariableBinding)) {
			return toResponse(IMPOSSIBLE_MATCH);
		}
		Object bindingElement = binding.getJavaElement();
		LocalVariable localVar = getLocalVariable();
		if (Objects.equals(bindingElement, localVar)) {
			// We need to know if this is a reference request or a declaration request
			if (this.locator.pattern.findReferences) {
				return new LocatorResponse(ACCURATE_MATCH, false, node, false, false);
			} else if (this.locator.pattern.findDeclarations) {
				// we need to make sure the node has a VariableDeclaration in its ancestry
				boolean isDecl = hasVariableDeclarationAncestor(node);
				if( isDecl) {
					if( !alreadyFound(localVar)) {
						foundDeclarations.add(localVar);
						return new LocatorResponse(ACCURATE_MATCH, false, node, false, false);
					}
				}
				return toResponse(IMPOSSIBLE_MATCH);
			}
		}
		return toResponse(IMPOSSIBLE_MATCH);
	}

	private boolean hasVariableDeclarationAncestor(ASTNode node) {
		ASTNode working = node;
		while(working != null ) {
			if( working instanceof VariableDeclaration) {
				return true;
			}
			working = working.getParent();
		}
		return false;
	}

	@Override
	public LocatorResponse match(VariableDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		int defaultLevelOnMatch = this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
		if (this.locator.pattern.findReferences) {
			// must be a write only access with an initializer
			if (this.locator.pattern.writeAccess && !this.locator.pattern.readAccess && node.getInitializer() != null) {
				if (this.locator.matchesName(this.locator.pattern.name, node.getName().getIdentifier().toCharArray())) {
					return toResponse(defaultLevelOnMatch, false);
				}
			}
		}

		if (this.locator.pattern.findDeclarations) {
			if (this.locator.matchesName(this.locator.pattern.name, node.getName().getIdentifier().toCharArray())) {
				LocalVariable lvFromPattern = getLocalVariable();
				if (lvFromPattern != null ) {
					if(node.getStartPosition() == lvFromPattern.declarationSourceStart) {
						return toResponse(defaultLevelOnMatch, false);
					} else if( node.getName().getStartPosition() == lvFromPattern.nameStart) {
						return new LocatorResponse(defaultLevelOnMatch, true, node.getName(), false, false);
					}
				}
			}
		}
		return toResponse(0, false);
	}

	@Override
	public void reportSearchMatch(MatchLocator locator, ASTNode node, SearchMatch match) throws CoreException {
		SearchMatch matchToReport = match;
		if(this.locator.pattern.findDeclarations && hasVariableDeclarationAncestor(node) ) {
			LocalVariable localVariable = getLocalVariable();
			int offset = localVariable.nameStart;
			int length = localVariable.nameEnd-offset+1;
			IJavaElement element = localVariable;
			matchToReport = locator.newDeclarationMatch(element, null, match.getAccuracy(), offset, length);
		}
		if( matchToReport != null) {
			SearchMatchingUtility.reportSearchMatch(locator, matchToReport);
		}
	}

	private boolean alreadyFound(LocalVariable matchToReport) {
		boolean found = foundDeclarations.stream().filter(x -> matchToReport.toString().equals(x.toString())).findFirst().isPresent();
		return found;
	}

}
