/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public abstract class AbstractVariableDeclaration extends Statement implements InvocationSite {
	public int declarationEnd;
	public int declarationSourceEnd;
	public int declarationSourceStart;
	public int hiddenVariableDepth; // used to diagnose hiding scenarii
	public Expression initialization;
	public int modifiers;
	public int modifiersSourceStart;
	public Annotation[] annotations;

	public char[] name;

	public TypeReference type;
	
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		return flowInfo;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
	 */
	public TypeBinding[] genericTypeArguments() {
		return null;
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

	
	public StringBuffer printStatement(int indent, StringBuffer output) {

		printIndent(indent, output);
		printModifiers(this.modifiers, output);
		type.print(0, output).append(' ').append(this.name); 
		if (initialization != null) {
			output.append(" = "); //$NON-NLS-1$
			initialization.printExpression(indent, output);
		}
		return output.append(';');
	}

	public void resolve(BlockScope scope) {
		// do nothing by default (redefined for local variables)
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#setActualReceiverType(org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding)
	 */
	public void setActualReceiverType(ReferenceBinding receiverType) {
		// do nothing by default
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
		// do nothing by default
	}
}
