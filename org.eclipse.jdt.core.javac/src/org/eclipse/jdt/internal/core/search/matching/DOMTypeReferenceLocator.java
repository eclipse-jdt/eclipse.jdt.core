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

import static org.eclipse.jdt.internal.core.search.DOMASTNodeUtils.insideDocComment;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.internal.core.search.DOMASTNodeUtils;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class DOMTypeReferenceLocator extends DOMPatternLocator {

	private TypeReferenceLocator locator;
	private List<IJavaElement> foundElements = new ArrayList<>();
	
	public DOMTypeReferenceLocator(TypeReferenceLocator locator) {
		super(locator.pattern);
		this.locator = locator;
	}
	private boolean hasPackageDeclarationAncestor(org.eclipse.jdt.core.dom.ASTNode node) {
		if( node instanceof PackageDeclaration) {
			return true;
		}
		return node == null ? false : hasPackageDeclarationAncestor(node.getParent());
	}




	@Override
	public int match(org.eclipse.jdt.core.dom.Annotation node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return match(node.getTypeName(), nodeSet, locator);
	}
	@Override
	public int match(Name name, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (name.getParent() instanceof AbstractTypeDeclaration) {
			return IMPOSSIBLE_MATCH;
		}
		if( name.getParent() instanceof LabeledStatement ls && ls.getLabel() == name) {
			return IMPOSSIBLE_MATCH;
		}
		if( name.getParent() instanceof BreakStatement bs && bs.getLabel() == name) {
			return IMPOSSIBLE_MATCH;
		}
		if (failsFineGrain(name, this.locator.fineGrain())) {
			return IMPOSSIBLE_MATCH;
		}
		if (this.locator.pattern.simpleName == null) {
			return nodeSet.addMatch(name, this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
		}
		if( name instanceof SimpleName sn2 ) {
			if( this.locator.pattern.qualification == null)
				return match(sn2, nodeSet);
			// searching for a qualified name but we are only simple
			org.eclipse.jdt.core.dom.ASTNode parent3 = name.getParent();
			if( !(parent3 instanceof QualifiedName)) {
				return match(sn2, nodeSet);
			}
			// Parent is a qualified name and we didn't match it...
			// so we know the whole name was a failed match, but...
			if( parent3 instanceof QualifiedName qn3 && qn3.getQualifier() == name) {
				// Maybe the qualifier is the type we're looking for
				if( match(sn2, nodeSet) == POSSIBLE_MATCH) {
					return POSSIBLE_MATCH;
				}
			}

			if( this.locator.pattern.getMatchMode() == SearchPattern.R_EXACT_MATCH) {
				return IMPOSSIBLE_MATCH;
			}
			if( match(sn2, nodeSet) == POSSIBLE_MATCH) {
				return POSSIBLE_MATCH;
			}
			return IMPOSSIBLE_MATCH;
		}
		if( name instanceof QualifiedName qn2 ) {
			return match(qn2, nodeSet);
		}
		return IMPOSSIBLE_MATCH;
	}
	@Override
	public int match(org.eclipse.jdt.core.dom.ASTNode node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (failsFineGrain(node, this.locator.fineGrain())) {
			return IMPOSSIBLE_MATCH;
		}
		if (node instanceof EnumConstantDeclaration enumConstantDecl
			&& node.getParent() instanceof EnumDeclaration enumDeclaration
			&& enumConstantDecl.getAnonymousClassDeclaration() != null) {
			if (this.locator.pattern.simpleName == null) {
				return nodeSet.addMatch(node, this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
			}
			if (this.locator.matchesName(this.locator.pattern.simpleName, enumDeclaration.getName().getIdentifier().toCharArray())) {
				return nodeSet.addMatch(node, this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
			}
		}
		return IMPOSSIBLE_MATCH;
	}
	@Override
	public int match(Type node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (failsFineGrain(node, this.locator.fineGrain())) {
			return IMPOSSIBLE_MATCH;
		}
		if (this.locator.pattern.simpleName == null)
			return nodeSet.addMatch(node, this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
		String qualifiedName = null;
		String simpleName = null;
		if (node instanceof SimpleType simple) {
			if (simple.getName() instanceof SimpleName name) {
				simpleName = name.getIdentifier();
			}
			if (simple.getName() instanceof QualifiedName name) {
				simpleName = name.getName().getIdentifier();
				qualifiedName = name.getFullyQualifiedName();
			}
		} else if (node instanceof QualifiedType qualified) {
			simpleName = qualified.getName().getIdentifier();
			qualifiedName = qualified.getName().getFullyQualifiedName();
		}
		if( qualifiedName != null && this.locator.pattern.qualification != null) {
			// we have a qualified name in the node, and our pattern is searching for a qualified name
			char[] patternQualified = (new String(this.locator.pattern.qualification) + "." + new String(this.locator.pattern.simpleName)).toCharArray();
			char[] found = qualifiedName.toCharArray();
			if( this.locator.matchesName(patternQualified, found)) {
				return nodeSet.addMatch(node, this.locator.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
			}
		} else if (simpleName != null && this.locator.matchesName(this.locator.pattern.simpleName, simpleName.toCharArray())) {
			return nodeSet.addMatch(node, this.locator.pattern.mustResolve || this.locator.pattern.qualification == null ? POSSIBLE_MATCH : ACCURATE_MATCH);
		}
		return IMPOSSIBLE_MATCH;
	}
	@Override
	public int resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (binding == null) {
			if( node instanceof SimpleName sn) {
				int accuracy = resolveLevelForSimpleName(node, sn.getIdentifier());
				if( accuracy != -1 ) {
					// Add directly
					IResource r = null;
					IJavaElement enclosing = DOMASTNodeUtils.getEnclosingJavaElement(node);
					IJavaElement ancestor = enclosing == null ? null : enclosing.getAncestor(IJavaElement.COMPILATION_UNIT);
					try {
						r = ancestor == null ? null : ancestor.getCorrespondingResource();
					} catch(JavaModelException jme) {
						// ignore
					}

					TypeReferenceMatch typeMatch = new TypeReferenceMatch(enclosing, accuracy, node.getStartPosition(), node.getLength(), insideDocComment(node), locator.getParticipant(), r);
					try {
						locator.report(typeMatch);
					} catch(CoreException ce) {
						// ignore
					}
					// Then return not possible so it doesn't get added again
					return IMPOSSIBLE_MATCH;
				}
			}
			return INACCURATE_MATCH;
		}
		if (binding instanceof ITypeBinding typeBinding) {
			return resolveLevelForTypeBinding(node, typeBinding, locator);
		}
		if( binding instanceof IPackageBinding && node instanceof SimpleName sn) {
			// var x = (B36479.C)val;
			// might interpret the B36479 to be a package and C a type,
			// rather than B36479 to be a type and C to be an inner-type
			if( this.locator.isDeclarationOfReferencedTypesPattern) {
				return IMPOSSIBLE_MATCH;
			}
			if( hasPackageDeclarationAncestor(node)) {
				return IMPOSSIBLE_MATCH;
			}
			String identifier = sn.getIdentifier();
			if( this.locator.matchesName(this.locator.pattern.simpleName, identifier.toCharArray())) {
				return INACCURATE_MATCH;
			}

		}
		return IMPOSSIBLE_MATCH;
	}
	
	private static boolean failsFineGrain(ASTNode node, int fineGrain) {
		if (fineGrain == 0) {
			return false;
		}
		if ((fineGrain & IJavaSearchConstants.INSTANCEOF_TYPE_REFERENCE) != 0) {
			ASTNode cursor = node;
			while (cursor != null && !(cursor instanceof InstanceofExpression)) {
				cursor = cursor.getParent();
			}
			if (cursor == null) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Returns a match flag OR -1 if it cannot determine at all.
	 */
	private int resolveLevelForSimpleName(org.eclipse.jdt.core.dom.ASTNode node, String simpleNameNeedle) {
		if( !simpleNameNeedle.contains(".") && this.locator.pattern.qualification != null && this.locator.pattern.qualification.length > 0 ) { //$NON-NLS-1$
			// we need to find out if we import this thing at all
			org.eclipse.jdt.core.dom.CompilationUnit cu = findCU(node);
			List imports = cu.imports();
			for( Object id : imports) {
				ImportDeclaration idd = (ImportDeclaration)id;
				if( idd.getName() instanceof QualifiedName qn) {
					if( qn.getName().toString().equals(simpleNameNeedle)) {
						char[] qualifiedPattern = this.locator.getQualifiedPattern(this.locator.pattern.simpleName, this.locator.pattern.qualification);
						// we were imported as qualified name...
						int level3 = this.resolveLevelForTypeSourceName(qualifiedPattern, qn.toString().toCharArray(), null);
						if( level3 == ACCURATE_MATCH ) {
							return INACCURATE_MATCH;
						}
						return INACCURATE_MATCH;
					}
				}
			}
		}
		return -1;
	}

	private int resolveLevelForTypeBinding(org.eclipse.jdt.core.dom.ASTNode node, ITypeBinding typeBinding,
			MatchLocator locator) {
		int newLevel = this.resolveLevelForType(this.locator.pattern.simpleName, this.locator.pattern.qualification, typeBinding);
		if( newLevel == IMPOSSIBLE_MATCH ) {
			String qualNameFromBinding = typeBinding.getQualifiedName();
			int simpleNameMatch = resolveLevelForSimpleName(node, qualNameFromBinding);
			if( simpleNameMatch != -1 ) {
				return simpleNameMatch;
			}
		}
		if( this.locator.isDeclarationOfReferencedTypesPattern) {
			IJavaElement enclosing = ((DeclarationOfReferencedTypesPattern)this.locator.pattern).enclosingElement;
			// We don't add this node. We manually add the declaration
			ITypeBinding t2 = typeBinding.getTypeDeclaration();
			IJavaElement je = t2 == null ? null : t2.getJavaElement();
			if( je != null && !this.foundElements.contains(je) && DOMASTNodeUtils.isWithinRange(node, enclosing)) {
				ISourceReference sr = je instanceof ISourceReference ? (ISourceReference)je : null;
				IResource r = null;
				ISourceRange srg = null;
				ISourceRange nameRange = null;
				try {
					srg = sr.getSourceRange();
					nameRange = sr.getNameRange();
					IJavaElement ancestor = je.getAncestor(IJavaElement.COMPILATION_UNIT);
					r = ancestor == null ? null : ancestor.getCorrespondingResource();
				} catch(JavaModelException jme) {
					// ignore
				}
				ISourceRange rangeToUse = (nameRange == null) ? srg : nameRange;
				if( rangeToUse != null ) {
					TypeDeclarationMatch tdm = new TypeDeclarationMatch(je, newLevel,
							rangeToUse.getOffset(), rangeToUse.getLength(),
							locator.getParticipant(), r);
					try {
						this.foundElements.add(je);
						locator.report(tdm);
					} catch(CoreException ce) {
						// ignore
					}
				}
			}
			return IMPOSSIBLE_MATCH;
		}
		return newLevel;
	}

	private org.eclipse.jdt.core.dom.CompilationUnit findCU(org.eclipse.jdt.core.dom.ASTNode node) {
		if( node == null )
			return null;
		if( node instanceof org.eclipse.jdt.core.dom.CompilationUnit cu) {
			return cu;
		}
		return findCU(node.getParent());
	}

	public int match(SimpleName name, NodeSetWrapper nodeSet) {
		String simpleName = name.getIdentifier();
		return simpleName != null && this.locator.matchesName(this.locator.pattern.simpleName, simpleName.toCharArray()) ?
			POSSIBLE_MATCH : IMPOSSIBLE_MATCH;
	}
	public int match(QualifiedName name, NodeSetWrapper nodeSet) {
		String simpleName = name.getName().getIdentifier();
		String qualifier = name.getQualifier().toString();
		if( this.locator.pattern.qualification == null ) {
			// Return an impossible match here, because we are not seeking a qualifier.
			// The SimpleName node should be the one to respond.
			return IMPOSSIBLE_MATCH;
		}
		if( qualifier != null) {
			String desiredQualifier = new String(this.locator.pattern.qualification);
			if( !qualifier.equals(desiredQualifier)) {
				return IMPOSSIBLE_MATCH;
			}
		}
		return simpleName != null && this.locator.matchesName(this.locator.pattern.simpleName, simpleName.toCharArray()) ?
			POSSIBLE_MATCH : IMPOSSIBLE_MATCH;
	}
	protected int resolveLevelForType(ITypeBinding typeBinding) {
		if (typeBinding == null) {
			if (this.locator.pattern.typeSuffix != IIndexConstants.TYPE_SUFFIX) return INACCURATE_MATCH;
		} else {
			switch (this.locator.pattern.typeSuffix) {
				case IIndexConstants.CLASS_SUFFIX:
					if (!typeBinding.isClass()) return IMPOSSIBLE_MATCH;
					break;
				case IIndexConstants.CLASS_AND_INTERFACE_SUFFIX:
					if (!(typeBinding.isClass() || (typeBinding.isInterface() && !typeBinding.isAnnotation()))) return IMPOSSIBLE_MATCH;
					break;
				case IIndexConstants.CLASS_AND_ENUM_SUFFIX:
					if (!(typeBinding.isClass() || typeBinding.isEnum())) return IMPOSSIBLE_MATCH;
					break;
				case IIndexConstants.INTERFACE_SUFFIX:
					if (!typeBinding.isInterface() || typeBinding.isAnnotation()) return IMPOSSIBLE_MATCH;
					break;
				case IIndexConstants.INTERFACE_AND_ANNOTATION_SUFFIX:
					if (!(typeBinding.isInterface() || typeBinding.isAnnotation())) return IMPOSSIBLE_MATCH;
					break;
				case IIndexConstants.ENUM_SUFFIX:
					if (!typeBinding.isEnum()) return IMPOSSIBLE_MATCH;
					break;
				case IIndexConstants.ANNOTATION_TYPE_SUFFIX:
					if (!typeBinding.isAnnotation()) return IMPOSSIBLE_MATCH;
					break;
				case IIndexConstants.TYPE_SUFFIX : // nothing
			}
		}
		return this.resolveLevelForType(this.locator.pattern.simpleName,
							this.locator.pattern.qualification,
							typeBinding);
	}

}
