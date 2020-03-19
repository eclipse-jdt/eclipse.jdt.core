/*
 * @(#)AnnotationMirror.java	1.5 04/07/16
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

import java.util.Map;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.util.SourcePosition;


/**
 * Represents an annotation.  An annotation associates a value with
 * each element of an annotation type.
 *
 * <p> Annotations should not be compared using reference-equality
 * ("<code>==</code>").  There is no guarantee that any particular
 * annotation will always be represented by the same object.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.5 04/07/16
 * @since 1.5
 */

public interface AnnotationMirror {

    /**
     * Returns the annotation type of this annotation.
     *
     * @return the annotation type of this annotation
     */
    AnnotationType getAnnotationType();

    /**
     * Returns the source position of the beginning of this annotation.
     * Returns null if the position is unknown or not applicable.
     *
     * <p>This source position is intended for use in providing diagnostics,
     * and indicates only approximately where an annotation begins.
     *
     * @return  the source position of the beginning of this annotation or
     * null if the position is unknown or not applicable
     */
    SourcePosition getPosition();

    /**
     * Returns this annotation's elements and their values.
     * This is returned in the form of a map that associates elements
     * with their corresponding values.
     * Only those elements and values explicitly present in the
     * annotation are included, not those that are implicitly assuming
     * their default values.
     * The order of the map matches the order in which the
     * elements appear in the annotation's source.
     *
     * @return this annotation's elements and their values,
     * or an empty map if there are none
     */
    Map<AnnotationTypeElementDeclaration, AnnotationValue> getElementValues();
}
