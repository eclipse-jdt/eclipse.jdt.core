package org.eclipse.jdt.internal.codeassist.complete;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Completion node build by the parser in any case it was intending to
 * reduce a single name reference containing the completion identifier.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      ba[cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <CompleteOnName:ba>
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
 
public class CompletionOnSingleNameReference extends SingleNameReference {
public CompletionOnSingleNameReference(char[] source, long pos) {
	super(source, pos);
}
public TypeBinding resolveType(BlockScope scope) {
	throw new CompletionNodeFound(this, scope);
}
public String toStringExpression() {
	return "<CompleteOnName:"/*nonNLS*/ + super.toStringExpression() + ">"/*nonNLS*/;
}
}
