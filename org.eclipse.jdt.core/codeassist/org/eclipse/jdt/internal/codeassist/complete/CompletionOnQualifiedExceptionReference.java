package org.eclipse.jdt.internal.codeassist.complete;

public class CompletionOnQualifiedExceptionReference extends CompletionOnQualifiedTypeReference {
public CompletionOnQualifiedExceptionReference(char[][] previousIdentifiers, char[] completionIdentifier, long[] positions) {
	super(previousIdentifiers, completionIdentifier, positions);
}
public String toStringExpression(int tab) {

	StringBuffer buffer = new StringBuffer();
	buffer.	append("<CompleteOnException:"/*nonNLS*/);
	for (int i = 0; i < tokens.length; i++) {
		buffer.append(tokens[i]);
		buffer.append("."/*nonNLS*/);
	}
	buffer.append(completionIdentifier).append(">"/*nonNLS*/);
	return buffer.toString();
}
}
