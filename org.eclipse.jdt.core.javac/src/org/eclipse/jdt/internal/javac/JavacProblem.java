/*******************************************************************************
* Copyright (c) 2024 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.jdt.internal.javac;

import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

public class JavacProblem extends DefaultProblem {
	private String javacCode;

	public JavacProblem(char[] originatingFileName, String message, String javacCode, int jdtProblemId, String[] stringArguments, int severity,
			int startPosition, int endPosition, int line, int column) {
		super(originatingFileName, message, jdtProblemId, stringArguments, severity, startPosition, endPosition, line, column);
		this.javacCode = javacCode;
	}

	@Override
	public String[] getExtraMarkerAttributeNames() {
		return new String[] { "javacCode" };
	}

	@Override
	public Object[] getExtraMarkerAttributeValues() {
		return new Object[] { this.javacCode };
	}
}
