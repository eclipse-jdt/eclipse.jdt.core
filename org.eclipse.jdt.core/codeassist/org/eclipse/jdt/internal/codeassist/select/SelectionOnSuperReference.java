package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

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

	public String toStringExpression() {

		return "<SelectOnSuper:" + super.toStringExpression() + ">";

	}

}
