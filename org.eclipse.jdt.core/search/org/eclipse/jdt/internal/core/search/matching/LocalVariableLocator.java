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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.core.LocalVariable;

public class LocalVariableLocator extends VariableLocator {

public LocalVariableLocator(LocalVariablePattern pattern) {
	super(pattern);
}
public int match(LocalDeclaration node, MatchingNodeSet nodeSet) {
	int referencesLevel = IMPOSSIBLE_MATCH;
	if (this.pattern.findReferences)
		// must be a write only access with an initializer
		if (this.pattern.writeAccess && !this.pattern.readAccess && node.initialization != null)
			if (matchesName(this.pattern.name, node.name))
				referencesLevel = this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;

	int declarationsLevel = IMPOSSIBLE_MATCH;
	if (this.pattern.findDeclarations)
		if (matchesName(this.pattern.name, node.name))
			if (node.declarationSourceStart == getLocalVariable().declarationSourceStart)
				declarationsLevel = this.pattern.mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;

	return nodeSet.addMatch(node, referencesLevel >= declarationsLevel ? referencesLevel : declarationsLevel); // use the stronger match
}
private LocalVariable getLocalVariable() {
	return ((LocalVariablePattern) this.pattern).localVariable;
}
protected void matchReportReference(ASTNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (reference instanceof SingleNameReference) {
		SearchMatch match = locator.newLocalVariableReferenceMatch(element, accuracy, reference.sourceStart, reference.sourceEnd+1, reference);
		locator.report(match);
	} else if (reference instanceof QualifiedNameReference) {
		QualifiedNameReference qNameRef = (QualifiedNameReference) reference;
		long sourcePosition = qNameRef.sourcePositions[0];
		SearchMatch match = locator.newLocalVariableReferenceMatch(element, accuracy, ((int) (sourcePosition >>> 32)), ((int) sourcePosition)+1, reference);
		locator.report(match);
	} else if (reference instanceof LocalDeclaration) {
		LocalVariable localVariable = getLocalVariable();
		SearchMatch match = locator.newLocalVariableReferenceMatch(localVariable, accuracy, localVariable.nameStart, localVariable.nameEnd+1, reference);
		locator.report(match);
	}
}
protected int matchContainer() {
	return METHOD_CONTAINER;
}
protected int matchLocalVariable(LocalVariableBinding variable, boolean matchName) {
	if (variable == null) return INACCURATE_MATCH;

	if (matchName && !matchesName(this.pattern.name, variable.readableName())) return IMPOSSIBLE_MATCH;

	return variable.declaration.declarationSourceStart == getLocalVariable().declarationSourceStart
		? ACCURATE_MATCH
		: IMPOSSIBLE_MATCH;
}
protected int referenceType() {
	return IJavaElement.LOCAL_VARIABLE;
}
public int resolveLevel(ASTNode possiblelMatchingNode) {
	if (this.pattern.findReferences)
		if (possiblelMatchingNode instanceof NameReference)
			return resolveLevel((NameReference) possiblelMatchingNode);
	if (possiblelMatchingNode instanceof LocalDeclaration)
		return matchLocalVariable(((LocalDeclaration) possiblelMatchingNode).binding, true);
	return IMPOSSIBLE_MATCH;
}
public int resolveLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof LocalVariableBinding)) return IMPOSSIBLE_MATCH;

	return matchLocalVariable((LocalVariableBinding) binding, true);
}
protected int resolveLevel(NameReference nameRef) {
	return resolveLevel(nameRef.binding);
}
}
