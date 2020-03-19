/*
 * @(#)AnnotationValue.java	1.6 04/07/19
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

import com.sun.mirror.util.SourcePosition;

/**
 * Represents a value of an annotation type element.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.6 04/07/19
 * @since 1.5
 */

public interface AnnotationValue {

    /**
     * Returns the value.
     * The result has one of the following types:
     * <ul><li> a wrapper class (such as {@link Integer}) for a primitive type
     *     <li> {@code String}
     *     <li> {@code TypeMirror}
     *     <li> {@code EnumConstantDeclaration}
     *     <li> {@code AnnotationMirror}
     *     <li> {@code Collection<AnnotationValue>}
     *		(representing the elements, in order, if the value is an array)
     * </ul>
     *
     * @return the value
     */
    Object getValue();

    /**
     * Returns the source position of the beginning of this annotation value.
     * Returns null if the position is unknown or not applicable.
     *
     * <p>This source position is intended for use in providing diagnostics,
     * and indicates only approximately where an annotation value begins.
     *
     * @return  the source position of the beginning of this annotation value or
     * null if the position is unknown or not applicable
     */
    SourcePosition getPosition();

    /**
     * Returns a string representation of this value.
     * This is returned in a form suitable for representing this value
     * in the source code of an annotation.
     *
     * @return a string representation of this value
     */
    @Override
	String toString();
}
