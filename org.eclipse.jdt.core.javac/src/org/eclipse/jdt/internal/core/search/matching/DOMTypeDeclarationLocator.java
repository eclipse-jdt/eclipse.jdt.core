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
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.search.LocatorResponse;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class DOMTypeDeclarationLocator extends DOMPatternLocator {

	private TypeDeclarationLocator locator;

	public DOMTypeDeclarationLocator(TypeDeclarationLocator locator) {
		super(locator.pattern);
		this.locator = locator;
	}
	@Override
	public LocatorResponse match(AbstractTypeDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (!matchSearchForTypeSuffix(node, this.locator.pattern.typeSuffix)) {
			return toResponse(IMPOSSIBLE_MATCH);
		}
		if (this.locator.pattern.simpleName == null || this.locator.matchesName(this.locator.pattern.simpleName, node.getName().getIdentifier().toCharArray())) {
			int level = nodeSet.addMatch(node, this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
			return toResponse(level, true);
		}

		return toResponse(IMPOSSIBLE_MATCH);
	}
	@Override
	public LocatorResponse match(LambdaExpression node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return toResponse(POSSIBLE_MATCH);
	}

	static boolean matchSearchForTypeSuffix(AbstractTypeDeclaration type, char typeSuffix) {
		return switch (typeSuffix) {
			case IIndexConstants.CLASS_SUFFIX -> type instanceof TypeDeclaration decl && !decl.isInterface();
			case IIndexConstants.CLASS_AND_INTERFACE_SUFFIX -> type instanceof TypeDeclaration;
			case IIndexConstants.CLASS_AND_ENUM_SUFFIX -> (type instanceof TypeDeclaration decl && !decl.isInterface()) || type instanceof EnumDeclaration;
			case IIndexConstants.INTERFACE_SUFFIX -> type instanceof TypeDeclaration decl && decl.isInterface();
			case IIndexConstants.INTERFACE_AND_ANNOTATION_SUFFIX -> (type instanceof TypeDeclaration decl && !decl.isInterface()) || type instanceof AnnotationTypeDeclaration;
			case IIndexConstants.ENUM_SUFFIX -> type instanceof EnumDeclaration;
			case IIndexConstants.ANNOTATION_TYPE_SUFFIX -> type instanceof AnnotationTypeDeclaration;
			case IIndexConstants.TYPE_SUFFIX -> true;
			default -> false;
		};
	}

	static boolean matchSearchForTypeSuffix(ITypeBinding type, char typeSuffix) {
		return switch (typeSuffix) {
			case IIndexConstants.CLASS_SUFFIX -> type.isClass();
			case IIndexConstants.CLASS_AND_INTERFACE_SUFFIX -> type.isClass() || (type.isInterface() && !type.isAnnotation());
			case IIndexConstants.CLASS_AND_ENUM_SUFFIX -> type.isClass() || type.isEnum();
			case IIndexConstants.INTERFACE_SUFFIX -> type.isInterface() && !type.isAnnotation();
			case IIndexConstants.INTERFACE_AND_ANNOTATION_SUFFIX -> type.isInterface() || type.isAnnotation();
			case IIndexConstants.ENUM_SUFFIX -> type.isEnum();
			case IIndexConstants.ANNOTATION_TYPE_SUFFIX -> type.isAnnotation();
			case IIndexConstants.TYPE_SUFFIX -> true;
			default -> false;
		};
	}

	@Override
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (binding == null) return toResponse(INACCURATE_MATCH);
		if (!(binding instanceof ITypeBinding)) return toResponse(IMPOSSIBLE_MATCH);

		ITypeBinding type = (ITypeBinding) binding;

		if (!matchSearchForTypeSuffix(type, this.locator.pattern.typeSuffix)) {
			return toResponse(IMPOSSIBLE_MATCH);
		}

		if (this.matchModule(this.locator.pattern, type) == IMPOSSIBLE_MATCH) {
			return toResponse(IMPOSSIBLE_MATCH);
		}
		// fully qualified name
		if (this.locator.pattern instanceof QualifiedTypeDeclarationPattern) {
			QualifiedTypeDeclarationPattern qualifiedPattern = (QualifiedTypeDeclarationPattern) this.locator.pattern;
			int level = this.resolveLevelForType(qualifiedPattern.simpleName, qualifiedPattern.qualification, type);
			return toResponse(level);
		} else {
			char[] enclosingTypeName = this.locator.pattern.enclosingTypeNames == null ? null : CharOperation.concatWith(this.locator.pattern.enclosingTypeNames, '.');
			int level = resolveLevelForType(this.locator.pattern.simpleName, this.locator.pattern.pkg, enclosingTypeName, type);
			return toResponse(level);
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
