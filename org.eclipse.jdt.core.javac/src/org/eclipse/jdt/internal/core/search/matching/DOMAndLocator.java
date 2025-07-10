/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. and others.
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
import java.util.Comparator;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.internal.core.search.DOMPatternLocatorFactory;
import org.eclipse.jdt.internal.core.search.LocatorResponse;

public class DOMAndLocator extends DOMPatternLocator {

	private final DOMPatternLocator[] children;

	public DOMAndLocator(AndLocator andLocator, AndPattern pattern) {
		super(pattern);
		children = Arrays.stream(children(andLocator))
			.map(child -> DOMPatternLocatorFactory.createWrapper(child, null)) // usually pattern would be inferred from locator so we can pass null
			.toArray(DOMPatternLocator[]::new);
	}

	private static PatternLocator[] children(AndLocator andLocator) {
		try {
			var locatorsField = AndLocator.class.getDeclaredField("patternLocators");
			locatorsField.setAccessible(true);
			return (PatternLocator[])locatorsField.get(andLocator);
		} catch (Exception ex) {
			ILog.get().error(ex.getMessage(), ex);
			return new PatternLocator[0];
		}
	}

	public LocatorResponse match(org.eclipse.jdt.core.dom.Annotation node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.ASTNode node, NodeSetWrapper nodeSet, MatchLocator locator) { // needed for some generic nodes
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.Expression node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.FieldDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.LambdaExpression node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(VariableDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.MethodDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.MemberValuePair node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(MethodInvocation node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	protected LocatorResponse match(org.eclipse.jdt.core.dom.ModuleDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(Name node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(FieldAccess node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(AbstractTypeDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(org.eclipse.jdt.core.dom.TypeParameter node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse match(Type node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return and(child -> child.match(node, nodeSet, locator));
	}
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, MatchLocator locator) {
		return and(child -> child.resolveLevel(node, locator));
	}
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		return and(child -> child.resolveLevel(node, binding, locator));
	}

	private LocatorResponse and(java.util.function.Function<DOMPatternLocator, LocatorResponse> query) {
		return Arrays.stream(this.children)
					.map(query::apply)
					.min(Comparator.comparingInt(LocatorResponse::level))
					.orElse(toResponse(PatternLocator.IMPOSSIBLE_MATCH));
	}

	@Override
	public void initializePolymorphicSearch(MatchLocator locator) {
		for (PatternLocator patternLocator : this.children)
			patternLocator.initializePolymorphicSearch(locator);
	}

}
