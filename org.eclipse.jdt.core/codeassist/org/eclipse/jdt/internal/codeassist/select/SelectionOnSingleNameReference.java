package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Selection node build by the parser in any case it was intending to
 * reduce a single name reference containing the assist identifier.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      [start]ba[end]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <SelectOnName:ba>
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
 
public class SelectionOnSingleNameReference extends SingleNameReference {
public SelectionOnSingleNameReference(char[] source, long pos) {
	super(source, pos);
}
public TypeBinding resolveType(BlockScope scope) {
	// it can be a package, type, member type, local variable or field
	binding = scope.getBinding(token, VARIABLE | TYPE | PACKAGE, this);
	if (!binding.isValidBinding()) {
		if (binding instanceof ProblemFieldBinding) {
			scope.problemReporter().invalidField(this, (FieldBinding) binding);
		} else if (binding instanceof ProblemReferenceBinding) {
			scope.problemReporter().invalidType(this, (TypeBinding) binding);
		} else {
			scope.problemReporter().unresolvableReference(this, binding);
		}
		throw new SelectionNodeFound();
	}

	throw new SelectionNodeFound(binding);
}
public String toStringExpression() {
	return "<SelectOnName:" + super.toStringExpression() + ">"; //$NON-NLS-2$ //$NON-NLS-1$
}
}
