package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Selection node build by the parser in any case it was intending to
 * reduce a type reference containing the selection identifier as a single
 * name reference.
 * e.g.
 *
 *	class X extends [start]Object[end]
 *
 *	---> class X extends <SelectOnType:Object>
 *
 */
 
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class SelectionOnSingleTypeReference extends SingleTypeReference {
public SelectionOnSingleTypeReference(char[] source, long pos) {
	super(source, pos);
}
public void aboutToResolve(Scope scope) {
	getTypeBinding(scope.parent); // step up from the ClassScope
}
public TypeBinding getTypeBinding(Scope scope) {
	// it can be a package, type or member type
	Binding binding = scope.getTypeOrPackage(new char[][] {token});
	if (!binding.isValidBinding()) {
		scope.problemReporter().invalidType(this, (TypeBinding) binding);
		throw new SelectionNodeFound();
	}

	throw new SelectionNodeFound(binding);
}
public TypeBinding resolveTypeEnclosing(BlockScope scope, ReferenceBinding enclosingType) {
	super.resolveTypeEnclosing(scope, enclosingType);

	if (binding == null || !binding.isValidBinding())
		throw new SelectionNodeFound();
	else
		throw new SelectionNodeFound(binding);
}
public String toStringExpression(int tab){

	return "<SelectOnType:"/*nonNLS*/ + new String(token) + ">"/*nonNLS*/ ;
}
}
