/*******************************************************************************
 * Copyright (c) 2023, 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.javac;

import java.util.function.Predicate;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.comp.TransTypes;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Context.Factory;

public class ProceedOnErrorTransTypes extends TransTypes {
	
	public static void preRegister(Context context) {
		context.put(transTypesKey, (Factory<TransTypes>) c -> new ProceedOnErrorTransTypes(c));
	}

	private boolean needsProceedOnError;
	private Context context;

	protected ProceedOnErrorTransTypes(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public void visitClassDef(JCClassDecl def) {
		this.needsProceedOnError = hasErrors(def);
		super.visitClassDef(def);
		this.needsProceedOnError = false;
	}

	private boolean hasErrors(JCClassDecl def) {
		return this.context.get(JavacCompiler.FILES_WITH_ERRORS_KEY).contains(def.sym.sourcefile);
	}

	@Override
	public void visitApply(JCMethodInvocation tree) {
		if (!isValid(tree) || tree.args.stream().anyMatch(Predicate.not(this::isValid))) {
			return;
		}
		// The next lines of code should allow to generate
		// classes with errors, but they are sometimes
		// causing infinite processing for files that 
		// have no errors (eg with XLargeTests).
		// So at the moment we guard them by `needProceedOnError`
		// but those lines must be considered fragile and made
		// more bullet proof; concretely then need to work with
		// XLargeTest.
		// Cf https://github.com/eclipse-jdtls/eclipse-jdt-core-incubator/issues/1008
		
		Symbol meth = TreeInfo.symbol(tree.meth);
		if (!(meth.baseSymbol() instanceof MethodSymbol)) {
			//workaround: guard against ClassCastException when referencing non existing member
			return;
		}
		super.visitApply(tree);
	}

	@Override
	public void visitAssign(JCAssign tree) {
		if (!isValid(tree.lhs) || !isValid(tree.rhs)) {
			return;
		}
		super.visitAssign(tree);
	}

	@Override
	public void visitTypeCast(JCTypeCast tree) {
		if (!isValid(tree) || !isValid(tree.expr)) {
			return;
		}
		super.visitTypeCast(tree);
	}

	@Override
	public void visitParens(JCParens tree) {
		if (!isValid(tree)) {
			return;
		}
		super.visitParens(tree);
	}

	private boolean isValid(JCTree tree) {
		return tree != null && tree.type != null && !tree.type.isErroneous();
	}
}
