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

import java.util.Queue;

import javax.tools.JavaFileObject;

import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.CompileStates.CompileState;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Pair;

public class TolerantJavaCompiler extends JavaCompiler {
	boolean isInGeneration = false;

	public TolerantJavaCompiler(Context context) {
		super(context);
	}

	@Override
	protected boolean shouldStop(CompileState cs) {
		// Never stop
		return false;
	}

	@Override
	public void generate(Queue<Pair<Env<AttrContext>, JCClassDecl>> queue, Queue<JavaFileObject> results) {
		try {
			this.isInGeneration = true;
			super.generate(queue, results);
		} catch (Throwable ex) {
			// TODO error handling
		} finally {
			this.isInGeneration = false;
		}
	}

	@Override
	protected void desugar(Env<AttrContext> env, Queue<Pair<Env<AttrContext>, JCClassDecl>> results) {
		try {
			super.desugar(env, results);
		} catch (Throwable ex) {
			// TODO error handling
		}
	}

	@Override
	public int errorCount() {
		// See JavaCompiler.genCode(Env<AttrContext> env, JCClassDecl cdef),
		// it stops writeClass if errorCount is not zero.
		// Force it to return 0 if we are in generation phase, and keeping
		// generating class files for those files without errors.
		return this.isInGeneration ? 0 : super.errorCount();
	}

	public static JavaCompiler configureCompilerInstance(Context context) {
		TolerantJavaCompiler javacCompiler = new TolerantJavaCompiler(context);
		context.put(JavaCompiler.compilerKey, javacCompiler);
		return javacCompiler;
	}
}
