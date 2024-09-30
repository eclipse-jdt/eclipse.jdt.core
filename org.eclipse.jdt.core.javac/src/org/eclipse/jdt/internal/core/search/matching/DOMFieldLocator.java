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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.search.FieldDeclarationMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.core.search.DOMASTNodeUtils;

public class DOMFieldLocator extends DOMPatternLocator {

	private FieldLocator fieldLocator;

	public DOMFieldLocator(FieldLocator locator) {
		super(locator.pattern);
		this.fieldLocator = locator;
	}

	@Override
	public int match(org.eclipse.jdt.core.dom.ASTNode node, NodeSetWrapper nodeSet, MatchLocator locator) {
		int declarationsLevel = PatternLocator.IMPOSSIBLE_MATCH;
		if (node instanceof EnumConstantDeclaration enumConstant) {
			return match(enumConstant, nodeSet);
		}
		if (this.fieldLocator.pattern.findReferences) {
			if (node instanceof ImportDeclaration importRef) {
				// With static import, we can have static field reference in import reference
				if (importRef.isStatic() && !importRef.isOnDemand()
						&& this.fieldLocator.matchesName(this.fieldLocator.pattern.name,
								importRef.getName().toString().toCharArray())
						&& this.fieldLocator.pattern instanceof FieldPattern fieldPattern) {
					char[] declaringType = CharOperation.concat(fieldPattern.declaringQualification,
							fieldPattern.declaringSimpleName, '.');
					if (this.fieldLocator.matchesName(declaringType, importRef.getName().toString().toCharArray())) {
						declarationsLevel = this.fieldLocator.pattern.mustResolve ? PatternLocator.POSSIBLE_MATCH
								: PatternLocator.ACCURATE_MATCH;
					}
				}
			}
		}
		return nodeSet.addMatch(node, declarationsLevel);
	}

	@Override
	public int match(Name name, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (this.fieldLocator.pattern.findDeclarations) {
			return PatternLocator.IMPOSSIBLE_MATCH; // already caught by match(VariableDeclaration)
		}

		if (this.fieldLocator.matchesName(this.fieldLocator.pattern.name, name.toString().toCharArray())) {
			if (this.fieldLocator.isDeclarationOfAccessedFieldsPattern
					&& this.fieldLocator.pattern instanceof DeclarationOfAccessedFieldsPattern doafp) {
				if (doafp.enclosingElement != null) {
					// we have an enclosing element to check
					if (!DOMASTNodeUtils.isWithinRange(name, doafp.enclosingElement)) {
						return PatternLocator.IMPOSSIBLE_MATCH;
					}
					// We need to report the declaration, not the usage
					// TODO testDeclarationOfAccessedFields2
					IBinding b = name.resolveBinding();
					IJavaElement je = b == null ? null : b.getJavaElement();
					if (je != null && doafp.knownFields.includes(je)) {
						doafp.knownFields.remove(je);
						ISourceReference sr = je instanceof ISourceReference ? (ISourceReference) je : null;
						IResource r = null;
						ISourceRange srg = null;
						String elName = je.getElementName();
						try {
							srg = sr.getSourceRange();
							IJavaElement ancestor = je.getAncestor(IJavaElement.COMPILATION_UNIT);
							r = ancestor == null ? null : ancestor.getCorrespondingResource();
						} catch (JavaModelException jme) {
							// ignore
						}
						if (srg != null) {
							int accuracy = this.fieldLocator.pattern.mustResolve ? PatternLocator.POSSIBLE_MATCH
									: PatternLocator.ACCURATE_MATCH;
							FieldDeclarationMatch fdMatch = new FieldDeclarationMatch(je, accuracy,
									srg.getOffset() + srg.getLength() - elName.length() - 1, elName.length(),
									locator.getParticipant(), r);
							try {
								locator.report(fdMatch);
							} catch (CoreException ce) {
								// ignore
							}
						}
					}
					return PatternLocator.IMPOSSIBLE_MATCH;
				}
			}

			return nodeSet.addMatch(name, this.fieldLocator.pattern.mustResolve ? PatternLocator.POSSIBLE_MATCH
					: PatternLocator.ACCURATE_MATCH);
		}
		return PatternLocator.IMPOSSIBLE_MATCH;
	}

	@Override
	public int resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (binding == null)
			return PatternLocator.ACCURATE_MATCH;
		if (binding instanceof IVariableBinding variableBinding) {
			if (variableBinding.isRecordComponent()) {
				// for matching the component in constructor of a record
				if (!this.fieldLocator.matchesName(this.fieldLocator.pattern.name,
						variableBinding.getName().toCharArray()))
					return PatternLocator.IMPOSSIBLE_MATCH;
				FieldPattern fieldPattern = (FieldPattern) this.fieldLocator.pattern;
				return this.resolveLevelForType(fieldPattern.declaringSimpleName,
						fieldPattern.declaringQualification, variableBinding.getDeclaringMethod().getDeclaringClass());
			}
			if (variableBinding.isField()) {
				return this.matchField(variableBinding, true);
			}
		}
		return PatternLocator.IMPOSSIBLE_MATCH;
	}

	@Override
	public int match(VariableDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (!this.fieldLocator.pattern.findDeclarations && !this.fieldLocator.isDeclarationOfAccessedFieldsPattern) {
			return PatternLocator.IMPOSSIBLE_MATCH;
		}
		if (node.getLocationInParent() != org.eclipse.jdt.core.dom.FieldDeclaration.FRAGMENTS_PROPERTY) {
			return PatternLocator.IMPOSSIBLE_MATCH;
		}
		int referencesLevel = PatternLocator.IMPOSSIBLE_MATCH;
		if (this.fieldLocator.pattern.findReferences)
			// must be a write only access with an initializer
			if (this.fieldLocator.pattern.writeAccess && !this.fieldLocator.pattern.readAccess
					&& node.getInitializer() != null)
				if (this.fieldLocator.matchesName(this.fieldLocator.pattern.name,
						node.getName().getIdentifier().toCharArray()))
					referencesLevel = this.fieldLocator.pattern.mustResolve ? PatternLocator.POSSIBLE_MATCH
							: PatternLocator.ACCURATE_MATCH;

		int declarationsLevel = PatternLocator.IMPOSSIBLE_MATCH;
		if ((this.fieldLocator.pattern.findDeclarations || this.fieldLocator.isDeclarationOfAccessedFieldsPattern)
				&& this.fieldLocator.matchesName(this.fieldLocator.pattern.name,
						node.getName().getIdentifier().toCharArray())
				&& this.fieldLocator.pattern instanceof FieldPattern fieldPattern
				&& this.matchesTypeReference(fieldPattern.typeSimpleName,
						((org.eclipse.jdt.core.dom.FieldDeclaration) node.getParent()).getType())) {
			declarationsLevel = this.fieldLocator.pattern.mustResolve ? PatternLocator.POSSIBLE_MATCH
					: PatternLocator.ACCURATE_MATCH;
		}
		return nodeSet.addMatch(node, referencesLevel >= declarationsLevel ? referencesLevel : declarationsLevel); // use
																													// the
																													// stronger
																													// match
	}

	private int match(EnumConstantDeclaration node, NodeSetWrapper nodeSet) {
		int referencesLevel = PatternLocator.IMPOSSIBLE_MATCH;
		if (this.fieldLocator.pattern.findReferences)
			// must be a write only access with an initializer
			if (this.fieldLocator.pattern.writeAccess && !this.fieldLocator.pattern.readAccess)
				if (this.fieldLocator.matchesName(this.fieldLocator.pattern.name,
						node.getName().getIdentifier().toCharArray()))
					referencesLevel = this.fieldLocator.pattern.mustResolve ? PatternLocator.POSSIBLE_MATCH
							: PatternLocator.ACCURATE_MATCH;

		int declarationsLevel = PatternLocator.IMPOSSIBLE_MATCH;
		if (this.fieldLocator.pattern.findDeclarations
				&& this.fieldLocator.matchesName(this.fieldLocator.pattern.name,
						node.getName().getIdentifier().toCharArray())
				&& this.fieldLocator.pattern instanceof FieldPattern fieldPattern
				&& this.fieldLocator.matchesName(fieldPattern.typeSimpleName,
						((EnumDeclaration) node.getParent()).getName().getIdentifier().toCharArray())) {
			declarationsLevel = this.fieldLocator.pattern.mustResolve ? PatternLocator.POSSIBLE_MATCH
					: PatternLocator.ACCURATE_MATCH;
		}
		return nodeSet.addMatch(node, referencesLevel >= declarationsLevel ? referencesLevel : declarationsLevel); // use
																													// the
																													// stronger
																													// match
	}

	protected int matchField(IVariableBinding field, boolean matchName) {
		if (field == null)
			return INACCURATE_MATCH;
		if (!field.isField())
			return IMPOSSIBLE_MATCH;

		if (matchName && !this.fieldLocator.matchesName(this.fieldLocator.pattern.name, field.getName().toCharArray()))
			return IMPOSSIBLE_MATCH;

		FieldPattern fieldPattern = (FieldPattern) this.fieldLocator.pattern;
		ITypeBinding receiverBinding = field.getDeclaringClass();
		if (receiverBinding == null) {
			if (field == ArrayBinding.ArrayLength)
				// optimized case for length field of an array
				return fieldPattern.declaringQualification == null && fieldPattern.declaringSimpleName == null
						? ACCURATE_MATCH
						: IMPOSSIBLE_MATCH;
			int mode = fieldPattern.getMatchMode();
			if (mode == SearchPattern.R_EXACT_MATCH) {
				return IMPOSSIBLE_MATCH;
			}
			return INACCURATE_MATCH;
		}

		// Note there is no dynamic lookup for field access
		int declaringLevel = this.resolveLevelForType(fieldPattern.declaringSimpleName,
				fieldPattern.declaringQualification, receiverBinding);
		if (declaringLevel == IMPOSSIBLE_MATCH)
			return IMPOSSIBLE_MATCH;

		// look at field type only if declaring type is not specified
		if (fieldPattern.declaringSimpleName == null) {
			if (this.fieldLocator.isDeclarationOfAccessedFieldsPattern
					&& this.fieldLocator.pattern instanceof DeclarationOfAccessedFieldsPattern doafp) {
				IJavaElement je = field.getJavaElement();
				if (je != null) {
					doafp.knownFields.add(je);
				}
			} else {
				return declaringLevel;
			}
			return IMPOSSIBLE_MATCH;
		}

		// get real field binding
		// TODO what is a ParameterizedFieldBinding?
//	FieldBinding fieldBinding = field;
//	if (field instanceof ParameterizedFieldBinding) {
//		fieldBinding = ((ParameterizedFieldBinding) field).originalField;
//	}

		int typeLevel = resolveLevelForType(field.getType());
		int ret = declaringLevel > typeLevel ? typeLevel : declaringLevel; // return the weaker match
		if (this.fieldLocator.isDeclarationOfAccessedFieldsPattern
				&& this.fieldLocator.pattern instanceof DeclarationOfAccessedFieldsPattern doafp) {
			IJavaElement je = field.getJavaElement();
			if (je != null) {
				doafp.knownFields.add(je);
			}
		} else {
			return ret;
		}
		return IMPOSSIBLE_MATCH;
	}

	protected int resolveLevelForType(ITypeBinding typeBinding) {
		FieldPattern fieldPattern = (FieldPattern) this.fieldLocator.pattern;
		ITypeBinding fieldTypeBinding = typeBinding;
		if (fieldTypeBinding != null && fieldTypeBinding.isParameterizedType()) {
			fieldTypeBinding = typeBinding.getErasure();
		}
		int fieldNameMatch = this.resolveLevelForType(fieldPattern.typeSimpleName,
				fieldPattern.typeQualification, fieldTypeBinding);
		return fieldNameMatch;
	}
}
