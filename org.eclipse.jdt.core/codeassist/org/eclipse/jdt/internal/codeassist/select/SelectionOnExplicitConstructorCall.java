package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Selection node build by the parser in any case it was intending to
 * reduce an explicit constructor call containing the cursor.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      Y.[start]super[end](1, 2)
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <SelectOnExplicitConstructorCall:this.bar(1, 2)>
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class SelectionOnExplicitConstructorCall extends ExplicitConstructorCall {
public SelectionOnExplicitConstructorCall(int accessMode) {
	super(accessMode);
}
public void resolve(BlockScope scope) {
	super.resolve(scope);

	if (binding == null || !binding.isValidBinding())
		throw new SelectionNodeFound();
	else
		throw new SelectionNodeFound(binding);
}
public String toString(int tab) {
	String s = tabString(tab);
	s += "<SelectOnExplicitConstructorCall:"/*nonNLS*/;
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
