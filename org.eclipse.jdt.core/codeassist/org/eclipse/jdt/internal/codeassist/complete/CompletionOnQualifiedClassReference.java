package org.eclipse.jdt.internal.codeassist.complete;

/**
 * @author davida
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class CompletionOnQualifiedClassReference extends CompletionOnQualifiedTypeReference {
public CompletionOnQualifiedClassReference(char[][] previousIdentifiers, char[] completionIdentifier, long[] positions) {
	super(previousIdentifiers, completionIdentifier, positions);
}
public String toStringExpression(int tab) {

	StringBuffer buffer = new StringBuffer();
	buffer. append("<CompleteOnClass:"); //$NON-NLS-1$
	for (int i = 0; i < tokens.length; i++) {
		buffer.append(tokens[i]);
		buffer.append("."); //$NON-NLS-1$
	}
	buffer.append(completionIdentifier).append(">"); //$NON-NLS-1$
	return buffer.toString();
}
}
