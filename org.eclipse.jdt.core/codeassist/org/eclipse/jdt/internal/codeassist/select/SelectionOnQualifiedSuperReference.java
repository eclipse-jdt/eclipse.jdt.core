package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Selection node build by the parser in any case it was intending to
 * reduce a qualified super reference containing the assist identifier.
 * e.g.
 *
 *	class X extends Z {
 *    class Y {
 *    	void foo() {
 *      	X.[start]super[end].bar();
 *      }
 *    }
 *  }
 *
 *	---> class X {
 *		   class Y {
 *           void foo() {
 *             <SelectOnQualifiedSuper:X.super>
 *           }
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class SelectionOnQualifiedSuperReference
	extends QualifiedSuperReference {
	public SelectionOnQualifiedSuperReference(
		TypeReference name,
		int pos,
		int sourceEnd) {
		super(name, pos, sourceEnd);
	}

	public TypeBinding resolveType(BlockScope scope) {
		TypeBinding binding = super.resolveType(scope);

		if (binding == null || !binding.isValidBinding())
			throw new SelectionNodeFound();
		else
			throw new SelectionNodeFound(binding);
	}

	public String toStringExpression() {

		StringBuffer buffer = new StringBuffer("<SelectOnQualifiedSuper:");
		buffer.append(super.toStringExpression());
		buffer.append(">");
		return buffer.toString();
	}

}
