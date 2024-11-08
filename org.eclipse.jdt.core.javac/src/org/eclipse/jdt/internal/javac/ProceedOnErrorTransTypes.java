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

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.comp.TransTypes;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Context.Factory;

public class ProceedOnErrorTransTypes extends TransTypes {
	
	public static void preRegister(Context context) {
		context.put(transTypesKey, (Factory<TransTypes>) c -> new ProceedOnErrorTransTypes(c));
	}

	protected ProceedOnErrorTransTypes(Context context) {
		super(context);
	}

	@Override
	public void visitApply(JCMethodInvocation tree) {
		tree.meth = translate(tree.meth, null);
		Symbol meth = TreeInfo.symbol(tree.meth);
		if (meth.baseSymbol() instanceof MethodSymbol) {
			//workaround: guard against ClassCastException when referencing non existing member
			super.visitApply(tree);
		}
	}

}
