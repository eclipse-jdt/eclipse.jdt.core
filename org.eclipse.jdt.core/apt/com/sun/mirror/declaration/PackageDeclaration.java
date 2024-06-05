/*
 * @(#)PackageDeclaration.java	1.1 04/01/26
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


/**
 * Represents the declaration of a package.  Provides access to information
 * about the package and its members.
 *
 * <p> {@link com.sun.mirror.util.DeclarationFilter}
 * provides a simple way to select just the items of interest
 * when a method returns a collection of declarations.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.1 04/01/26
 * @since 1.5
 */

public interface PackageDeclaration extends Declaration {

    /**
     * Returns the fully qualified name of this package.
     * This is also known as the package's <i>canonical</i> name.
     *
     * @return the fully qualified name of this package, or the
     * empty string if this is the unnamed package
     */
    String getQualifiedName();

    /**
     * Returns the declarations of the top-level classes in this package.
     * Interfaces are not included, but enum types are.
     *
     * @return the declarations of the top-level classes in this package
     *
     * @see com.sun.mirror.util.DeclarationFilter
     */
    Collection<ClassDeclaration> getClasses();

    /**
     * Returns the declarations of the top-level enum types in this package.
     *
     * @return the declarations of the top-level enum types in this package
     *
     * @see com.sun.mirror.util.DeclarationFilter
     */
    Collection<EnumDeclaration> getEnums();

    /**
     * Returns the declarations of the top-level interfaces in this package.
     * Annotation types are included.
     *
     * @return the declarations of the top-level interfaces in this package
     *
     * @see com.sun.mirror.util.DeclarationFilter
     */
    Collection<InterfaceDeclaration> getInterfaces();

    /**
     * Returns the declarations of the top-level annotation types in this
     * package.
     *
     * @return the declarations of the top-level annotation types in this
     * package
     *
     * @see com.sun.mirror.util.DeclarationFilter
     */
    Collection<AnnotationTypeDeclaration> getAnnotationTypes();
}
