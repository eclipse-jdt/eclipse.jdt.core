package org.eclipse.jdt.internal.codeassist.complete;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Completion node build by the parser in any case it was intending to
 * reduce a type reference containing the completion identifier as part
 * of a qualified name.
 * e.g.
 *
 *	class X extends java.lang.Obj[cursor]
 *
 *	---> class X extends <CompleteOnType:java.lang.Obj>
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionOnQualifiedTypeReference
	extends QualifiedTypeReference {
	public char[] completionIdentifier;
	public CompletionOnQualifiedTypeReference(
		char[][] previousIdentifiers,
		char[] completionIdentifier,
		long[] positions) {
		super(previousIdentifiers, positions);
		this.completionIdentifier = completionIdentifier;
	}

	public void aboutToResolve(Scope scope) {
		getTypeBinding(scope.parent); // step up from the ClassScope
	}

	/*
	 * No expansion of the completion reference into an array one
	 */
	public TypeReference copyDims(int dim) {
		return this;
	}

	public TypeBinding getTypeBinding(Scope scope) {
		// it can be a package, type or member type
		Binding binding = scope.getTypeOrPackage(tokens);
		if (!binding.isValidBinding()) {
			scope.problemReporter().invalidType(this, (TypeBinding) binding);
			throw new CompletionNodeFound();
		}

		throw new CompletionNodeFound(this, binding, scope);
	}

	public String toStringExpression(int tab) {

		StringBuffer buffer = new StringBuffer();
		buffer.append("<CompleteOnType:");
		for (int i = 0; i < tokens.length; i++) {
			buffer.append(tokens[i]);
			buffer.append(".");
		}
		buffer.append(completionIdentifier).append(">");
		return buffer.toString();
	}

}
