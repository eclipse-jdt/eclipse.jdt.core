package org.eclipse.jdt.internal.codeassist.complete;

public class CompletionOnExceptionReference extends CompletionOnSingleTypeReference {
public CompletionOnExceptionReference(char[] source, long pos) {
	super(source, pos);
}
public String toStringExpression(int tab) {
	return "<CompleteOnException:"/*nonNLS*/ + new String(token) + ">"/*nonNLS*/;
}
}
