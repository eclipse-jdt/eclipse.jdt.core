package org.eclipse.jdt.internal.codeassist.complete;

public class CompletionOnQualifiedExceptionReference extends CompletionOnQualifiedTypeReference {
public CompletionOnQualifiedExceptionReference(char[][] previousIdentifiers, char[] completionIdentifier, long[] positions) {
	super(previousIdentifiers, completionIdentifier, positions);
}
public String toStringExpression(int tab) {

	StringBuffer buffer = new StringBuffer();
	buffer.	append("<CompleteOnException:");
	for (int i = 0; i < tokens.length; i++) {
		buffer.append(tokens[i]);
		buffer.append(".");
	}
	buffer.append(completionIdentifier).append(">");
	return buffer.toString();
}
}
