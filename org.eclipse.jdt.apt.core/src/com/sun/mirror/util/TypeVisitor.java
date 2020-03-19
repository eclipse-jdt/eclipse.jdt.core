/*
 * @(#)TypeVisitor.java	1.4 04/06/07
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


import com.sun.mirror.type.*;


/**
 * A visitor for types, in the style of the standard visitor design pattern.
 * This is used to operate on a type when the kind
 * of type is unknown at compile time.
 * When a visitor is passed to a type's
 * {@link TypeMirror#accept accept} method,
 * the most specific <code>visitXxx</code> method applicable to
 * that type is invoked.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.4 04/06/07
 * @since 1.5
 */

public interface TypeVisitor {

    /**
     * Visits a type mirror.
     *
     * @param t the type to visit
     */
    public void visitTypeMirror(TypeMirror t);

    /**
     * Visits a primitive type.

     * @param t the type to visit
     */
    public void visitPrimitiveType(PrimitiveType t);

    /**
     * Visits a void type.
     *
     * @param t the type to visit
     */
    public void visitVoidType(VoidType t);

    /**
     * Visits a reference type.
     *
     * @param t the type to visit
     */
    public void visitReferenceType(ReferenceType t);

    /**
     * Visits a declared type.
     *
     * @param t the type to visit
     */
    public void visitDeclaredType(DeclaredType t);

    /**
     * Visits a class type.
     *
     * @param t the type to visit
     */
    public void visitClassType(ClassType t);

    /**
     * Visits an enum type.
     *
     * @param t the type to visit
     */
    public void visitEnumType(EnumType t);

    /**
     * Visits an interface type.
     *
     * @param t the type to visit
     */
    public void visitInterfaceType(InterfaceType t);

    /**
     * Visits an annotation type.
     *
     * @param t the type to visit
     */
    public void visitAnnotationType(AnnotationType t);

    /**
     * Visits an array type.
     *
     * @param t the type to visit
     */
    public void visitArrayType(ArrayType t);

    /**
     * Visits a type variable.
     *
     * @param t the type to visit
     */
    public void visitTypeVariable(TypeVariable t);

    /**
     * Visits a wildcard.
     *
     * @param t the type to visit
     */
    public void visitWildcardType(WildcardType t);
}
