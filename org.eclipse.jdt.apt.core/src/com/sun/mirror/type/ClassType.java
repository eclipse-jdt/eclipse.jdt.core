/*
 * @(#)ClassType.java	1.2 04/04/30
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


import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;


/**
 * Represents a class type.
 * Interface types are represented separately by {@link InterfaceType}.
 * Note that an {@linkplain EnumType enum} is a kind of class.
 *
 * <p> While a {@link ClassDeclaration} represents the <i>declaration</i>
 * of a class, a <code>ClassType</code> represents a class <i>type</i>.
 * See {@link TypeDeclaration} for more on this distinction.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.2 04/04/30
 * @since 1.5
 */

public interface ClassType extends DeclaredType {

    /**
     * {@inheritDoc}
     */
    @Override
	ClassDeclaration getDeclaration();

    /**
     * Returns the class type that is a direct supertype of this one.
     * This is the superclass of this type's declaring class, with any
     * type arguments substituted in.
     * The only class with no superclass is <code>java.lang.Object</code>,
     * for which this method returns <code>null</code>.
     *
     * <p> For example, the class type extended by
     * {@code java.util.TreeSet<String>} is
     * {@code java.util.AbstractSet<String>}.
     *
     * @return the class type that is a direct supertype of this one,
     * or <code>null</code> if there is none
     */
    ClassType getSuperclass();
}
