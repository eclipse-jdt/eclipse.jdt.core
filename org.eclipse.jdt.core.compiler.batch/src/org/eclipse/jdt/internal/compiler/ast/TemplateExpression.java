/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class TemplateExpression extends Expression {
	Expression processor;
	public StringTemplate template;
	private MessageSend invocation;
	public TemplateExpression(Expression processor, StringTemplate template) {
		this.processor = processor;
		this.template = template;
		this.sourceStart = processor.sourceStart;
		this.sourceEnd = template.sourceEnd;
	}
	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		this.processor.printExpression(0, output);
		output.append("."); //$NON-NLS-1$
		this.template.printExpression(0, output);
		return output;
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		if (this.constant != Constant.NotAConstant) {
			this.constant = Constant.NotAConstant;
		}
		this.template.resolve(scope);
		if (this.processor != null) {
			this.invocation = new MessageSend();
			this.invocation.receiver = this.processor;
			this.invocation.selector = "process".toCharArray(); // TODO make a constant //$NON-NLS-1$
			this.invocation.arguments = new Expression[] {this.template};
			this.invocation.resolve(scope);
			if (this.invocation.binding != null)
				this.resolvedType = this.invocation.binding.returnType;
		}
		// Validate processor is of expected type (java.lang.StringTemplate.Processor)
		// Create
		return this.resolvedType;
	}

	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		this.invocation.generateCode(currentScope, codeStream, true);
		codeStream.checkcast(this.invocation.binding.returnType);
	}
}
