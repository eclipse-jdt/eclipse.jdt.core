package org.eclipse.jdt.internal.codeassist.complete;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Completion node build by the parser in any case it was intending to
 * reduce a message send containing the cursor.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      this.bar(1, 2, [cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <CompleteOnMessageSend:this.bar(1, 2)>
 *         }
 *       }
 *
 * The source range is always of length 0.
 * The arguments of the message send are all the arguments defined
 * before the cursor.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionOnMessageSend extends MessageSend {
	public TypeBinding resolveType(BlockScope scope) {
		if (receiver == ThisReference.ThisImplicit)
			throw new CompletionNodeFound(this, null, scope);

		TypeBinding receiverType = receiver.resolveType(scope);
		if (receiverType == null || receiverType.isBaseType())
			throw new CompletionNodeFound();

		if (receiverType.isArrayType())
			receiverType = scope.getJavaLangObject();
		throw new CompletionNodeFound(this, receiverType, scope);
	}

	public String toStringExpression() {
		/*slow code*/

		String s = "<CompleteOnMessageSend:";
		if (receiver != ThisReference.ThisImplicit)
			s = s + receiver.toStringExpression() + ".";
		s = s + new String(selector) + "(";
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				s += arguments[i].toStringExpression();
				if (i != arguments.length - 1) {
					s += ", ";
				}
			};
		}
		s = s + ")>";
		return s;
	}

}
