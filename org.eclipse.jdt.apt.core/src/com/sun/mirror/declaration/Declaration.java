/*
 * @(#)Declaration.java	1.6 04/07/16
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


import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.MirroredTypesException;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.Declarations;
import com.sun.mirror.util.SourcePosition;
import java.lang.annotation.Annotation;
import java.util.Collection;


/**
 * Represents the declaration of a program element such as a package,
 * class, or method.  Each declaration represents a static, language-level
 * construct (and not, for example, a runtime construct of the virtual
 * machine), and typically corresponds one-to-one with a particular
 * fragment of source code.
 *
 * <p> Declarations should be compared using the {@link #equals(Object)}
 * method.  There is no guarantee that any particular declaration will
 * always be represented by the same object.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.6 04/07/16
 *
 * @see Declarations
 * @see TypeMirror
 * @since 1.5
 */

public interface Declaration {

    /**
     * Tests whether an object represents the same declaration as this.
     *
     * @param obj  the object to be compared with this declaration
     * @return <code>true</code> if the specified object represents the same
     *		declaration as this
     */
    @Override
	boolean equals(Object obj);

    /**
     * Returns the text of the documentation ("javadoc") comment of
     * this declaration.
     *
     * @return the documentation comment of this declaration, or <code>null</code>
     *		if there is none
     */
    String getDocComment();

    /**
     * Returns the annotations that are directly present on this declaration.
     *
     * @return the annotations directly present on this declaration;
     *		an empty collection if there are none
     */
    Collection<AnnotationMirror> getAnnotationMirrors();

    /**
     * Returns the annotation of this declaration having the specified
     * type.  The annotation may be either inherited or directly
     * present on this declaration.
     *
     * <p> The annotation returned by this method could contain an element
     * whose value is of type <code>Class</code>.
     * This value cannot be returned directly:  information necessary to
     * locate and load a class (such as the class loader to use) is
     * not available, and the class might not be loadable at all.
     * Attempting to read a <code>Class</code> object by invoking the relevant
     * method on the returned annotation
     * will result in a {@link MirroredTypeException},
     * from which the corresponding {@link TypeMirror} may be extracted.
     * Similarly, attempting to read a <code>Class[]</code>-valued element
     * will result in a {@link MirroredTypesException}.
     *
     * <blockquote>
     * <i>Note:</i> This method is unlike
     * others in this and related interfaces.  It operates on run-time
     * reflective information -- representations of annotation types
     * currently loaded into the VM -- rather than on the mirrored
     * representations defined by and used throughout these
     * interfaces.  It is intended for callers that are written to
     * operate on a known, fixed set of annotation types.
     * </blockquote>
     *
     * @param <A>  the annotation type
     * @param annotationType  the <code>Class</code> object corresponding to
     *		the annotation type
     * @return the annotation of this declaration having the specified type
     *
     * @see #getAnnotationMirrors()
     */
    <A extends Annotation> A getAnnotation(Class<A> annotationType);

    /**
     * Returns the modifiers of this declaration, excluding annotations.
     * Implicit modifiers, such as the <code>public</code> and <code>static</code>
     * modifiers of interface members, are included.
     *
     * @return the modifiers of this declaration in undefined order;
     *		an empty collection if there are none
     */
    Collection<Modifier> getModifiers();

    /**
     * Returns the simple (unqualified) name of this declaration.
     * The name of a generic type does not include any reference
     * to its formal type parameters.
     * For example, the simple name of the interface declaration
     * {@code java.util.Set<E>} is <code>"Set"</code>.
     * If this declaration represents the empty package, an empty
     * string is returned.
     * If it represents a constructor, the simple name of its
     * declaring class is returned.
     *
     * @return the simple name of this declaration
     */
    String getSimpleName();

    /**
     * Returns the source position of the beginning of this declaration.
     * Returns <code>null</code> if the position is unknown or not applicable.
     *
     * <p> This source position is intended for use in providing
     * diagnostics, and indicates only approximately where a declaration
     * begins.
     *
     * @return the source position of the beginning of this declaration,
     *		or null if the position is unknown or not applicable
     */
    SourcePosition getPosition();

    /**
     * Applies a visitor to this declaration.
     *
     * @param v the visitor operating on this declaration
     */
    void accept(DeclarationVisitor v);
}
