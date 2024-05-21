/*******************************************************************************
* Copyright (c) 2024 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.internal.compiler.codegen;

import java.util.Stack;
import java.util.function.Supplier;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class OperandStack {

	private Stack<TypeBinding> stack;
	private ClassFile classFile;

	public OperandStack() {}

	@SuppressWarnings("unchecked")
	OperandStack(OperandStack operandStack) {
		this.stack = (Stack<TypeBinding>) operandStack.stack.clone();
		this.classFile = operandStack.classFile;
	}

	public OperandStack(ClassFile classFile) {
		this.stack = new Stack<>();
		this.classFile = classFile;
	}

	public int size() {
		return this.stack.size();
	}

	public TypeBinding get(int index) {
		return this.stack.get(index);
	}

	public void clear() {
		this.stack.clear();
	}

	protected OperandStack copy() {
		return new OperandStack(this);
	}

	public TypeBinding pop() {
		return this.stack.pop();
	}

	public void push(TypeBinding typeBinding) {
		if (typeBinding == null) {
			throw new AssertionError("Attempt to push null on operand stack!"); //$NON-NLS-1$
		}
		/* 4.9.2 Structural Constraints: ...
		   An instruction operating on values of type int is also permitted to operate on
		   values of type boolean, byte, char, and short.
		   As noted in §2.3.4 and §2.11.1, the Java Virtual Machine internally converts values of
		   types boolean, byte, short, and char to type int.)
		*/
		switch(typeBinding.id) {
			case TypeIds.T_boolean:
			case TypeIds.T_byte:
			case TypeIds.T_short:
			case TypeIds.T_char:
				typeBinding = TypeBinding.INT;
		}
		this.stack.push(typeBinding);
	}

	private TypeBinding getPopularBinding(char[] typeName) {
		Scope scope = this.classFile.referenceBinding.scope;
		assert scope != null;
		Supplier<ReferenceBinding> finder = scope.getCommonReferenceBinding(typeName);
		return finder != null ? finder.get() : TypeBinding.NULL;
	}

	public void push(char[] typeName) {
		push(getPopularBinding(typeName));
	}

	public TypeBinding peek() {
		return this.stack.peek();
	}

	public void xaload() { // [... arrayref, index] -> [... element]
		this.stack.pop();
		TypeBinding type = this.stack.pop();
		this.stack.push(((ArrayBinding) type).elementsType());
	}

	public boolean depthEquals(int expected) {
		int depth = 0;
		for (int i = 0, size = size(); i < size; i++) {
			TypeBinding t = get(i);
			depth += TypeIds.getCategory(t.id);
		}
		return depth == expected;
	}

	public static class NullStack extends OperandStack {
		public NullStack() {
			return;
		}
		@Override
		public int size() {
			return 0;
		}
		@Override
		public void clear() {
			return;
		}
		@Override
		protected NullStack copy()   {
			return new NullStack();
		}
		@Override
		public TypeBinding pop() {
			return TypeBinding.VOID;
		}
		@Override
		public void push(TypeBinding typeBinding) {
			return;
		}
		@Override
		public TypeBinding peek() {
			return TypeBinding.VOID;
		}
		@Override
		public TypeBinding get(int index) {
			return TypeBinding.VOID;
		}
		@Override
		public void push(char[] typeName) {
			return;
		}
		@Override
		public void xaload() { // [... arrayref, index] -> [... element]
			return;
		}
		@Override
		public boolean depthEquals(int expected) {
			return true;
		}
	}
}
