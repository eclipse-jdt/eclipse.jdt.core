package org.eclipse.jdt.apt.ui.quickfix;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

/**
 * APT plugins may implement this api in order to 
 * add assists for errors they create.
 */
public interface IAPTQuickFixProvider {
    
    /**
     * Return the proposals to suggest
     * @param context
     * @param locations
     * @return
     * @throws CoreException
     */
    public IJavaCompletionProposal [] getProposals(IInvocationContext context,
            IProblemLocation [] locations) throws CoreException;
}
