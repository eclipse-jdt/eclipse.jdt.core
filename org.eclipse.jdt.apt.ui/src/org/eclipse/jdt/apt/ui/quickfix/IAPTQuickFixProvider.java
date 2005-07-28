/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    dsomerfi@bea.com - initial API and implementation
 *    
 *******************************************************************************/
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
