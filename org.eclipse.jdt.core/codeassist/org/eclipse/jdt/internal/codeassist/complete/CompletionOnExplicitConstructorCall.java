package org.eclipse.jdt.internal.codeassist.complete;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Completion node build by the parser in any case it was intending to
 * reduce a explicit constructor call containing the cursor.
 * e.g.
 *
 *	class X {
 *    X() {
 *      this(1, 2, [cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         X() {
 *           <CompleteOnExplicitConstructorCall:this(1, 2)>
 *         }
 *       }
 *
 * The source range is always of length 0.
 * The arguments of the constructor call are all the arguments defined
 * before the cursor.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionOnExplicitConstructorCall extends ExplicitConstructorCall {
public CompletionOnExplicitConstructorCall(int accessMode) {
	super(accessMode);
}
public void resolve(BlockScope scope) {
	ReferenceBinding receiverType = scope.enclosingSourceType();

	if (accessMode != This && receiverType != null) {
		if (receiverType.isHierarchyInconsistent())
			throw new CompletionNodeFound();
		receiverType = receiverType.superclass();
	}
	if (receiverType == null)
		throw new CompletionNodeFound();
	else
		throw new CompletionNodeFound(this, receiverType, scope);
}
public String toString(int tab) {
	String s = tabString(tab);
	s += "<CompleteOnExplicitConstructorCall:"/*nonNLS*/;
	if (qualification != null)
		s = s + qualification.toStringExpression() + "."/*nonNLS*/;
	if (accessMode == This) {
		s = s + "this("/*nonNLS*/;
	} else {
		s = s + "super("/*nonNLS*/;
	}
	if (arguments != null) {
		for (int i = 0; i < arguments.length; i++) {
			s += arguments[i].toStringExpression();
			if (i != arguments.length - 1) {
				s += ", "/*nonNLS*/;
			}
		};
	}
	s += ")>"/*nonNLS*/;
	return s;
}
}
