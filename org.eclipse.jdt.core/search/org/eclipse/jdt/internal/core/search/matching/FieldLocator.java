/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.util.SimpleSet;

public class FieldLocator extends PatternLocator {

protected FieldPattern pattern;
protected boolean isDeclarationOfAccessedFieldsPattern;

public FieldLocator(FieldPattern pattern) {
	super(pattern);

	this.pattern = pattern;
	this.isDeclarationOfAccessedFieldsPattern = this.pattern instanceof DeclarationOfAccessedFieldsPattern;
}
//public int match(AstNode node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(ConstructorDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
public int match(Expression node, MatchingNodeSet nodeSet) { // interested in Assignment
	if (this.pattern.writeAccess) {
		if (this.pattern.readAccess) return IMPOSSIBLE_MATCH; // already checked the lhs in match(Reference...) before we reached here

		if (node instanceof Assignment) {
			Expression lhs = ((Assignment) node).lhs;
			if (lhs instanceof Reference)
				return matchReference((Reference) lhs, nodeSet, true);
		}
	} else if (this.pattern.readAccess) {
		if (node instanceof Assignment && !(node instanceof CompoundAssignment)) {
			// the lhs of a simple assignment may be added in match(Reference...) before we reach here
			// for example, the fieldRef to 'this.x' in the statement this.x = x; is not considered a readAccess
			Expression lhs = ((Assignment) node).lhs;
			nodeSet.removePossibleMatch(lhs);
			nodeSet.removeTrustedMatch(lhs);
		}
	}
	return IMPOSSIBLE_MATCH;
}
public int match(FieldDeclaration node, MatchingNodeSet nodeSet) {
	int referencesLevel = IMPOSSIBLE_MATCH;
	if (this.pattern.findReferences)
		// must be a write only access with an initializer
		if (this.pattern.writeAccess && !this.pattern.readAccess && node.initialization != null)
			if (matchesName(this.pattern.name, node.name))
				referencesLevel = this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;

	int declarationsLevel = IMPOSSIBLE_MATCH;
	if (this.pattern.findDeclarations)
		if (node.isField()) // ignore field initializers
			if (matchesName(this.pattern.name, node.name))
				if (matchesTypeReference(this.pattern.typeSimpleName, node.type))
					declarationsLevel = this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;

	return nodeSet.addMatch(node, referencesLevel >= declarationsLevel ? referencesLevel : declarationsLevel); // use the stronger match
}
//public int match(MethodDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(MessageSend node, MatchingNodeSet nodeSet) - SKIP IT
public int match(Reference node, MatchingNodeSet nodeSet) { // interested in FieldReference, NameReference & its subtypes
	if (this.pattern.readAccess)
		return matchReference(node, nodeSet, false);

	return IMPOSSIBLE_MATCH;
}
//public int match(TypeDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(TypeReference node, MatchingNodeSet nodeSet) - SKIP IT

protected int matchContainer() {
	if (this.pattern.findReferences) {
		if (this.pattern.findDeclarations || this.pattern.writeAccess)
			return CLASS_CONTAINER | METHOD_CONTAINER | FIELD_CONTAINER;
		return METHOD_CONTAINER | FIELD_CONTAINER;
	}
	return CLASS_CONTAINER;
}
protected int matchField(FieldBinding field, boolean matchName) {
	if (field == null) return INACCURATE_MATCH;

	if (matchName && !matchesName(this.pattern.name, field.readableName())) return IMPOSSIBLE_MATCH;

	ReferenceBinding receiverBinding = field.declaringClass;
	if (receiverBinding == null) {
		if (field == ArrayBinding.ArrayLength)
			// optimized case for length field of an array
			return this.pattern.declaringQualification == null && this.pattern.declaringSimpleName == null
				? ACCURATE_MATCH
				: IMPOSSIBLE_MATCH;
		return INACCURATE_MATCH;
	}

	// Note there is no dynamic lookup for field access
	int declaringLevel = resolveLevelForType(this.pattern.declaringSimpleName, this.pattern.declaringQualification, receiverBinding);
	if (declaringLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;

	// look at field type only if declaring type is not specified
	if (this.pattern.declaringSimpleName == null) return declaringLevel;

	int typeLevel = resolveLevelForType(this.pattern.typeSimpleName, this.pattern.typeQualification, field.type);
	return declaringLevel > typeLevel ? typeLevel : declaringLevel; // return the weaker match
}
protected int matchReference(Reference node, MatchingNodeSet nodeSet, boolean writeOnlyAccess) {
	if (node instanceof FieldReference) {
		if (matchesName(this.pattern.name, ((FieldReference) node).token))
			return nodeSet.addMatch(node, this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
	} else if (node instanceof NameReference) {
		if (this.pattern.name == null) {
			return nodeSet.addMatch(node, this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
		} else if (node instanceof SingleNameReference) {
			if (matchesName(this.pattern.name, ((SingleNameReference) node).token))
				return nodeSet.addMatch(node, POSSIBLE_MATCH);
		} else {
			QualifiedNameReference qNameRef = (QualifiedNameReference) node;
			char[][] tokens = qNameRef.tokens;
			if (writeOnlyAccess) {
				// in the case of the assigment of a qualified name reference, the match must be on the last token
				if (matchesName(this.pattern.name, tokens[tokens.length-1]))
					return nodeSet.addMatch(node, POSSIBLE_MATCH);
			} else {
				for (int i = 0, max = tokens.length; i < max; i++)
					if (matchesName(this.pattern.name, tokens[i]))
						return nodeSet.addMatch(node, POSSIBLE_MATCH);
			}
		}
	}
	return IMPOSSIBLE_MATCH;
}
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.isDeclarationOfAccessedFieldsPattern) {
		// need exact match to be able to open on type ref
		if (accuracy != IJavaSearchResultCollector.EXACT_MATCH) return;

		// element that references the field must be included in the enclosing element
		DeclarationOfAccessedFieldsPattern declPattern = (DeclarationOfAccessedFieldsPattern) this.pattern; 
		while (element != null && !declPattern.enclosingElement.equals(element))
			element = element.getParent();
		if (element != null) {
			if (reference instanceof FieldReference) {
				reportDeclaration(((FieldReference) reference).binding, locator, declPattern.knownFields);
			} else if (reference instanceof QualifiedNameReference) {
				QualifiedNameReference qNameRef = (QualifiedNameReference) reference;
				Binding binding = qNameRef.binding;
				if (binding instanceof FieldBinding)
					reportDeclaration((FieldBinding)binding, locator, declPattern.knownFields);
				int otherMax = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;
				for (int i = 0; i < otherMax; i++)
					reportDeclaration(qNameRef.otherBindings[i], locator, declPattern.knownFields);
			} else if (reference instanceof SingleNameReference) {
				reportDeclaration((FieldBinding)((SingleNameReference) reference).binding, locator, declPattern.knownFields);
			}
		}
	} else if (this.pattern.findReferences && reference instanceof QualifiedNameReference) {
		QualifiedNameReference qNameRef = (QualifiedNameReference) reference;
		int length = qNameRef.tokens.length;
		int[] accuracies = new int[length];
		Binding binding = qNameRef.binding;
		int indexOfFirstFieldBinding = qNameRef.indexOfFirstFieldBinding > 0 ? qNameRef.indexOfFirstFieldBinding-1 : 0;
		for (int i = 0; i < indexOfFirstFieldBinding; i++)
			accuracies[i] = -1;
		// first token
		if (matchesName(this.pattern.name, qNameRef.tokens[indexOfFirstFieldBinding]) && !(binding instanceof LocalVariableBinding)) {
			FieldBinding fieldBinding = binding instanceof FieldBinding ? (FieldBinding) binding : null;
			if (fieldBinding == null) {
				accuracies[indexOfFirstFieldBinding] = accuracy;
			} else {
				switch (matchField(fieldBinding, false)) {
					case ACCURATE_MATCH:
						accuracies[indexOfFirstFieldBinding] = IJavaSearchResultCollector.EXACT_MATCH;
						break;
					case INACCURATE_MATCH:
						accuracies[indexOfFirstFieldBinding] = IJavaSearchResultCollector.POTENTIAL_MATCH;
						break;
					default:
						accuracies[indexOfFirstFieldBinding] = -1;
				}
			}
		} else {
			accuracies[indexOfFirstFieldBinding] = -1;
		}
		// other tokens
		for (int i = indexOfFirstFieldBinding+1; i < length; i++) {
			char[] token = qNameRef.tokens[i];
			if (matchesName(this.pattern.name, token)) {
				FieldBinding otherBinding = qNameRef.otherBindings == null ? null : qNameRef.otherBindings[i-(indexOfFirstFieldBinding+1)];
				if (otherBinding == null) {
					accuracies[i] = accuracy;
				} else {
					switch (matchField(otherBinding, false)) {
						case ACCURATE_MATCH:
							accuracies[i] = IJavaSearchResultCollector.EXACT_MATCH;
							break;
						case INACCURATE_MATCH:
							accuracies[i] = IJavaSearchResultCollector.POTENTIAL_MATCH;
							break;
						default:
							accuracies[i] = -1;
					}
				}
			} else {
				accuracies[i] = -1;
			}
		}
		locator.reportAccurateReference(
			reference.sourceStart, 
			reference.sourceEnd, 
			qNameRef.tokens, 
			element, 
			accuracies);
	} else {
		locator.reportAccurateReference(
			reference.sourceStart, 
			reference.sourceEnd, 
			new char[][] {this.pattern.name}, 
			element, 
			accuracy);
	}
}
protected void reportDeclaration(FieldBinding fieldBinding, MatchLocator locator, SimpleSet knownFields) throws CoreException {
	// ignore length field
	if (fieldBinding == ArrayBinding.ArrayLength) return;
	
	ReferenceBinding declaringClass = fieldBinding.declaringClass;
	IType type = locator.lookupType(declaringClass);
	if (type == null) return; // case of a secondary type

	char[] bindingName = fieldBinding.name;
	IField field = type.getField(new String(bindingName));
	if (knownFields.includes(field)) return;

	knownFields.add(field);
	IResource resource = type.getResource();
	boolean isBinary = type.isBinary();
	IBinaryType info = null;
	if (isBinary) {
		if (resource == null)
			resource = type.getJavaProject().getProject();
		info = locator.getBinaryInfo((org.eclipse.jdt.internal.core.ClassFile) type.getClassFile(), resource);
		locator.reportBinaryMatch(resource, field, info, IJavaSearchResultCollector.EXACT_MATCH);
	} else {
		ClassScope scope = ((SourceTypeBinding) declaringClass).scope;
		if (scope != null) {
			TypeDeclaration typeDecl = scope.referenceContext;
			FieldDeclaration fieldDecl = null;
			FieldDeclaration[] fieldDecls = typeDecl.fields;
			for (int i = 0, length = fieldDecls.length; i < length; i++) {
				if (CharOperation.equals(bindingName, fieldDecls[i].name)) {
					fieldDecl = fieldDecls[i];
					break;
				}
			} 
			if (fieldDecl != null)
				locator.report(resource, fieldDecl.sourceStart, fieldDecl.sourceEnd, field, IJavaSearchResultCollector.EXACT_MATCH);
		}
	}
}
public int resolveLevel(AstNode possiblelMatchingNode) {
	if (this.pattern.findReferences) {
		if (possiblelMatchingNode instanceof FieldReference)
			return matchField(((FieldReference) possiblelMatchingNode).binding, true);
		else if (possiblelMatchingNode instanceof NameReference)
			return resolveLevel((NameReference) possiblelMatchingNode);
	}
	if (possiblelMatchingNode instanceof FieldDeclaration)
		return matchField(((FieldDeclaration) possiblelMatchingNode).binding, true);
	return IMPOSSIBLE_MATCH;
}
public int resolveLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof FieldBinding)) return IMPOSSIBLE_MATCH;

	return matchField((FieldBinding) binding, true);
}
protected int resolveLevel(NameReference nameRef) {
	if (nameRef instanceof SingleNameReference)
		return resolveLevel(nameRef.binding);

	Binding binding = nameRef.binding;
	QualifiedNameReference qNameRef = (QualifiedNameReference) nameRef;
	FieldBinding fieldBinding = null;
	if (binding instanceof FieldBinding) {
		fieldBinding = (FieldBinding) binding;
		char[] bindingName = fieldBinding.name;
		int lastDot = CharOperation.lastIndexOf('.', bindingName);
		if (lastDot > -1)
			bindingName = CharOperation.subarray(bindingName, lastDot+1, bindingName.length);
		if (matchesName(this.pattern.name, bindingName))
			return matchField(fieldBinding, false);
	} 
	int otherMax = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;
	for (int i = 0; i < otherMax; i++) {
		char[] token = qNameRef.tokens[i + qNameRef.indexOfFirstFieldBinding];
		if (matchesName(this.pattern.name, token)) {
			FieldBinding otherBinding = qNameRef.otherBindings[i];
			int level = matchField(otherBinding, false);
			if (level != IMPOSSIBLE_MATCH) return level;
		}
	}
	return IMPOSSIBLE_MATCH;
}
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}
