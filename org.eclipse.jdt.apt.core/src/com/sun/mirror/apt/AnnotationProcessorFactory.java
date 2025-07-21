/*
 * @(#)AnnotationProcessorFactory.java	1.9 04/07/13
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


import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import java.util.Collection;
import java.util.Set;


/**
 * A factory for creating annotation processors.
 * Each factory is responsible for creating processors for one or more
 * annotation types.
 * The factory is said to <i>support</i> these types.
 *
 * <p> Each implementation of an <code>AnnotationProcessorFactory</code>
 * must provide a public no-argument constructor to be used by tools to
 * instantiate the factory.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.9 04/07/13
 * @since 1.5
 */

public interface AnnotationProcessorFactory {

    /**
     * Returns the options recognized by this factory or by any of the
     * processors it may create.
     * Only {@linkplain AnnotationProcessorEnvironment#getOptions()
     * processor-specific} options are included, each of which begins
     * with <code>"-A"</code>.  For example, if this factory recognizes
     * options such as <code>-Adebug -Aloglevel=3</code>, it will
     * return the strings <code>"-Adebug"</code> and <code>"-Aloglevel"</code>.
     *
     * <p> A tool might use this information to determine if any
     * options provided by a user are unrecognized by any processor,
     * in which case it may wish to report an error.
     *
     * @return the options recognized by this factory or by any of the
     * processors it may create, or an empty collection if none
     */
    Collection<String> supportedOptions();

    /**
     * Returns the names of the annotation types supported by this factory.
     * An element of the result may be the canonical (fully qualified) name
     * of a supported annotation type.  Alternately it may be of the form
     * <code>"<i>name</i>.*"</code>
     * representing the set of all annotation types
     * with canonical names beginning with <code>"<i>name</i>."</code>
     * Finally, <code>"*"</code> by itself represents the set of all
     * annotation types.
     *
     * @return the names of the annotation types supported by this factory
     */
    Collection<String> supportedAnnotationTypes();

    /**
     * Returns an annotation processor for a set of annotation
     * types. The set will be empty if the factory supports
     * &quot;<code>*</code>&quot; and the specified type declarations have
     * no annotations.  Note that the set of annotation types may be
     * empty for other reasons, such as giving the factory an
     * opportunity to register a listener.  An
     * <code>AnnotationProcessorFactory</code> must gracefully handle an
     * empty set of annotations; an appropriate response to an empty
     * set will often be returning {@link AnnotationProcessors#NO_OP}.
     *
     * @param atds type declarations of the annotation types to be processed
     * @param env  environment to use during processing
     * @return an annotation processor for the given annotation types,
     *		or <code>null</code> if the types are not supported or the
     *		processor cannot be created
     */
    AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
					AnnotationProcessorEnvironment env);
}
