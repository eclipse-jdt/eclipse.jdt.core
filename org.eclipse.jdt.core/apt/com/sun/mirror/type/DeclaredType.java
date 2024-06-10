/*
 * @(#)DeclaredType.java	1.6 04/06/07
 *
 * Copyright (c) 2004, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of
 *       its contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.mirror.type;


import java.util.Collection;

import com.sun.mirror.declaration.TypeDeclaration;


/**
 * Represents a declared type, either a class type or an interface type.
 * This includes parameterized types such as {@code java.util.Set<String>}
 * as well as raw types.
 *
 * <p> While a <code>TypeDeclaration</code> represents the <i>declaration</i>
 * of a class or interface, a <code>DeclaredType</code> represents a class
 * or interface <i>type</i>, the latter being a use of the former.
 * See {@link TypeDeclaration} for more on this distinction.
 *
 * <p> A <code>DeclaredType</code> may represent a type
 * for which details (declaration, supertypes, <i>etc.</i>) are unknown.
 * This may be the result of a processing error, such as a missing class file,
 * and is indicated by {@link #getDeclaration()} returning <code>null</code>.
 * Other method invocations on such an unknown type will not, in general,
 * return meaningful results.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.6 04/06/07
 * @since 1.5
 */

public interface DeclaredType extends ReferenceType {

    /**
     * Returns the declaration of this type.
     *
     * <p> Returns null if this type's declaration is unknown.  This may
     * be the result of a processing error, such as a missing class file.
     *
     * @return the declaration of this type, or null if unknown
     */
    TypeDeclaration getDeclaration();

    /**
     * Returns the type that contains this type as a member.
     * Returns <code>null</code> if this is a top-level type.
     *
     * <p> For example, the containing type of {@code O.I<S>}
     * is the type {@code O}, and the containing type of
     * {@code O<T>.I<S>} is the type {@code O<T>}.
     *
     * @return the type that contains this type,
     * or <code>null</code> if this is a top-level type
     */
    DeclaredType getContainingType();

    /**
     * Returns (in order) the actual type arguments of this type.
     * For a generic type nested within another generic type
     * (such as {@code Outer<String>.Inner<Number>}), only the type
     * arguments of the innermost type are included.
     *
     * @return the actual type arguments of this type, or an empty collection
     * if there are none
     */
    Collection<TypeMirror> getActualTypeArguments();

    /**
     * Returns the interface types that are direct supertypes of this type.
     * These are the interface types implemented or extended
     * by this type's declaration, with any type arguments
     * substituted in.
     *
     * <p> For example, the interface type extended by
     * {@code java.util.Set<String>} is {@code java.util.Collection<String>}.
     *
     * @return the interface types that are direct supertypes of this type,
     * or an empty collection if there are none
     */
    Collection<InterfaceType> getSuperinterfaces();
}
