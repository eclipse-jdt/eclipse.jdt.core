/*
 * @(#)FieldDeclaration.java	1.2 04/04/20
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

package com.sun.mirror.declaration;


import com.sun.mirror.type.TypeMirror;


/**
 * Represents a field of a type declaration.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.2 04/04/20
 * @since 1.5
 */

public interface FieldDeclaration extends MemberDeclaration {

    /**
     * Returns the type of this field.
     *
     * @return the type of this field
     */
    TypeMirror getType();

    /**
     * Returns the value of this field if this field is a compile-time
     * constant.  Returns <code>null</code> otherwise.
     * The value will be of a primitive type or <code>String</code>.
     * If the value is of a primitive type, it is wrapped in the
     * appropriate wrapper class (such as {@link Integer}).
     *
     * @return the value of this field if this field is a compile-time
     * constant, or <code>null</code> otherwise
     */
    Object getConstantValue();

    /**
     * Returns the text of a <i>constant expression</i> representing the
     * value of this field if this field is a compile-time constant.
     * Returns <code>null</code> otherwise.
     * The value will be of a primitive type or <code>String</code>.
     * The text returned is in a form suitable for representing the value
     * in source code.
     *
     * @return the text of a constant expression if this field is a
     *		compile-time constant, or <code>null</code> otherwise
     */
    String getConstantExpression();
}
