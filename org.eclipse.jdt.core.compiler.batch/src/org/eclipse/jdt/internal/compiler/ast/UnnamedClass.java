/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Represents an unnamed class as defined in JEP 445
 */
public class UnnamedClass extends TypeDeclaration {

	public UnnamedClass(CompilationResult result) {
		super(result);
		this.modifiers = ClassFileConstants.AccDefault | ClassFileConstants.AccFinal;
		this.name = "<unnamed class>".toCharArray(); //$NON-NLS-1$
	}

}
