package org.eclipse.jdt.internal.codeassist.complete;

/**
 * @author davida
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class CompletionOnInterfaceReference extends CompletionOnSingleTypeReference {
	public CompletionOnInterfaceReference(char[] source, long pos) {
		super(source, pos);
	}
	public String toStringExpression(int tab) {
		return "<CompleteOnInterface:" + new String(token) + ">"; //$NON-NLS-2$ //$NON-NLS-1$
	}
}
