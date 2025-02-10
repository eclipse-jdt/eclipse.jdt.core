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

import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.CompilerConfiguration;
import org.eclipse.jdt.internal.compiler.ICompilerFactory;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;

public class JavacCompilerFactory implements ICompilerFactory {

	@Override
	public Compiler newCompiler(INameEnvironment environment, IErrorHandlingPolicy policy,
			CompilerConfiguration compilerConfig, ICompilerRequestor requestor, IProblemFactory problemFactory,
			CompilationProgress compilationProgress) {
		return new JavacCompiler(environment, policy, compilerConfig, requestor, problemFactory, compilationProgress);
	}
}
