/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.util.SimpleSet;

public class FieldLocator extends VariableLocator {

protected boolean isDeclarationOfAccessedFieldsPattern;

public FieldLocator(FieldPattern pattern) {
	super(pattern);

	this.isDeclarationOfAccessedFieldsPattern = this.pattern instanceof DeclarationOfAccessedFieldsPattern;
}
//public int match(ASTNode node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(ConstructorDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
public int match(FieldDeclaration node, MatchingNodeSet nodeSet) {
	int referencesLevel = IMPOSSIBLE_MATCH;
	if (this.pattern.findReferences)
		// must be a write only access with an initializer
		if (this.pattern.writeAccess && !this.pattern.readAccess && node.initialization != null)
			if (matchesName(this.pattern.name, node.name))
				referencesLevel = ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;

	int declarationsLevel = IMPOSSIBLE_MATCH;
	if (this.pattern.findDeclarations)
		if (node.isField()) // ignore field initializers
			if (matchesName(this.pattern.name, node.name))
				if (matchesTypeReference(((FieldPattern)this.pattern).typeSimpleName, node.type))
					declarationsLevel = ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;

	return nodeSet.addMatch(node, referencesLevel >= declarationsLevel ? referencesLevel : declarationsLevel); // use the stronger match
}
//public int match(MethodDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(MessageSend node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(TypeDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(TypeReference node, MatchingNodeSet nodeSet) - SKIP IT

protected int matchContainer() {
	if (this.pattern.findReferences) {
		// need to look almost everywhere to find in javadocs
		return CLASS_CONTAINER | METHOD_CONTAINER | FIELD_CONTAINER;
	}
	return CLASS_CONTAINER;
}
protected int matchField(FieldBinding field, boolean matchName) {
	if (field == null) return INACCURATE_MATCH;

	if (matchName && !matchesName(this.pattern.name, field.readableName())) return IMPOSSIBLE_MATCH;

	FieldPattern fieldPattern = (FieldPattern)this.pattern;
	ReferenceBinding receiverBinding = field.declaringClass;
	if (receiverBinding == null) {
		if (field == ArrayBinding.ArrayLength)
			// optimized case for length field of an array
			return fieldPattern.declaringQualification == null && fieldPattern.declaringSimpleName == null
				? ACCURATE_MATCH
				: IMPOSSIBLE_MATCH;
		return INACCURATE_MATCH;
	}

	// Note there is no dynamic lookup for field access
	int declaringLevel = resolveLevelForType(fieldPattern.declaringSimpleName, fieldPattern.declaringQualification, receiverBinding);
	if (declaringLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;

	// look at field type only if declaring type is not specified
	if (fieldPattern.declaringSimpleName == null) return declaringLevel;

	int typeLevel = resolveLevelForType(fieldPattern.typeSimpleName, fieldPattern.typeQualification, field.type);
	return declaringLevel > typeLevel ? typeLevel : declaringLevel; // return the weaker match
}
protected int matchReference(Reference node, MatchingNodeSet nodeSet, boolean writeOnlyAccess) {
	if (node instanceof FieldReference) {
		if (matchesName(this.pattern.name, ((FieldReference) node).token))
			return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
		return IMPOSSIBLE_MATCH;
	}
	return super.matchReference(node, nodeSet, writeOnlyAccess);
}
protected void matchReportReference(ASTNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.isDeclarationOfAccessedFieldsPattern) {
		// need exact match to be able to open on type ref
		if (accuracy != SearchMatch.A_ACCURATE) return;

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
	} else if (reference instanceof FieldReference) {
		FieldReference fieldReference = (FieldReference) reference;
		long position = fieldReference.nameSourcePosition;
		int start = (int) (position >>> 32);
		int end = (int) position;
		SearchMatch match = locator.newFieldReferenceMatch(element, accuracy, start, end-start+1, fieldReference);
		locator.report(match);
	} else if (reference instanceof SingleNameReference) {
		int offset = reference.sourceStart;
		SearchMatch match = locator.newFieldReferenceMatch(element, accuracy, offset, reference.sourceEnd-offset+1, reference);
		locator.report(match);
	} else if (reference instanceof QualifiedNameReference) {
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
						accuracies[indexOfFirstFieldBinding] = SearchMatch.A_ACCURATE;
						break;
					case INACCURATE_MATCH:
						accuracies[indexOfFirstFieldBinding] = SearchMatch.A_INACCURATE;
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
							accuracies[i] = SearchMatch.A_ACCURATE;
							break;
						case INACCURATE_MATCH:
							accuracies[i] = SearchMatch.A_INACCURATE;
							break;
						default:
							accuracies[i] = -1;
					}
				}
			} else {
				accuracies[i] = -1;
			}
		}
		locator.reportAccurateFieldReference(qNameRef, element, accuracies);
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
		locator.reportBinaryMemberDeclaration(resource, field, info, SearchMatch.A_ACCURATE);
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
			if (fieldDecl != null) {
				int offset = fieldDecl.sourceStart;
				SearchMatch match = new FieldDeclarationMatch(field, SearchMatch.A_ACCURATE, offset, fieldDecl.sourceEnd-offset+1, locator.getParticipant(), resource);
				locator.report(match);

			}
		}
	}
}
protected int referenceType() {
	return IJavaElement.FIELD;
}
public int resolveLevel(ASTNode possiblelMatchingNode) {
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
		if (matchesName(this.pattern.name, bindingName)) {
			int level = matchField(fieldBinding, false);
			if (level != IMPOSSIBLE_MATCH) return level;
		}
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
}
