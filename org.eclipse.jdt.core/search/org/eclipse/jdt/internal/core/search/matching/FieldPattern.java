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

import java.io.IOException;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchResultCollector;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.AbstractIndexer;

public class FieldPattern extends SearchPattern {

protected boolean findDeclarations;
protected boolean findReferences;
protected boolean readAccess;
protected boolean writeAccess;

protected char[] name;
	
// declaring type
protected char[] declaringQualification;
protected char[] declaringSimpleName;

// type
protected char[] typeQualification;
protected char[] typeSimpleName;

protected char[] decodedName;
protected char[] currentTag;

protected static char[][] REF_TAGS = { FIELD_REF, REF };
protected static char[][] REF_AND_DECL_TAGS = { FIELD_REF, REF, FIELD_DECL };
protected static char[][] DECL_TAGS = { FIELD_DECL };

public FieldPattern(
	boolean findDeclarations,
	boolean readAccess,
	boolean writeAccess,
	char[] name, 
	int matchMode, 
	boolean isCaseSensitive,
	char[] declaringQualification,
	char[] declaringSimpleName,	
	char[] typeQualification, 
	char[] typeSimpleName) {

	super(matchMode, isCaseSensitive);

	this.findDeclarations = findDeclarations; // set to find declarations & all occurences
	this.readAccess = readAccess; // set to find any reference, read only references & all occurences
	this.writeAccess = writeAccess; // set to find any reference, write only references & all occurences
	this.findReferences = readAccess || writeAccess;

	this.name = isCaseSensitive ? name : CharOperation.toLowerCase(name);
	this.declaringQualification = isCaseSensitive ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	this.typeQualification = isCaseSensitive ? typeQualification : CharOperation.toLowerCase(typeQualification);
	this.typeSimpleName = isCaseSensitive ? typeSimpleName : CharOperation.toLowerCase(typeSimpleName);

	this.mustResolve = mustResolve();
}
protected void acceptPath(IIndexSearchRequestor requestor, String path) {
	if (this.currentTag ==  FIELD_DECL && this.findDeclarations)
		requestor.acceptFieldDeclaration(path, this.decodedName);
	else
		requestor.acceptFieldReference(path, this.decodedName);
}
protected void decodeIndexEntry(IEntryResult entryResult) {
	this.decodedName = CharOperation.subarray(entryResult.getWord(), this.currentTag.length, -1);
}
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {
	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	// in the new story this will be a single call with a mask
	char[][] possibleTags =
		this.findReferences
			? (this.findDeclarations || this.writeAccess ? REF_AND_DECL_TAGS : REF_TAGS)
			: DECL_TAGS;
	for (int i = 0, max = possibleTags.length; i < max; i++) {
		this.currentTag = possibleTags[i];
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
}
/**
 * @see SearchPattern#indexEntryPrefix
 */
protected char[] indexEntryPrefix() {
	return AbstractIndexer.bestReferencePrefix(currentTag, name, matchMode, isCaseSensitive); // should really be called bestPrefix
}
/**
 * @see SearchPattern#matchCheck(AstNode, MatchingNodeSet)
 */
protected void matchCheck(AstNode node, MatchingNodeSet set) {
	if (this.findDeclarations || this.readAccess)
		super.matchCheck(node, set);

	if (this.findReferences && node instanceof Assignment) {
		AstNode lhs = ((Assignment) node).lhs;
		if (this.writeAccess) {
			super.matchCheck(lhs, set);
		} else if (!(node instanceof CompoundAssignment)) {
			// the lhs may have been added when checking if it was a read access
			set.removePossibleMatch(lhs);
			set.removeTrustedMatch(lhs);
		}	
	}
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	if (this.findReferences) {
		if (this.findDeclarations || this.writeAccess)
			return CLASS | METHOD | FIELD;
		return METHOD | FIELD;
	}
	return CLASS;
}
/**
 * @see SearchPattern#matchesBinary(Object, Object)
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	if (!this.findDeclarations) return false; // only relevant when finding declarations
	if (!(binaryInfo instanceof IBinaryField)) return false;

	IBinaryField field = (IBinaryField) binaryInfo;
	if (!matchesName(this.name, field.getName())) return false;

	// declaring type
	if (enclosingBinaryInfo != null) {
		IBinaryType declaringType = (IBinaryType) enclosingBinaryInfo;
		char[] declaringTypeName = (char[]) declaringType.getName().clone();
		CharOperation.replace(declaringTypeName, '/', '.');
		if (!matchesType(this.declaringSimpleName, this.declaringQualification, declaringTypeName))
			return false;
	}

	// field type
	String fieldTypeSignature = new String(field.getTypeName()).replace('/', '.');
	return matchesType(this.typeSimpleName, this.typeQualification, Signature.toString(fieldTypeSignature).toCharArray());
}
protected int matchField(FieldBinding field, boolean matchName) {
	if (field == null) return INACCURATE_MATCH;

	if (matchName && !matchesName(this.name, field.readableName())) return IMPOSSIBLE_MATCH;

	// receiver type
	ReferenceBinding receiverBinding = field.declaringClass;
	if (receiverBinding == null) {
		if (field == ArrayBinding.ArrayLength)
			// optimized case for length field of an array
			return this.declaringQualification == null && this.declaringSimpleName == null
				? ACCURATE_MATCH
				: IMPOSSIBLE_MATCH;
		return INACCURATE_MATCH;
	}

	// Note there is no dynamic lookup for field access
	int declaringLevel = matchLevelForType(this.declaringSimpleName, this.declaringQualification, receiverBinding);
	if (declaringLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;

	// look at field type only if declaring type is not specified
	if (this.declaringSimpleName == null) return declaringLevel;

	int typeLevel = matchLevelForType(this.typeSimpleName, this.typeQualification, field.type);
	return declaringLevel > typeLevel ? typeLevel : declaringLevel; // return the weaker match
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	if (name != null) {
		switch(matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(name, decodedName, isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(name, decodedName, isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(name, decodedName, isCaseSensitive);
		}
	}
	return true;
}
/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	if (this.findReferences) {
		if (node instanceof FieldReference)
			return matchLevel((FieldReference) node, resolve);
		else if (node instanceof NameReference)
			return matchLevel((NameReference) node, resolve);
	}
	if (node instanceof FieldDeclaration)
		return matchLevel((FieldDeclaration) node, resolve);
	return IMPOSSIBLE_MATCH;
}
/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof FieldBinding)) return IMPOSSIBLE_MATCH;

	return matchField((FieldBinding) binding, true);
}
/**
 * Returns whether this field reference pattern matches the given field declaration in
 * write access.
 * Look at resolved information only if specified.
 */
protected int matchLevel(FieldDeclaration fieldDecl, boolean resolve) {
	if (resolve)
		return matchField(fieldDecl.binding, true);

	int referencesLevel = IMPOSSIBLE_MATCH;
	if (this.findReferences) {
		// must be a write only access with an initializer
		if (this.writeAccess && !this.readAccess && fieldDecl.initialization != null) {
			if (matchesName(this.name, fieldDecl.name)) {
				if (!this.mustResolve) return ACCURATE_MATCH; // cannot get better
				referencesLevel = POTENTIAL_MATCH;
			}
		}
	}

	int declarationsLevel = IMPOSSIBLE_MATCH;
	if (this.findDeclarations) {
		// answer referencesLevel if this is an IMPOSSIBLE_MATCH
		if (!fieldDecl.isField()) return referencesLevel; // ignore field initializers
		if (!matchesName(this.name, fieldDecl.name)) return referencesLevel;
		if (!matchesTypeReference(this.typeSimpleName, fieldDecl.type)) return referencesLevel;

		if (!this.mustResolve) return ACCURATE_MATCH; // cannot get better
		declarationsLevel = POTENTIAL_MATCH;
	}
	return referencesLevel >= declarationsLevel ? referencesLevel : declarationsLevel; // answer the stronger match
}
/**
 * Returns whether this field reference pattern matches the given field reference.
 * Look at resolved information only if specified.
 */
protected int matchLevel(FieldReference fieldRef, boolean resolve) {	
	if (resolve)
		return matchField(fieldRef.binding, true);

	if (!matchesName(this.name, fieldRef.token)) return IMPOSSIBLE_MATCH;
	return this.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH;
}
/**
 * Returns whether this field reference pattern matches the given name reference.
 * Look at resolved information only if specified.
 */
protected int matchLevel(NameReference nameRef, boolean resolve) {	
	if (!resolve) {
		if (this.name == null)
			return this.mustResolve ? POTENTIAL_MATCH : ACCURATE_MATCH;
		if (nameRef instanceof SingleNameReference) {
			if (matchesName(this.name, ((SingleNameReference) nameRef).token)) return POTENTIAL_MATCH;
		} else {
			QualifiedNameReference qNameRef = (QualifiedNameReference) nameRef;
			char[][] tokens = qNameRef.tokens;
			if (this.writeAccess && !this.readAccess) {
				// in the case of the assigment of a qualified name reference, the match must be on the last token
				if (matchesName(this.name, tokens[tokens.length-1])) return POTENTIAL_MATCH;
			} else {
				for (int i = 0, max = tokens.length; i < max; i++)
					if (matchesName(this.name, tokens[i])) return POTENTIAL_MATCH;
			}
		}
		return IMPOSSIBLE_MATCH;
	}				

	if (nameRef instanceof SingleNameReference)
		return matchLevel(nameRef.binding);

	Binding binding = nameRef.binding;
	QualifiedNameReference qNameRef = (QualifiedNameReference) nameRef;
	FieldBinding fieldBinding = null;
	if (binding instanceof FieldBinding) {
		fieldBinding = (FieldBinding) binding;
		char[] bindingName = fieldBinding.name;
		int lastDot = CharOperation.lastIndexOf('.', bindingName);
		if (lastDot > -1)
			bindingName = CharOperation.subarray(bindingName, lastDot+1, bindingName.length);
		if (matchesName(this.name, bindingName))
			return matchField(fieldBinding, false);
	} 
	int otherMax = qNameRef.otherBindings == null ? 0 : qNameRef.otherBindings.length;
	for (int i = 0; i < otherMax; i++) {
		char[] token = qNameRef.tokens[i + qNameRef.indexOfFirstFieldBinding];
		if (matchesName(this.name, token)) {
			FieldBinding otherBinding = qNameRef.otherBindings[i];
			int level = matchField(otherBinding, false);
			if (level != IMPOSSIBLE_MATCH) return level;
		}
	}
	return IMPOSSIBLE_MATCH;
}
/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.findReferences && reference instanceof QualifiedNameReference) {
		QualifiedNameReference qNameRef = (QualifiedNameReference) reference;
		int length = qNameRef.tokens.length;
		int[] accuracies = new int[length];
		Binding binding = qNameRef.binding;
		int indexOfFirstFieldBinding = qNameRef.indexOfFirstFieldBinding > 0 ? qNameRef.indexOfFirstFieldBinding-1 : 0;
		for (int i = 0; i < indexOfFirstFieldBinding; i++)
			accuracies[i] = -1;
		// first token
		if (matchesName(this.name, qNameRef.tokens[indexOfFirstFieldBinding]) && !(binding instanceof LocalVariableBinding)) {
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
			if (matchesName(this.name, token)) {
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
			new char[][] {this.name}, 
			element, 
			accuracy);
	}
}
/**
 * Returns whether a method declaration or message send will need to be resolved to 
 * find out if this method pattern matches it.
 */
protected boolean mustResolve() {
	// would like to change this so that we only do it if generic references are found
	if (findReferences) return true; // always resolve (in case of a simple name reference being a potential match)

	// declaring type
	if (declaringSimpleName != null || declaringQualification != null) return true;

	// return type
	if (typeSimpleName != null || typeQualification != null) return true;

	return false;
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	if (this.findDeclarations) {
		buffer.append(this.findReferences
			? "FieldCombinedPattern: " //$NON-NLS-1$
			: "FieldDeclarationPattern: "); //$NON-NLS-1$
	} else {
		buffer.append("FieldReferencePattern: "); //$NON-NLS-1$
	}
	if (declaringQualification != null) buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName).append('.');
	else if (declaringQualification != null) buffer.append("*."); //$NON-NLS-1$
	if (name == null) {
		buffer.append("*"); //$NON-NLS-1$
	} else {
		buffer.append(name);
	}
	if (typeQualification != null) 
		buffer.append(" --> ").append(typeQualification).append('.'); //$NON-NLS-1$
	else if (typeSimpleName != null) buffer.append(" --> "); //$NON-NLS-1$
	if (typeSimpleName != null) 
		buffer.append(typeSimpleName);
	else if (typeQualification != null) buffer.append("*"); //$NON-NLS-1$
	buffer.append(", "); //$NON-NLS-1$
	switch(matchMode){
		case EXACT_MATCH : 
			buffer.append("exact match, "); //$NON-NLS-1$
			break;
		case PREFIX_MATCH :
			buffer.append("prefix match, "); //$NON-NLS-1$
			break;
		case PATTERN_MATCH :
			buffer.append("pattern match, "); //$NON-NLS-1$
			break;
	}
	buffer.append(isCaseSensitive ? "case sensitive" : "case insensitive"); //$NON-NLS-1$ //$NON-NLS-2$
	return buffer.toString();
}
}
