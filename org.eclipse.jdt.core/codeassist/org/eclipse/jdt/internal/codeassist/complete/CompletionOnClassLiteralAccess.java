package org.eclipse.jdt.internal.codeassist.complete;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Completion node build by the parser in any case it was intending to
 * reduce an access to the literal 'class' containing the cursor.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      String[].[cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <CompleteOnClassLiteralAccess:String[].>
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionOnClassLiteralAccess extends ClassLiteralAccess {
	public char[] completionIdentifier;
public CompletionOnClassLiteralAccess(long pos, TypeReference t) {
	super((int) (pos >>> 32), t);
	this.sourceEnd = (int)pos;
}
public TypeBinding resolveType(BlockScope scope) {
	if (super.resolveType(scope) == null)
		throw new CompletionNodeFound();
	else
		throw new CompletionNodeFound(this, targetType, scope);
}
public String toStringExpression() {
	StringBuffer result = new StringBuffer("<CompleteOnClassLiteralAccess:"); //$NON-NLS-1$
	result.append(type.toString());
	result.append("."); //$NON-NLS-1$
	result.append(completionIdentifier);
	result.append(">"); //$NON-NLS-1$
	return result.toString();
}
}
