package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Selection node build by the parser in any case it was intending to
 * reduce a field reference containing the cursor.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      bar().[start]fred[end]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <SelectOnFieldReference:bar().fred>
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class SelectionOnFieldReference extends FieldReference {
	public SelectionOnFieldReference(char[] source, long pos) {
		super(source, pos);
	}

	public TypeBinding resolveType(BlockScope scope) {
		super.resolveType(scope);

		if (binding == null || !binding.isValidBinding())
			throw new SelectionNodeFound();
		else
			throw new SelectionNodeFound(binding);
	}

	public String toStringExpression() {
		/* slow code */

		return "<SelectionOnFieldReference:" + super.toStringExpression() + ">";
	}

}
