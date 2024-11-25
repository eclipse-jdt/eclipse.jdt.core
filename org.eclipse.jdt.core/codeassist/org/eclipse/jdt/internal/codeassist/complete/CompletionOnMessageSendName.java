/*******************************************************************************
 * Copyright (c) 2006, 2024 IBM Corporation and others.
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
package org.eclipse.jdt.internal.codeassist.complete;

import java.util.function.Consumer;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Initially (https://bugs.eclipse.org/106450) this class was only used when an invocation has
 * actual type arguments.
 * Since https://bugs.eclipse.org/539685 it is also used for non-parameterized invocations,
 * and signals that the selector is to be matched inexactly (in contrast to CompletionOnMessageSend)..
 */
public class CompletionOnMessageSendName extends MessageSend implements CompletionNode {

	public boolean nextIsCast;
	public boolean cursorIsToTheLeftOfTheLParen;

	public CompletionOnMessageSendName(char[] selector, int start, int end) {
		this(selector, start, end, setup -> {/* use defaults */});
	}

	public CompletionOnMessageSendName(char[] selector, int start, int end, Consumer<CompletionOnMessageSendName> setup) {
		super();
		this.selector = selector;
		this.sourceStart = start;
		this.sourceEnd = end;
		this.nameSourcePosition = end;
		setup.accept(this);
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {

		this.constant = Constant.NotAConstant;

		this.actualReceiverType = this.receiver.resolveType(scope);
		if (this.actualReceiverType == null || this.actualReceiverType.isBaseType() || this.actualReceiverType.isArrayType())
			throw new CompletionNodeFound();

		// resolve type arguments
		if (this.typeArguments != null) {
			int length = this.typeArguments.length;
			this.genericTypeArguments = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				this.genericTypeArguments[i] = this.typeArguments[i].resolveType(scope, true /* check bounds*/);
			}
		}

		throw new CompletionNodeFound(this, this.actualReceiverType, scope);
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {

		output.append("<CompleteOnMessageSendName:"); //$NON-NLS-1$
		if (!this.receiver.isImplicitThis()) this.receiver.printExpression(0, output).append('.');
		if (this.typeArguments != null) {
			output.append('<');
			int max = this.typeArguments.length - 1;
			for (int j = 0; j < max; j++) {
				this.typeArguments[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			this.typeArguments[max].print(0, output);
			output.append('>');
		}
		output.append(this.selector).append('(');
		return output.append(")>"); //$NON-NLS-1$
	}
}
