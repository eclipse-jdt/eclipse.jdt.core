/*
 * @(#)SimpleTypeVisitor.java	1.4 04/06/07
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
 * A simple visitor for types.
 *
 * <p> The implementations of the methods of this class do nothing but
 * delegate up the type hierarchy.  A subclass should override the
 * methods that correspond to the kinds of types on which it will
 * operate.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.4 04/06/07
 * @since 1.5
 */

public class SimpleTypeVisitor implements TypeVisitor {

    /**
     * Creates a new <code>SimpleTypeVisitor</code>.
     */
    public SimpleTypeVisitor() {}

    /**
     * Visits a type mirror.
     * The implementation does nothing.
     * @param t the type to visit
     */
    @Override
	public void visitTypeMirror(TypeMirror t) {
    }

    /**
     * Visits a primitive type.
     * The implementation simply invokes
     * {@link #visitTypeMirror visitTypeMirror}.
     * @param t the type to visit
     */
    @Override
	public void visitPrimitiveType(PrimitiveType t) {
	visitTypeMirror(t);
    }

    /**
     * Visits a void type.
     * The implementation simply invokes
     * {@link #visitTypeMirror visitTypeMirror}.
     * @param t the type to visit
     */
    @Override
	public void visitVoidType(VoidType t) {
	visitTypeMirror(t);
    }

    /**
     * Visits a reference type.
     * The implementation simply invokes
     * {@link #visitTypeMirror visitTypeMirror}.
     * @param t the type to visit
     */
    @Override
	public void visitReferenceType(ReferenceType t) {
	visitTypeMirror(t);
    }

    /**
     * Visits a declared type.
     * The implementation simply invokes
     * {@link #visitReferenceType visitReferenceType}.
     * @param t the type to visit
     */
    @Override
	public void visitDeclaredType(DeclaredType t) {
	visitReferenceType(t);
    }

    /**
     * Visits a class type.
     * The implementation simply invokes
     * {@link #visitDeclaredType visitDeclaredType}.
     * @param t the type to visit
     */
    @Override
	public void visitClassType(ClassType t) {
	visitDeclaredType(t);
    }

    /**
     * Visits an enum type.
     * The implementation simply invokes
     * {@link #visitClassType visitClassType}.
     * @param t the type to visit
     */
    @Override
	public void visitEnumType(EnumType t) {
	visitClassType(t);
    }

    /**
     * Visits an interface type.
     * The implementation simply invokes
     * {@link #visitDeclaredType visitDeclaredType}.
     * @param t the type to visit
     */
    @Override
	public void visitInterfaceType(InterfaceType t) {
	visitDeclaredType(t);
    }

    /**
     * Visits an annotation type.
     * The implementation simply invokes
     * {@link #visitInterfaceType visitInterfaceType}.
     * @param t the type to visit
     */
    @Override
	public void visitAnnotationType(AnnotationType t) {
	visitInterfaceType(t);
    }

    /**
     * Visits an array type.
     * The implementation simply invokes
     * {@link #visitReferenceType visitReferenceType}.
     * @param t the type to visit
     */
    @Override
	public void visitArrayType(ArrayType t) {
	visitReferenceType(t);
    }

    /**
     * Visits a type variable.
     * The implementation simply invokes
     * {@link #visitReferenceType visitReferenceType}.
     * @param t the type to visit
     */
    @Override
	public void visitTypeVariable(TypeVariable t) {
	visitReferenceType(t);
    }

    /**
     * Visits a wildcard.
     * The implementation simply invokes
     * {@link #visitTypeMirror visitTypeMirror}.
     * @param t the type to visit
     */
    @Override
	public void visitWildcardType(WildcardType t) {
	visitTypeMirror(t);
    }
}
