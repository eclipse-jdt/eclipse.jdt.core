/*******************************************************************************
 * Copyright (c) 2024 GK Software SE, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.util;

public interface Tuples {

    static final class Pair<T> {
        private final T left;
        private final T right;

        public Pair(T left, T right) {
            this.left = left;
            this.right = right;
        }

        public T left() {
            return left;
        }

        public T right() {
            return right;
        }

        @java.lang.Override
        public boolean equals(java.lang.Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Pair) obj;
            return java.util.Objects.equals(this.left, that.left) &&
                   java.util.Objects.equals(this.right, that.right);
        }

        @java.lang.Override
        public int hashCode() {
            return java.util.Objects.hash(left, right);
        }

        @java.lang.Override
        public String toString() {
            return "Pair[" +
                   "left=" + left + ", " +
                   "right=" + right + ']';
        }
    }

}
