package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Selection node build by the parser in any case it was intending to
 * reduce an allocation expression containing the cursor.
 * If the allocation expression is not qualified, the enclosingInstance field
 * is null.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      new [start]Bar[end](1, 2)
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <SelectOnAllocationExpression:new Bar(1, 2)>
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
 
public class SelectionOnQualifiedAllocationExpression extends QualifiedAllocationExpression {
public SelectionOnQualifiedAllocationExpression() {
}
public SelectionOnQualifiedAllocationExpression(AnonymousLocalTypeDeclaration anonymous) {
	anonymousType = anonymous ;
}
public TypeBinding resolveType(BlockScope scope) {
	super.resolveType(scope);

	if (binding == null || !binding.isValidBinding())
		throw new SelectionNodeFound();
	if (anonymousType == null)
		throw new SelectionNodeFound(binding);

	// if selecting a type for an anonymous type creation, we have to
	// find its target super constructor (if extending a class) or its target 
	// super interface (if extending an interface)
	if (anonymousType.binding.superInterfaces == NoSuperInterfaces) {
		// find the constructor binding inside the super constructor call
		ConstructorDeclaration constructor = (ConstructorDeclaration) anonymousType.declarationOf(binding);
		throw new SelectionNodeFound(constructor.constructorCall.binding);
	} else {
		// open on the only superinterface
		throw new SelectionNodeFound(anonymousType.binding.superInterfaces[0]);
	}
}
public String toStringExpression(int tab) {
	return 
		((this.enclosingInstance == null) ? 
			"<SelectOnAllocationExpression:"/*nonNLS*/ : 
			"<SelectOnQualifiedAllocationExpression:"/*nonNLS*/) + 
		super.toStringExpression(tab) + ">"/*nonNLS*/;
}
}
