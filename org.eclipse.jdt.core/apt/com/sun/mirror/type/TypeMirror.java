/*
 * @(#)TypeMirror.java	1.3 04/07/16
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


import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.util.Types;
import com.sun.mirror.util.TypeVisitor;


/**
 * Represents a type in the Java programming language.
 * Types include primitive types, class and interface types, array
 * types, and type variables.  Wildcard type arguments, and the
 * pseudo-type representing the type of <code>void</code>, are represented
 * by type mirrors as well.
 *
 * <p> Types may be compared using the utility methods in
 * {@link Types}.
 * There is no guarantee that any particular type will
 * always be represented by the same object.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.3 04/07/16
 *
 * @see Declaration
 * @see Types
 * @since 1.5
 */

public interface TypeMirror {

    /**
     * Returns a string representation of this type.
     * Any names embedded in the expression are qualified.
     *
     * @return a string representation of this type
     */
    @Override
	String toString();

    /**
     * Tests whether two types represent the same type.
     *
     * @param obj the object to be compared with this type
     * @return <code>true</code> if the specified object represents the same
     *		type as this.
     */
    @Override
	boolean equals(Object obj);

    /**
     * Applies a visitor to this type.
     *
     * @param v the visitor operating on this type
     */
    void accept(TypeVisitor v);
}
