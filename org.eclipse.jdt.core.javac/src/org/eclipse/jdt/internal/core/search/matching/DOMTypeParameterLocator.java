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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.core.search.LocatorResponse;

public class DOMTypeParameterLocator extends DOMPatternLocator {

	private TypeParameterLocator locator;

	public DOMTypeParameterLocator(TypeParameterLocator locator) {
		super(locator.pattern);
		this.locator = locator;
	}

	private static boolean nodeSourceRangeMatchesElement(org.eclipse.jdt.core.dom.ASTNode node, IJavaElement focus) {
		if( focus == null )
			return false;

		ISourceRange sr = null;
		try {
			if( focus instanceof ISourceReference isr2) {
				sr = isr2.getSourceRange();
			}
		} catch(JavaModelException jme3) {
			// ignore
		}

		if( sr == null )
			return false;

		if( sr.getOffset() == node.getStartPosition() && sr.getLength() == node.getLength()) {
			return true;
		}
		return false;
	}

	@Override
	public LocatorResponse match(Type node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (this.locator.pattern.findReferences) {
			if (node instanceof SimpleType simple) { // Type parameter cannot be qualified
				if (this.locator.matchesName(this.locator.pattern.name, simple.getName().toString().toCharArray())) {
					int level = this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
					return toResponse(nodeSet.addMatch(node, level), true);
				}
			}
		}
		return toResponse(IMPOSSIBLE_MATCH);
	}

	@Override
	public LocatorResponse match(org.eclipse.jdt.core.dom.TypeParameter node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (this.locator.pattern.findReferences) {
			if (this.locator.matchesName(this.locator.pattern.name, node.getName().toString().toCharArray())) {
				int level = this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
				return toResponse(nodeSet.addMatch(node, level), true);
			}
		}
		if (this.locator.pattern.findDeclarations) {
			if (this.locator.matchesName(this.locator.pattern.name, node.getName().toString().toCharArray())) {
				int level = this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
				return toResponse(nodeSet.addMatch(node, level), true);
			}
		}
		return toResponse(IMPOSSIBLE_MATCH);
	}
	@Override
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (binding == null) return toResponse(INACCURATE_MATCH);
		if (!(binding instanceof ITypeBinding)) return toResponse(IMPOSSIBLE_MATCH);
		ITypeBinding tb = (ITypeBinding)binding;
		int ret = matchTypeParameter(tb, true);
		if( ret == ACCURATE_MATCH) {
			if( !this.locator.pattern.findDeclarations && nodeSourceRangeMatchesElement(node, this.locator.pattern.focus)) {
				return toResponse(IMPOSSIBLE_MATCH);
			}
		}
		return toResponse(ret);
	}
	protected int matchTypeParameter(ITypeBinding variable, boolean matchName) {
		if (variable.getDeclaringMethod() != null) {
			var methBinding  = variable.getDeclaringMethod();
			if (this.locator.matchesName(methBinding.getDeclaringClass().getName().toCharArray(), this.locator.pattern.methodDeclaringClassName) &&
				(methBinding.isConstructor() || this.locator.matchesName(methBinding.getName().toCharArray(), this.locator.pattern.declaringMemberName))) {
				int length = this.locator.pattern.methodArgumentTypes==null ? 0 : this.locator.pattern.methodArgumentTypes.length;
				if (methBinding.getParameterTypes() == null) {
					if (length == 0) return ACCURATE_MATCH;
				} else if (methBinding.getParameterTypes().length == length){
					ITypeBinding[] p = methBinding.getParameterTypes();
					for (int i=0; i<length; i++) {
						if (!this.locator.matchesName(this.locator.pattern.methodArgumentTypes[i], p[i].getName().toCharArray())) {
							return IMPOSSIBLE_MATCH;
						}
					}
					return ACCURATE_MATCH;
				}
			}
		}
		if (variable.getDeclaringMember() != null && this.locator.matchesName(variable.getDeclaringMember().getName().toCharArray(), this.locator.pattern.declaringMemberName)) {
			return ACCURATE_MATCH;
		}
		return IMPOSSIBLE_MATCH;
	}


}
