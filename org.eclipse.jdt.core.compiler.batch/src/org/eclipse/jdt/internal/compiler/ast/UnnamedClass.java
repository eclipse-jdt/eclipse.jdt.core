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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Represents an unnamed class as defined in JEP 445
 */
public class UnnamedClass extends TypeDeclaration {

	private static String NAME_TEMPLATE = "<unnamed_class${0}>"; //$NON-NLS-1$

	public UnnamedClass(CompilationResult result) {
		super(result);
		this.modifiers = ClassFileConstants.AccDefault | ClassFileConstants.AccFinal;

		Path p = Paths.get(new String(result.fileName));
		String basename = p.getFileName().toString();
		String classSuffix;
		if (basename.endsWith(".java")) { //$NON-NLS-1$
			classSuffix = basename.substring(0, basename.length() - 5);
		} else {
			classSuffix = basename;
		}

		String nameString = MessageFormat.format(NAME_TEMPLATE, classSuffix);
		this.name = nameString.toCharArray();
	}

}
