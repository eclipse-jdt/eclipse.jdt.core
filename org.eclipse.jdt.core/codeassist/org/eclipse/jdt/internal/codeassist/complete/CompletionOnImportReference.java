package org.eclipse.jdt.internal.codeassist.complete;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Completion node build by the parser in any case it was intending to
 * reduce an import reference containing the cursor location.
 * e.g.
 *
 *  import java.io[cursor];
 *	class X {
 *    void foo() {
 *    }
 *  }
 *
 *	---> <CompleteOnImport:java.io>
 *		 class X {
 *         void foo() {
 *         }
 *       }
 *
 * The source range is always of length 0.
 * The arguments of the allocation expression are all the arguments defined
 * before the cursor.
 */

import org.eclipse.jdt.internal.compiler.ast.*;

public class CompletionOnImportReference extends ImportReference {

public CompletionOnImportReference(char[][] tokens , long[] positions) {
	super(tokens, positions, false);
}
public String toString(int tab, boolean withOnDemand) {

	StringBuffer buffer = new StringBuffer(tabString(tab));
	buffer.	append("<CompleteOnImport:"); //$NON-NLS-1$
	for (int i = 0; i < tokens.length; i++) {
		buffer.append(tokens[i]);
		if (i < (tokens.length - 1)) {
			buffer.append("."); //$NON-NLS-1$
		}
	}
	buffer.append(">"); //$NON-NLS-1$
	return buffer.toString();
}
}
