package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Selection node build by the parser in any case it was intending to
 * reduce an package statement containing the assist identifier.
 * e.g.
 *
 *  package java.[start]io[end];
 *	class X {
 *    void foo() {
 *    }
 *  }
 *
 *	---> <SelectOnPackage:java.io>
 *		 class X {
 *         void foo() {
 *         }
 *       }
 *
 */
 
import org.eclipse.jdt.internal.compiler.ast.*;

public class SelectionOnPackageReference extends ImportReference {
public SelectionOnPackageReference(char[][] tokens , long[] positions) {
	super(tokens, positions, true);
}
public String toString(int tab, boolean withOnDemand) {
	StringBuffer buffer = new StringBuffer(tabString(tab));
	buffer.	append("<SelectOnPackage:"/*nonNLS*/);
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
