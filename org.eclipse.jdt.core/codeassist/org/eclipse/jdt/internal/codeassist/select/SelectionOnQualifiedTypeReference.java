package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Selection node build by the parser in any case it was intending to
 * reduce a type reference containing the completion identifier as part
 * of a qualified name.
 * e.g.
 *
 *	class X extends java.lang.[start]Object[end]
 *
 *	---> class X extends <SelectOnType:java.lang.Object>
 *
 */
 
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class SelectionOnQualifiedTypeReference extends QualifiedTypeReference {
public SelectionOnQualifiedTypeReference(char[][] previousIdentifiers, char[] selectionIdentifier, long[] positions) {
	super(
		CharOperation.arrayConcat(previousIdentifiers, selectionIdentifier),
		positions);
}
public void aboutToResolve(Scope scope) {
	getTypeBinding(scope.parent); // step up from the ClassScope
}
public TypeBinding getTypeBinding(Scope scope) {
	// it can be a package, type or member type
	Binding binding = scope.getTypeOrPackage(tokens);
	if (!binding.isValidBinding()) {
		scope.problemReporter().invalidType(this, (TypeBinding) binding);
		throw new SelectionNodeFound();
	}

	throw new SelectionNodeFound(binding);
}
public String toStringExpression(int tab) {

	StringBuffer buffer = new StringBuffer();
	buffer.append("<SelectOnType:"/*nonNLS*/);
	for (int i = 0, length = tokens.length; i < length; i++) {
		buffer.append(tokens[i]);
		if (i != length - 1)
			buffer.append("."/*nonNLS*/);
	}
	buffer.append(">"/*nonNLS*/);
	return buffer.toString();
}
}
