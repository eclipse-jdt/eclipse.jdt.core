package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Selection node build by the parser in any case it was intending to
 * reduce an import reference containing the assist identifier.
 * e.g.
 *
 *  import java.[start]io[end].*;
 *	class X {
 *    void foo() {
 *    }
 *  }
 *
 *	---> <SelectOnImport:java.io>
 *		 class X {
 *         void foo() {
 *         }
 *       }
 *
 */
 
import org.eclipse.jdt.internal.compiler.ast.*;

public class SelectionOnImportReference extends ImportReference {

public SelectionOnImportReference(char[][] tokens , long[] positions) {
	super(tokens, positions, false);
}
public String toString(int tab, boolean withOnDemand) {

	StringBuffer buffer = new StringBuffer(tabString(tab));
	buffer.	append("<SelectOnImport:"/*nonNLS*/);
	for (int i = 0; i < tokens.length; i++) {
		buffer.append(tokens[i]);
		if (i < (tokens.length - 1)) {
			buffer.append("."/*nonNLS*/);
		}
	}
	buffer.append(">"/*nonNLS*/);
	return buffer.toString();
}
}
