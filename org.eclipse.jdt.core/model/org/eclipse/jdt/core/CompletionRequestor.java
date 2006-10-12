/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Abstract base class for a completion requestor which is passed completion
 * proposals as they are generated in response to a code assist request.
 * <p>
 * This class is intended to be subclassed by clients.
 * </p>
 * <p>
 * The code assist engine normally invokes methods on completion
 * requestors in the following sequence:
 * <pre>
 * requestor.beginReporting();
 * requestor.acceptContext(context);
 * requestor.accept(proposal_1);
 * requestor.accept(proposal_2);
 * ...
 * requestor.endReporting();
 * </pre>
 * If, however, the engine is unable to offer completion proposals
 * for whatever reason, <code>completionFailure</code> is called
 * with a problem object describing why completions were unavailable.
 * In this case, the sequence of calls is:
 * <pre>
 * requestor.beginReporting();
 * requestor.acceptContext(context);
 * requestor.completionFailure(problem);
 * requestor.endReporting();
 * </pre>
 * In either case, the bracketing <code>beginReporting</code>
 * <code>endReporting</code> calls are always made as well as
 * <code>acceptContext</code> call.
 * </p>
 * <p>
 * The class was introduced in 3.0 as a more evolvable replacement
 * for the <code>ICompletionRequestor</code> interface.
 * </p>
 * 
 * @see ICodeAssist
 * @since 3.0
 */
public abstract class CompletionRequestor {

	/**
	 * The set of CompletionProposal kinds that this requestor
	 * ignores; <code>0</code> means the set is empty.
	 * 1 << completionProposalKind
	 */
	private int ignoreSet = 0;
	
	/**
	 * The set of CompletionProposal kinds that this requestor
	 * allows for required proposals; <code>0</code> means the set is empty.
	 * 1 << completionProposalKind
	 */
	private int requiredProposalAllowSet = 0;

	/**
	 * Creates a new completion requestor.
	 * The requestor is interested in all kinds of completion
	 * proposals; none will be ignored.
	 */
	public CompletionRequestor() {
		// do nothing
	}

	/**
	 * Returns whether the given kind of completion proposal is ignored.
	 * 
	 * @param completionProposalKind one of the kind constants declared
	 * on <code>CompletionProposal</code>
	 * @return <code>true</code> if the given kind of completion proposal
	 * is ignored by this requestor, and <code>false</code> if it is of
	 * interest
	 * @see #setIgnored(int, boolean)
	 * @see CompletionProposal#getKind()
	 */
	public boolean isIgnored(int completionProposalKind) {
		if (completionProposalKind < CompletionProposal.FIRST_KIND
			|| completionProposalKind > CompletionProposal.LAST_KIND) {
				throw new IllegalArgumentException("Unknown kind of completion proposal: "+completionProposalKind); //$NON-NLS-1$
		}
		return 0 != (this.ignoreSet & (1 << completionProposalKind));
	}
	
	/**
	 * Sets whether the given kind of completion proposal is ignored.
	 * 
	 * @param completionProposalKind one of the kind constants declared
	 * on <code>CompletionProposal</code>
	 * @param ignore <code>true</code> if the given kind of completion proposal
	 * is ignored by this requestor, and <code>false</code> if it is of
	 * interest
	 * @see #isIgnored(int)
	 * @see CompletionProposal#getKind()
	 */
	public void setIgnored(int completionProposalKind, boolean ignore) {
		if (completionProposalKind < CompletionProposal.FIRST_KIND
			|| completionProposalKind > CompletionProposal.LAST_KIND) {
				throw new IllegalArgumentException("Unknown kind of completion proposal: "+completionProposalKind); //$NON-NLS-1$
		}
		if (ignore) {
			this.ignoreSet |= (1 << completionProposalKind);
		} else {
			this.ignoreSet &= ~(1 << completionProposalKind);
		}
	}
	
	/**
	 * Returns whether a proposal with a required proposal
	 * of the given kind is allowed.
	 * 
	 * @param completionProposalKind one of the kind constants declared
	 * on <code>CompletionProposal</code>
	 * @return <code>true</code> if a proposal with a required proposal
	 * of the given kind is allowed by this requestor, and <code>false</code> 
	 * if it isn't of interest.
	 * <p>
	 * By default, all kinds of required proposals aren't allowed.
	 * </p>
	 * @see #setAllowsRequiredProposals(int, boolean)
	 * @see CompletionProposal#getKind()
	 * @see CompletionProposal#getRequiredProposals()
	 * 
	 * @since 3.3
	 */
	public boolean isAllowingRequiredProposals(int completionProposalKind) {
		if (completionProposalKind < CompletionProposal.FIRST_KIND
			|| completionProposalKind > CompletionProposal.LAST_KIND) {
				throw new IllegalArgumentException("Unknown kind of completion proposal: "+completionProposalKind); //$NON-NLS-1$
		}
		return 0 != (this.requiredProposalAllowSet & (1 << completionProposalKind));
	}
	
	/**
	 * Sets whether a proposal with a required proposal
	 * of the given kind is allowed.
	 * 
	 * @param completionProposalKind one of the kind constants declared
	 * on <code>CompletionProposal</code>
	 * @param allow <code>true</code> if a proposal with a required proposal
	 * of the given kind is allowed by this requestor, and <code>false</code> 
	 * if it isn't of interest
	 * @see #isAllowingRequiredProposals(int)
	 * @see CompletionProposal#getKind()
	 * @see CompletionProposal#getRequiredProposals()
	 * 
	 * @since 3.3
	 */
	public void setAllowsRequiredProposals(int completionProposalKind, boolean allow) {
		if (completionProposalKind < CompletionProposal.FIRST_KIND
			|| completionProposalKind > CompletionProposal.LAST_KIND) {
				throw new IllegalArgumentException("Unknown kind of completion proposal: "+completionProposalKind); //$NON-NLS-1$
		}
		if (allow) {
			this.requiredProposalAllowSet |= (1 << completionProposalKind);
		} else {
			this.requiredProposalAllowSet &= ~(1 << completionProposalKind);
		}
	}
	
	/**
	 * Pro forma notification sent before reporting a batch of
	 * completion proposals.
	 * <p>
	 * The default implementation of this method does nothing.
	 * Clients may override.
	 * </p>
	 */
	public void beginReporting() {
		// do nothing
	}

	/**
	 * Pro forma notification sent after reporting a batch of
	 * completion proposals.
	 * <p>
	 * The default implementation of this method does nothing.
	 * Clients may override.
	 * </p>
	 */
	public void endReporting() {
		// do nothing
	}

	/**
	 * Notification of failure to produce any completions.
	 * The problem object explains what prevented completing.
	 * <p>
	 * The default implementation of this method does nothing.
	 * Clients may override to receive this kind of notice.
	 * </p>
	 * 
	 * @param problem the problem object
	 */
	public void completionFailure(IProblem problem) {
		// default behavior is to ignore
	}

	/**
	 * Proposes a completion. Has no effect if the kind of proposal
	 * is being ignored by this requestor. Callers should consider
	 * checking {@link #isIgnored(int)} before avoid creating proposal
	 * objects that would only be ignored.
	 * <p>
	 * Similarly, implementers should check 
	 * {@link #isIgnored(int) isIgnored(proposal.getKind())} 
	 * and ignore proposals that have been declared as uninteresting.
	 * The proposal object passed is only valid for the duration of
	 * completion operation.
	 * 
	 * @param proposal the completion proposal
	 * @exception IllegalArgumentException if the proposal is null
	 */
	public abstract void accept(CompletionProposal proposal);
	
	/**
	 * Propose the context in which the completion occurs.
	 * <p>
	 * This method is called one and only one time before any call to
	 * {@link #accept(CompletionProposal)}.
	 * The default implementation of this method does nothing.
	 * Clients may override.
	 * </p>
	 * @param context the completion context
	 * 
	 * @since 3.1
	 */
	public void acceptContext(CompletionContext context) {
		// do nothing
	}
}
