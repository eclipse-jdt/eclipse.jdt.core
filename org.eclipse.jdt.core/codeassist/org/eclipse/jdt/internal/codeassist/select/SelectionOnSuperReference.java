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
package org.eclipse.jdt.internal.codeassist.select;

/*
 * Selection node build by the parser in any case it was intending to
 * reduce a super reference containing the assist identifier.
 * e.g.
 *
 *	class X extends Z {
 *    class Y {
 *    	void foo() {
 *      	[start]super[end].bar();
 *      }
 *    }
 *  }
 *
 *	---> class X {
 *		   class Y {
 *           void foo() {
 *             <SelectOnQualifiedSuper:super>
 *           }
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnSuperReference extends SuperReference {

public SelectionOnSuperReference(int pos, int sourceEnd) {
	super(pos, sourceEnd);
}
public TypeBinding resolveType(BlockScope scope) {
	TypeBinding binding = super.resolveType(scope);

	if (binding == null || !binding.isValidBinding())
		throw new SelectionNodeFound();
	else
		throw new SelectionNodeFound(binding);
}
public String toStringExpression(){

	return "<SelectOnSuper:"+super.toStringExpression()+">"; //$NON-NLS-2$ //$NON-NLS-1$
	
}
}
