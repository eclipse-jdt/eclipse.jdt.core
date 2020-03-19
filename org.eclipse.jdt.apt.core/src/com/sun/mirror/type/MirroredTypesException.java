/*
 * @(#)MirroredTypesException.java	1.1 04/04/20
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.sun.mirror.declaration.Declaration;


/**
 * Thrown when an application attempts to access a sequence of {@link Class}
 * objects each corresponding to a {@link TypeMirror}.
 *
 * @see MirroredTypeException
 * @see Declaration#getAnnotation(Class)
 */
public class MirroredTypesException extends RuntimeException {

    private static final long serialVersionUID = 1;

    private transient Collection<TypeMirror> types;	// cannot be serialized
    private Collection<String> names;		// types' qualified "names"

    /**
     * Constructs a new MirroredTypesException for the specified types.
     *
     * @param types  an ordered collection of the types being accessed
     */
    public MirroredTypesException(Collection<TypeMirror> types) {
	super("Attempt to access Class objects for TypeMirrors " + types); //$NON-NLS-1$
	this.types = types;
	names = new ArrayList<String>();
	for (TypeMirror t : types) {
	    names.add(t.toString());
	}
    }

    /**
     * Returns the type mirrors corresponding to the types being accessed.
     * The type mirrors may be unavailable if this exception has been
     * serialized and then read back in.
     *
     * @return the type mirrors in order, or <code>null</code> if unavailable
     */
    public Collection<TypeMirror> getTypeMirrors() {
	return (types != null)
		? Collections.unmodifiableCollection(types)
		: null;
    }

    /**
     * Returns the fully qualified names of the types being accessed.
     * More precisely, returns the canonical names of each class,
     * interface, array, or primitive, and <code>"void"</code> for
     * the pseudo-type representing the type of <code>void</code>.
     *
     * @return the fully qualified names, in order, of the types being
     *		accessed
     */
    public Collection<String> getQualifiedNames() {
	return Collections.unmodifiableCollection(names);
    }
}
