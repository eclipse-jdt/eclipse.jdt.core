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
package org.eclipse.jdt.internal.codeassist.complete;

/*
 * Completion node build by the parser in any case it was intending to
 * reduce an allocation expression containing the cursor.
 * If the allocation expression is not qualified, the enclosingInstance field
 * is null.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      new Bar(1, 2, [cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <CompleteOnAllocationExpression:new Bar(1, 2)>
 *         }
 *       }
 *
 * The source range is always of length 0.
 * The arguments of the allocation expression are all the arguments defined
 * before the cursor.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionOnQualifiedAllocationExpression extends QualifiedAllocationExpression {
public TypeBinding resolveType(BlockScope scope) {
	if (arguments != null) {
		int argsLength = arguments.length;
		for (int a = argsLength; --a >= 0;)
			arguments[a].resolveType(scope);
	}
	
	if (enclosingInstance != null) {
		TypeBinding enclosingType = enclosingInstance.resolveType(scope);
		if (enclosingType == null || !(enclosingType instanceof ReferenceBinding)) {
			throw new CompletionNodeFound();
		}
		this.resolvedType = ((SingleTypeReference) type).resolveTypeEnclosing(scope, (ReferenceBinding) enclosingType);
		if (!(this.resolvedType instanceof ReferenceBinding))
			throw new CompletionNodeFound(); // no need to continue if its an array or base type
		if (this.resolvedType.isInterface()) // handle the anonymous class definition case
			this.resolvedType = scope.getJavaLangObject();
	} else {
		this.resolvedType = type.resolveType(scope);
		if (!(this.resolvedType instanceof ReferenceBinding))
			throw new CompletionNodeFound(); // no need to continue if its an array or base type
	}

	throw new CompletionNodeFound(this, this.resolvedType, scope);
}
public String toStringExpression(int tab) {
	return 
		((this.enclosingInstance == null) ? 
			"<CompleteOnAllocationExpression:" :  //$NON-NLS-1$
			"<CompleteOnQualifiedAllocationExpression:") +  //$NON-NLS-1$
		super.toStringExpression(tab) + ">"; //$NON-NLS-1$
}
}
