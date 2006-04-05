/*
 * @(#)PrimitiveType.java	1.1 04/01/26
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


/**
 * Represents a primitive type.  These include
 * <tt>boolean</tt>, <tt>byte</tt>, <tt>short</tt>, <tt>int</tt>,
 * <tt>long</tt>, <tt>char</tt>, <tt>float</tt>, and <tt>double</tt>.
 * 
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.1 04/01/26
 * @since 1.5
 */

public interface PrimitiveType extends TypeMirror {

    /**
     * Returns the kind of primitive type that this object represents.
     *
     * @return the kind of primitive type that this object represents
     */
    Kind getKind();

    /**
     * An enumeration of the different kinds of primitive types.
     */
    enum Kind {
	/** The primitive type <tt>boolean</tt> */	BOOLEAN,
	/** The primitive type <tt>byte</tt> */		BYTE,
	/** The primitive type <tt>short</tt> */	SHORT,
	/** The primitive type <tt>int</tt> */		INT,
	/** The primitive type <tt>long</tt> */		LONG,
	/** The primitive type <tt>char</tt> */		CHAR,
	/** The primitive type <tt>float</tt> */	FLOAT,
	/** The primitive type <tt>double</tt> */	DOUBLE
    }
}
