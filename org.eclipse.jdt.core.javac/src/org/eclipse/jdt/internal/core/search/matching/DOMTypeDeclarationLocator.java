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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class DOMTypeDeclarationLocator extends DOMPatternLocator {

	private TypeDeclarationLocator locator;

	public DOMTypeDeclarationLocator(TypeDeclarationLocator locator) {
		super(locator.pattern);
		this.locator = locator;
	}
	@Override
	public int match(AbstractTypeDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (this.locator.pattern.simpleName == null || this.locator.matchesName(this.locator.pattern.simpleName, node.getName().getIdentifier().toCharArray()))
			return nodeSet.addMatch(node, this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);

		return IMPOSSIBLE_MATCH;
	}
	@Override
	public int resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (binding == null) return INACCURATE_MATCH;
		if (!(binding instanceof ITypeBinding)) return IMPOSSIBLE_MATCH;

		ITypeBinding type = (ITypeBinding) binding;

		switch (this.locator.pattern.typeSuffix) {
			case IIndexConstants.CLASS_SUFFIX:
				if (!type.isClass()) return IMPOSSIBLE_MATCH;
				break;
			case IIndexConstants.CLASS_AND_INTERFACE_SUFFIX:
				if (!(type.isClass() || (type.isInterface() && !type.isAnnotation()))) return IMPOSSIBLE_MATCH;
				break;
			case IIndexConstants.CLASS_AND_ENUM_SUFFIX:
				if (!(type.isClass() || type.isEnum())) return IMPOSSIBLE_MATCH;
				break;
			case IIndexConstants.INTERFACE_SUFFIX:
				if (!type.isInterface() || type.isAnnotation()) return IMPOSSIBLE_MATCH;
				break;
			case IIndexConstants.INTERFACE_AND_ANNOTATION_SUFFIX:
				if (!(type.isInterface() || type.isAnnotation())) return IMPOSSIBLE_MATCH;
				break;
			case IIndexConstants.ENUM_SUFFIX:
				if (!type.isEnum()) return IMPOSSIBLE_MATCH;
				break;
			case IIndexConstants.ANNOTATION_TYPE_SUFFIX:
				if (!type.isAnnotation()) return IMPOSSIBLE_MATCH;
				break;
			case IIndexConstants.TYPE_SUFFIX : // nothing
		}

		if (this.matchModule(this.locator.pattern, type) == IMPOSSIBLE_MATCH) {
			return IMPOSSIBLE_MATCH;
		}
		// fully qualified name
		if (this.locator.pattern instanceof QualifiedTypeDeclarationPattern) {
			QualifiedTypeDeclarationPattern qualifiedPattern = (QualifiedTypeDeclarationPattern) this.locator.pattern;
			return this.resolveLevelForType(qualifiedPattern.simpleName, qualifiedPattern.qualification, type);
		} else {
			char[] enclosingTypeName = this.locator.pattern.enclosingTypeNames == null ? null : CharOperation.concatWith(this.locator.pattern.enclosingTypeNames, '.');
			return resolveLevelForType(this.locator.pattern.simpleName, this.locator.pattern.pkg, enclosingTypeName, type);
		}
	}
	protected int resolveLevelForType(char[] simpleNamePattern, char[] qualificationPattern, char[] enclosingNamePattern, ITypeBinding type) {
		if (enclosingNamePattern == null)
			return this.resolveLevelForType(simpleNamePattern, qualificationPattern, type);
		if (qualificationPattern == null)
			return this.resolveLevelForType(simpleNamePattern, enclosingNamePattern, type);

		// pattern was created from a Java element: qualification is the package name.
		char[] fullQualificationPattern = CharOperation.concat(qualificationPattern, enclosingNamePattern, '.');
		if (CharOperation.equals(this.locator.pattern.pkg, type.getPackage().getName().toCharArray()))
			return this.resolveLevelForType(simpleNamePattern, fullQualificationPattern, type);
		return IMPOSSIBLE_MATCH;
	}
	private int matchModule(TypeDeclarationPattern typePattern, ITypeBinding type) {
		IModuleBinding module = type.getModule();
		if (module == null || module.getName() == null || typePattern.moduleNames == null)
			return POSSIBLE_MATCH; //can't determine, say possible to all.
		String bindModName = module.getName();

		if (typePattern.modulePatterns == null) {// use 'normal' matching
			char[][] moduleList = this.locator.getModuleList(typePattern);
			for (char[] m : moduleList) { // match any in the list
				int ret = this.locator.matchNameValue(m, bindModName.toCharArray());
				if (ret != IMPOSSIBLE_MATCH) return ret;
			}
		} else {// use pattern matching
			for (Pattern p : typePattern.modulePatterns) {
				Matcher matcher = p.matcher(bindModName);
				if (matcher.matches()) return ACCURATE_MATCH;
			}
		}
		return IMPOSSIBLE_MATCH;
	}

}
