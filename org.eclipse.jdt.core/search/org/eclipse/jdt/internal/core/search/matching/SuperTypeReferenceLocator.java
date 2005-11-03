/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class SuperTypeReferenceLocator extends PatternLocator {

protected SuperTypeReferencePattern pattern;

public SuperTypeReferenceLocator(SuperTypeReferencePattern pattern) {
	super(pattern);

	this.pattern = pattern;
}
//public int match(ASTNode node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(ConstructorDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(Expression node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(FieldDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(MethodDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(MessageSend node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(Reference node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(TypeDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
public int match(TypeReference node, MatchingNodeSet nodeSet) {
	if (this.pattern.superSimpleName == null)
		return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);

	char[] typeRefSimpleName = null;
	if (node instanceof SingleTypeReference) {
		typeRefSimpleName = ((SingleTypeReference) node).token;
	} else { // QualifiedTypeReference
		char[][] tokens = ((QualifiedTypeReference) node).tokens;
		typeRefSimpleName = tokens[tokens.length-1];
	}				
	if (matchesName(this.pattern.superSimpleName, typeRefSimpleName))
		return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);

	return IMPOSSIBLE_MATCH;
}

protected int matchContainer() {
	return CLASS_CONTAINER;
}
protected int referenceType() {
	return IJavaElement.TYPE;
}
public int resolveLevel(ASTNode node) {
	if (!(node instanceof TypeReference)) return IMPOSSIBLE_MATCH;

	TypeReference typeRef = (TypeReference) node;
	TypeBinding binding = typeRef.resolvedType;
	if (binding == null) return INACCURATE_MATCH;
	return resolveLevelForType(this.pattern.superSimpleName, this.pattern.superQualification, binding);
}
public int resolveLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof ReferenceBinding)) return IMPOSSIBLE_MATCH;

	ReferenceBinding type = (ReferenceBinding) binding;
	int level = IMPOSSIBLE_MATCH;
	if (this.pattern.superRefKind != SuperTypeReferencePattern.ONLY_SUPER_INTERFACES) {
		level = resolveLevelForType(this.pattern.superSimpleName, this.pattern.superQualification, type.superclass());
		if (level == ACCURATE_MATCH) return ACCURATE_MATCH;
	}

	if (this.pattern.superRefKind != SuperTypeReferencePattern.ONLY_SUPER_CLASSES) {
		ReferenceBinding[] superInterfaces = type.superInterfaces();
		for (int i = 0, max = superInterfaces.length; i < max; i++) {
			int newLevel = resolveLevelForType(this.pattern.superSimpleName, this.pattern.superQualification, superInterfaces[i]);
			if (newLevel > level) {
				if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
				level = newLevel;
			}
		}
	}
	return level;
}
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}
