/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public abstract class AbstractVariableDeclaration extends Statement implements InvocationSite {
	public int declarationEnd;
	public int declarationSourceEnd;
	public int declarationSourceStart;
	public int hiddenVariableDepth; // used to diagnose hiding scenarii
	public Expression initialization;
	public int modifiers;
	public int modifiersSourceStart;

	public char[] name;

	public TypeReference type;
	
	public AbstractVariableDeclaration() {}

	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		return flowInfo;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#isSuperAccess()
	 */
	public boolean isSuperAccess() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#isTypeAccess()
	 */
	public boolean isTypeAccess() {
		return false;
	}

	public abstract String name();

	public void resolve(BlockScope scope) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#setActualReceiverType(org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding)
	 */
	public void setActualReceiverType(ReferenceBinding receiverType) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#setDepth(int)
	 */
	public void setDepth(int depth) {

		this.hiddenVariableDepth = depth;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#setFieldIndex(int)
	 */
	public void setFieldIndex(int depth) {
	}
		
	public String toString(int tab) {

		String s = tabString(tab);
		if (modifiers != AccDefault) {
			s += modifiersString(modifiers);
		}
		s += type.toString(0) + " " + new String(name()); //$NON-NLS-1$
		if (initialization != null)
			s += " = " + initialization.toStringExpression(tab); //$NON-NLS-1$
		return s;
	}

}
