package org.eclipse.jdt.internal.codeassist.complete;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Completion node build by the parser in any case it was intending to
 * reduce an access to a member (field reference or message send) 
 * containing the completion identifier.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      bar().fred[cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <CompleteOnMemberAccess:bar().fred>
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionOnMemberAccess extends FieldReference {
public CompletionOnMemberAccess(char[] source , long pos) {
	super(source, pos);
}
public TypeBinding resolveType(BlockScope scope) {
	TypeBinding receiverType = receiver.resolveType(scope);
	if (receiverType == null || receiverType.isBaseType())
		throw new CompletionNodeFound();
	else
		throw new CompletionNodeFound(this, receiverType, scope); // array types are passed along to find the length field
}
public String toStringExpression(){
	/* slow code */
	
	return 	"<CompleteOnMemberAccess:"  //$NON-NLS-1$
			+ super.toStringExpression() 
			+ ">"; //$NON-NLS-1$
}
}
