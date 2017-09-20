/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.internal.compiler.ast.RequiresStatement;

public class RecoveredRequiresStatement extends RecoveredModuleStatement {

	public RecoveredRequiresStatement(RequiresStatement requiresStatement, RecoveredElement parent, int bracketBalance) {
		super(requiresStatement, parent, bracketBalance);
	}
	public String toString(int tab) {
		return tabString(tab) + "Recovered requires: " + super.toString(); //$NON-NLS-1$
	}
	public RequiresStatement updatedRequiresStatement(){
		return (RequiresStatement)this.moduleStatement;
	}
}
