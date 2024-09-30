/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search;

import java.util.function.Function;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.core.search.matching.ConstructorLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMConstructorLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMFieldLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMLocalVariableLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMMethodLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMPackageReferenceLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMPatternLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMSuperTypeReferenceLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMTypeDeclarationLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMTypeParameterLocator;
import org.eclipse.jdt.internal.core.search.matching.DOMTypeReferenceLocator;
import org.eclipse.jdt.internal.core.search.matching.FieldLocator;
import org.eclipse.jdt.internal.core.search.matching.LocalVariableLocator;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.MethodLocator;
import org.eclipse.jdt.internal.core.search.matching.NodeSetWrapper;
import org.eclipse.jdt.internal.core.search.matching.PackageReferenceLocator;
import org.eclipse.jdt.internal.core.search.matching.PatternLocator;
import org.eclipse.jdt.internal.core.search.matching.SuperTypeReferenceLocator;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationLocator;
import org.eclipse.jdt.internal.core.search.matching.TypeParameterLocator;
import org.eclipse.jdt.internal.core.search.matching.TypeReferenceLocator;

/**
 * Visits an AST to feel the possible match with nodes
 */
class PatternLocatorVisitor extends ASTVisitor {

	private final PatternLocator patternLocator;
	private final NodeSetWrapper nodeSet;
	private MatchLocator locator;

	public PatternLocatorVisitor(PatternLocator patternLocator, NodeSetWrapper nodeSet, MatchLocator locator) {
		super(true);
		this.patternLocator = patternLocator;
		this.nodeSet = nodeSet;
		this.locator = locator;
	}
	
	private DOMPatternLocator getWrapper(PatternLocator locator) {
		// TODO implement all this. 
		if( locator instanceof FieldLocator fl) {
			return new DOMFieldLocator(fl);
		}
		if( locator instanceof ConstructorLocator cl) {
			return new DOMConstructorLocator(cl);
		}
		if( locator instanceof LocalVariableLocator lcl) {
			return new DOMLocalVariableLocator(lcl);
		}
		if( locator instanceof MethodLocator ml) {
			return new DOMMethodLocator(ml);
		}
		if( locator instanceof PackageReferenceLocator prl) {
			return new DOMPackageReferenceLocator(prl);
		}
		if( locator instanceof SuperTypeReferenceLocator strl) {
			return new DOMSuperTypeReferenceLocator(strl);
		}
		if( locator instanceof TypeDeclarationLocator tdl) {
			return new DOMTypeDeclarationLocator(tdl);
		}
		if( locator instanceof TypeParameterLocator tpl) {
			return new DOMTypeParameterLocator(tpl);
		}
		if( locator instanceof TypeReferenceLocator trl) {
			return new DOMTypeReferenceLocator(trl);
		}
		return new DOMPatternLocator(null); // stub
	}

	private <T extends ASTNode> boolean defaultVisitImplementation(T node, Function<T, Integer> levelFunc) {
		return defaultVisitImplementationWithFunc(node, levelFunc, DOMASTNodeUtils::getBinding);
	}

	private <T extends ASTNode> boolean defaultVisitImplementationWithFunc(
			T node,
			Function<T, Integer> levelFunc,
			Function<T, IBinding> bindingFunc) {
		int level = levelFunc.apply(node);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.getWrapped().mustResolve || this.patternLocator.isMustResolve())) {
			level = getWrapper(this.patternLocator).resolveLevel(node, bindingFunc.apply(node), this.locator);
		}
		this.nodeSet.addMatch(node, level);
		return true;

	}


	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(TypeParameter node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(MethodDeclaration node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(MethodInvocation node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(ExpressionMethodReference node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(SuperMethodReference node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(SuperMethodInvocation node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}

	private boolean visitAbstractTypeDeclaration(AbstractTypeDeclaration node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(EnumDeclaration node) {
		return visitAbstractTypeDeclaration(node);
	}
	@Override
	public boolean visit(TypeDeclaration node) {
		return visitAbstractTypeDeclaration(node);
	}
	@Override
	public boolean visit(RecordDeclaration node) {
		return visitAbstractTypeDeclaration(node);
	}
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}

	private boolean visitType(Type node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(SimpleType type) {
		visitType(type);
		Name n = type.getName();
		if( n instanceof QualifiedName qn ) {
			Name qualifier = qn.getQualifier();
			if( qualifier instanceof SimpleName sn1 ) {
				visit(sn1);
			} else if( qualifier instanceof QualifiedName qn1) {
				visit(qn1);
			}
		}
		return false; // No need to visit single name child
	}
	@Override
	public boolean visit(QualifiedType type) {
		return visitType(type);
	}
	@Override
	public boolean visit(NameQualifiedType type) {
		return visitType(type);
	}
	@Override
	public boolean visit(ParameterizedType node) {
		return visitType(node);
	}
	@Override
	public boolean visit(IntersectionType node) {
		return visitType(node);
	}
	@Override
	public boolean visit(UnionType node) {
		return visitType(node);
	}
	@Override
	public boolean visit(ClassInstanceCreation node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(CreationReference node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(SimpleName node) {
		if (
			node.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY ||
			node.getLocationInParent() == SingleVariableDeclaration.NAME_PROPERTY ||
			node.getLocationInParent() == TypeDeclaration.NAME_PROPERTY ||
			node.getLocationInParent() == EnumDeclaration.NAME_PROPERTY ||
			node.getLocationInParent() == MethodDeclaration.NAME_PROPERTY) {
			return false; // skip as parent was most likely already matched
		}
		int level = getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.getWrapped().mustResolve || this.patternLocator.isMustResolve())) {
			IBinding b = node.resolveBinding();
			level = getWrapper(this.patternLocator).resolveLevel(node, b, this.locator);
		}
		this.nodeSet.addMatch(node, level);
		return level == 0;
	}
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		return defaultVisitImplementation(node, x -> getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator));
	}
	@Override
	public boolean visit(EnumConstantDeclaration node) {
		int level = getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.getWrapped().mustResolve || this.patternLocator.isMustResolve())) {
			int l1 = getWrapper(this.patternLocator).resolveLevel(node, node.resolveVariable(), this.locator);
			int l2 = getWrapper(this.patternLocator).resolveLevel(node, node.resolveConstructorBinding(), this.locator);
			level = Math.max(l1, l2);
		}
		this.nodeSet.addMatch(node, level);
		return true;
	}
	@Override
	public boolean visit(QualifiedName node) {
		if (node.getLocationInParent() == SimpleType.NAME_PROPERTY) {
			return false; // type was already checked
		}
		int level = getWrapper(this.patternLocator).match(node, this.nodeSet, this.locator);
		if ((level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.POSSIBLE_MATCH && (this.nodeSet.getWrapped().mustResolve || this.patternLocator.isMustResolve())) {
			level = getWrapper(this.patternLocator).resolveLevel(node, node.resolveBinding(), this.locator);
		}
		this.nodeSet.addMatch(node, level);
		if( (level & PatternLocator.MATCH_LEVEL_MASK) == PatternLocator.IMPOSSIBLE_MATCH ) {
			return true;
		}
		return false;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		return true;
	}
}
