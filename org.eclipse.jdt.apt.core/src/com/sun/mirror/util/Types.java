/*
 * @(#)Types.java	1.3 04/06/07
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

package com.sun.mirror.util;


import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.type.WildcardType;
import java.util.Collection;


/**
 * Utility methods for operating on types.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.3 04/06/07
 * @since 1.5
 */

public interface Types {

    /**
     * Tests whether one type is a subtype of the another.
     * Any type is considered to be a subtype of itself.
     *
     * @param t1  the first type
     * @param t2  the second type
     * @return <code>true</code> if and only if the first type is a subtype
     *		of the second
     */
    boolean isSubtype(TypeMirror t1, TypeMirror t2);

    /**
     * Tests whether one type is assignable to another.
     *
     * @param t1  the first type
     * @param t2  the second type
     * @return <code>true</code> if and only if the first type is assignable
     *		to the second
     */
    boolean isAssignable(TypeMirror t1, TypeMirror t2);

    /**
     * Returns the erasure of a type.
     *
     * @param t  the type to be erased
     * @return the erasure of the given type
     */
    TypeMirror getErasure(TypeMirror t);

    /**
     * Returns a primitive type.
     *
     * @param kind  the kind of primitive type to return
     * @return a primitive type
     */
    PrimitiveType getPrimitiveType(PrimitiveType.Kind kind);

    /**
     * Returns the pseudo-type representing the type of <code>void</code>.
     *
     * @return the pseudo-type representing the type of <code>void</code>
     */
    VoidType getVoidType();

    /**
     * Returns an array type with the specified component type.
     *
     * @param componentType  the component type
     * @return an array type with the specified component type.
     * @throws IllegalArgumentException if the component type is not valid for
     *		an array
     */
    ArrayType getArrayType(TypeMirror componentType);

    /**
     * Returns the type variable declared by a type parameter.
     *
     * @param tparam  the type parameter
     * @return the type variable declared by the type parameter
     */
    TypeVariable getTypeVariable(TypeParameterDeclaration tparam);

    /**
     * Returns a new wildcard.
     * Either the wildcards's upper bounds or lower bounds may be
     * specified, or neither, but not both.
     *
     * @param upperBounds  the upper bounds of this wildcard,
     *		or an empty collection if none
     * @param lowerBounds  the lower bounds of this wildcard,
     *		or an empty collection if none
     * @return a new wildcard
     * @throws IllegalArgumentException if bounds are not valid
     */
    WildcardType getWildcardType(Collection<ReferenceType> upperBounds,
				 Collection<ReferenceType> lowerBounds);

    /**
     * Returns the type corresponding to a type declaration and
     * actual type arguments.
     * Given the declaration for <code>String</code>, for example, this
     * method may be used to get the <code>String</code> type.  It may
     * then be invoked a second time, with the declaration for <code>Set</code>,
     * to make the parameterized type {@code Set<String>}.
     *
     * <p> The number of type arguments must either equal the
     * number of the declaration's formal type parameters, or must be
     * zero.  If zero, and if the declaration is generic,
     * then the declaration's raw type is returned.
     *
     * <p> If a parameterized type is being returned, its declaration
     * must not be contained within a generic outer class.
     * The parameterized type {@code Outer<String>.Inner<Number>},
     * for example, may be constructed by first using this
     * method to get the type {@code Outer<String>}, and then invoking
     * {@link #getDeclaredType(DeclaredType, TypeDeclaration, TypeMirror...)}.
     *
     * @param decl	the type declaration
     * @param typeArgs	the actual type arguments
     * @return the type corresponding to the type declaration and
     *		actual type arguments
     * @throws IllegalArgumentException if too many or too few
     *		type arguments are given, or if an inappropriate type
     *		argument or declaration is provided
     */
    DeclaredType getDeclaredType(TypeDeclaration decl,
				 TypeMirror... typeArgs);

    /**
     * Returns the type corresponding to a type declaration
     * and actual arguments, given a
     * {@linkplain DeclaredType#getContainingType() containing type}
     * of which it is a member.
     * The parameterized type {@code Outer<String>.Inner<Number>},
     * for example, may be constructed by first using
     * {@link #getDeclaredType(TypeDeclaration, TypeMirror...)}
     * to get the type {@code Outer<String>}, and then invoking
     * this method.
     *
     * <p> If the containing type is a parameterized type,
     * the number of type arguments must equal the
     * number of the declaration's formal type parameters.
     * If it is not parameterized or if it is <code>null</code>, this method is
     * equivalent to <code>getDeclaredType(decl, typeArgs)</code>.
     *
     * @param containing  the containing type, or <code>null</code> if none
     * @param decl	  the type declaration
     * @param typeArgs	  the actual type arguments
     * @return the type corresponding to the type declaration and
     *		actual type arguments,
     *		contained within the given type
     * @throws IllegalArgumentException if too many or too few
     *		type arguments are given, or if an inappropriate type
     *		argument, declaration, or containing type is provided
     */
    DeclaredType getDeclaredType(DeclaredType containing,
				 TypeDeclaration decl,
				 TypeMirror... typeArgs);
}
