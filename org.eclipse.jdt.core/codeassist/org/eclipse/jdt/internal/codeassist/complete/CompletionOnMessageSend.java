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
		if (arguments != null) {
			int argsLength = arguments.length;
			for (int a = argsLength; --a >= 0;)
				arguments[a].resolveType(scope);
		}
		
		if (receiver.isImplicitThis())
			throw new CompletionNodeFound(this, null, scope);

		this.receiverType = receiver.resolveType(scope);
		if (this.receiverType == null || this.receiverType.isBaseType())
			throw new CompletionNodeFound();

		if (this.receiverType.isArrayType())
			this.receiverType = scope.getJavaLangObject();
		throw new CompletionNodeFound(this, this.receiverType, scope);
	}

	public String toStringExpression() {

		String s = "<CompleteOnMessageSend:"; //$NON-NLS-1$
		if (!receiver.isImplicitThis())
			s = s + receiver.toStringExpression() + "."; //$NON-NLS-1$
		s = s + new String(selector) + "("; //$NON-NLS-1$
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				s += arguments[i].toStringExpression();
				if (i != arguments.length - 1) {
					s += ", "; //$NON-NLS-1$
				}
			};
		}
		s = s + ")>"; //$NON-NLS-1$
		return s;
	}
}
