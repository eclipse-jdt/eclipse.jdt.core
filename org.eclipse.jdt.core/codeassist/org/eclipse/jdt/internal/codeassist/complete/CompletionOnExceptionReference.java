package org.eclipse.jdt.internal.codeassist.complete;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;

/*
 * Completion node build by the parser in any case it was intending to
 * reduce an exception type reference containing the completion identifier.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      try {
 *        bar();
 *      } catch (IOExc[cursor] e) {
 *      }
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           try {
 *             bar();
 *           } catch (<CompleteOnException:IOExc> e) {
 *           }
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */
public class CompletionOnExceptionReference extends CompletionOnSingleTypeReference {
public CompletionOnExceptionReference(char[] source, long pos) {
	super(source, pos);
}
public String toStringExpression(int tab) {
	return "<CompleteOnException:"/*nonNLS*/ + new String(token) + ">"/*nonNLS*/;
}
}
