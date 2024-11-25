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

import static com.sun.tools.javac.jvm.ByteCodes.athrow;

import com.sun.tools.javac.code.Kinds.Kind;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.jvm.Gen;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

// Handled case so far:
// * Syntax error as direct statement
// What's missing so far
// * Syntax error not as direct child of statement
// * resolution errors
public class ProceedOnErrorGen extends Gen {

	public static void preRegister(Context context) {
		context.put(Gen.genKey, (Context.Factory<Gen>) ProceedOnErrorGen::new);
	}

	private Context context;

	protected ProceedOnErrorGen(Context context) {
		super(context);
		this.context = context;
	}

	private JCNewClass newRuntimeException() {
		var treeMaker = TreeMaker.instance(context);
		var res = treeMaker.NewClass(null, null, treeMaker.Ident(Names.instance(context).fromString(RuntimeException.class.getSimpleName())), List.of(treeMaker.Literal("Compile Error")), null);
		Attr.instance(context).attribStat(res, getAttrEnv());
		res.type = res.clazz.type; // ugly workaround
		return res;
	}

	@Override
	public void visitErroneous(JCErroneous that) {
		// workaround visitThrow assertions
		visitNewClass(newRuntimeException());
		getCode().emitop0(athrow);
	}

	@Override
	public void visitIdent(JCIdent tree) {
		if (tree.sym == null || tree.sym.kind == Kind.ERR || tree.sym.kind == Kind.STATICERR) {
			visitErroneous(null);
		} else {
			super.visitIdent(tree);
		}
	}

	@Override
	public void visitLiteral(JCLiteral tree) {
		if (tree.type == null || tree.type.isErroneous()) {
			visitErroneous(null);
		} else {
			super.visitLiteral(tree);
		}
	}
	
	@Override
	public void visitNewClass(JCNewClass tree) {
		if (tree.type == null || tree.type.isErroneous()) {
			visitErroneous(null);
		} else {
			super.visitNewClass(tree);
		}
	}

	@Override
	public void visitApply(JCMethodInvocation tree) {
		if (tree.type.isErroneous()) {
			visitErroneous(null);
		} else {
			super.visitApply(tree);
		}
	}

	@Override
	public void visitSelect(JCFieldAccess tree) {
		if (tree.type.isErroneous()) {
			visitErroneous(null);
		} else {
			super.visitSelect(tree);
		}
	}

	@Override
	public void visitExec(JCExpressionStatement tree) {
		if (tree.expr == null || tree.expr instanceof JCErroneous || tree.expr.type.isErroneous()) {
			visitErroneous(null);
		} else {
			super.visitExec(tree);
		}
	}

	@Override
	public void visitAssign(JCAssign tree) {
		if (tree.lhs.type.isErroneous()) {
			visitErroneous(null);
		} else {
			super.visitAssign(tree);
		}
	}

	@Override
	public void visitIndexed(JCArrayAccess tree) {
		if (tree.type.isErroneous() || tree.getIndex() == null) {
			visitErroneous(null);
		} else {
			super.visitIndexed(tree);
		}
	}
}
