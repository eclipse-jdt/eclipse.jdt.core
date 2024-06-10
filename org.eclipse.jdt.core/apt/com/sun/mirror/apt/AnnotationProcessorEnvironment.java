/*
 * @(#)AnnotationProcessorEnvironment.java	1.7 04/07/19
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

package com.sun.mirror.apt;


import java.util.Collection;
import java.util.Map;

import com.sun.mirror.declaration.*;
import com.sun.mirror.util.*;


/**
 * The environment encapsulating the state needed by an annotation processor.
 * An annotation processing tool makes this environment available
 * to all annotation processors.
 *
 * <p> When an annotation processing tool is invoked, it is given a
 * set of type declarations on which to operate.  These
 * are referred to as the <i>specified</i> types.
 * The type declarations said to be <i>included</i> in this invocation
 * consist of the specified types and any types nested within them.
 *
 * <p> {@link DeclarationFilter}
 * provides a simple way to select just the items of interest
 * when a method returns a collection of declarations.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.7 04/07/19
 * @since 1.5
 */

public interface AnnotationProcessorEnvironment {

    /**
     * Returns the options passed to the annotation processing tool.
     * Options are returned in the form of a map from option name
     * (such as <code>"-encoding"</code>) to option value.
     * For an option with no value (such as <code>"-help"</code>), the
     * corresponding value in the map is <code>null</code>.
     *
     * <p> Options beginning with <code>"-A"</code> are <i>processor-specific.</i>
     * Such options are unrecognized by the tool, but intended to be used by
     * some annotation processor.
     *
     * @return the options passed to the tool
     */
    Map<String,String> getOptions();

    /**
     * Returns the messager used to report errors, warnings, and other
     * notices.
     *
     * @return the messager
     */
    Messager getMessager();

    /**
     * Returns the filer used to create new source, class, or auxiliary
     * files.
     *
     * @return the filer
     */
    Filer getFiler();



    /**
     * Returns the declarations of the types specified when the
     * annotation processing tool was invoked.
     *
     * @return the types specified when the tool was invoked, or an
     * empty collection if there were none
     */
    Collection<TypeDeclaration> getSpecifiedTypeDeclarations();

    /**
     * Returns the declaration of a package given its fully qualified name.
     *
     * @param name  fully qualified package name, or "" for the unnamed package
     * @return the declaration of the named package, or null if it cannot
     * be found
     */
    PackageDeclaration getPackage(String name);

    /**
     * Returns the declaration of a type given its fully qualified name.
     *
     * @param name  fully qualified type name
     * @return the declaration of the named type, or null if it cannot be
     * found
     */
    TypeDeclaration getTypeDeclaration(String name);

    /**
     * A convenience method that returns the declarations of the types
     * {@linkplain AnnotationProcessorEnvironment <i>included</i>}
     * in this invocation of the annotation processing tool.
     *
     * @return the declarations of the types included in this invocation
     * of the tool, or an empty collection if there are none
     */
    Collection<TypeDeclaration> getTypeDeclarations();

    /**
     * Returns the declarations annotated with the given annotation type.
     * Only declarations of the types
     * {@linkplain AnnotationProcessorEnvironment <i>included</i>}
     * in this invocation of the annotation processing tool, or
     * declarations of members, parameters, or type parameters
     * declared within those, are returned.
     *
     * @param a  annotation type being requested
     * @return the declarations annotated with the given annotation type,
     * or an empty collection if there are none
     */
    Collection<Declaration> getDeclarationsAnnotatedWith(
						AnnotationTypeDeclaration a);

    /**
     * Returns an implementation of some utility methods for
     * operating on declarations.
     *
     * @return declaration utilities
     */
    Declarations getDeclarationUtils();

    /**
     * Returns an implementation of some utility methods for
     * operating on types.
     *
     * @return type utilities
     */
    Types getTypeUtils();

    /**
     * Add a listener.  If the listener is currently registered to listen,
     * adding it again will have no effect.
     *
     * @param listener The listener to add.
     * @throws NullPointerException if the listener is null
     */
    void addListener(AnnotationProcessorListener listener);


    /**
     * Remove a listener.  If the listener is not currently listening,
     * the method call does nothing.
     *
     * @param listener The listener to remove.
     * @throws NullPointerException if the listener is null
     */
    void removeListener(AnnotationProcessorListener listener);
}
