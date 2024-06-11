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

package org.eclipse.jdt.internal.compiler;

import java.util.Objects;

public class Either<L, R> {
	private final L left;
	private final R right;

	public static <L, R> Either<L, R> forLeft(L left) {
		return new Either<>(left, null);
	}

	public static <L, R> Either<L, R> forRight(R right) {
		return new Either<>(null, right);
	}

	protected Either(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return this.left;
	}

	public R getRight() {
		return this.right;
	}

	public Object get() {
		if (this.left != null)
			return this.left;
		if (this.right != null)
			return this.right;
		return null;
	}

	public boolean isLeft() {
		return this.left != null;
	}

	public boolean isRight() {
		return this.right != null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.left, this.right);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Either<?, ?> other = (Either<?, ?>) obj;
		return Objects.equals(this.left, other.left) && Objects.equals(this.right, other.right);
	}

	@Override
	public String toString() {
		return "Either [left=" + this.left + ", right=" + this.right + "]";  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
	}
}
