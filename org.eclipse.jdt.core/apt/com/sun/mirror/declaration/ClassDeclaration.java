/*
 * @(#)ClassDeclaration.java	1.3 04/02/20
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


import java.util.Collection;

import com.sun.mirror.type.ClassType;


/**
 * Represents the declaration of a class.
 * For the declaration of an interface, see {@link InterfaceDeclaration}.
 * Provides access to information about the class, its members, and
 * its constructors.
 * Note that an {@linkplain EnumDeclaration enum} is a kind of class.
 *
 * <p> While a <code>ClassDeclaration</code> represents the <i>declaration</i>
 * of a class, a {@link ClassType} represents a class <i>type</i>.
 * See {@link TypeDeclaration} for more on this distinction.
 *
 * <p> {@link com.sun.mirror.util.DeclarationFilter}
 * provides a simple way to select just the items of interest
 * when a method returns a collection of declarations.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.3 04/02/20
 *
 * @see ClassType
 * @since 1.5
 */

public interface ClassDeclaration extends TypeDeclaration {

    /**
     * Returns the class type directly extended by this class.
     * The only class with no superclass is <code>java.lang.Object</code>,
     * for which this method returns null.
     *
     * @return the class type directly extended by this class, or null
     * if there is none
     */
    ClassType getSuperclass();

    /**
     * Returns the constructors of this class.
     * This includes the default constructor if this class has
     * no constructors explicitly declared.
     *
     * @return the constructors of this class
     *
     * @see com.sun.mirror.util.DeclarationFilter
     */
    Collection<ConstructorDeclaration> getConstructors();

    /**
     * {@inheritDoc}
     */
    @Override
	Collection<MethodDeclaration> getMethods();
}
