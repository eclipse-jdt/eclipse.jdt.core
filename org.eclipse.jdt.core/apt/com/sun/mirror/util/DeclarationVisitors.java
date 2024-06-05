/*
 * @(#)DeclarationVisitors.java	1.4 04/07/13
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

package com.sun.mirror.util;

/**
 * Utilities to create specialized <code>DeclarationVisitor</code> instances.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.4 04/07/13
 * @since 1.5
 */
public class DeclarationVisitors {
    private DeclarationVisitors(){} // do not instantiate.

    /**
     * A visitor that has no side effects and keeps no state.
     */
    public static final DeclarationVisitor NO_OP = new SimpleDeclarationVisitor();

    /**
     * Return a <code>DeclarationVisitor</code> that will scan the
     * declaration structure, visiting declarations contained in
     * another declaration.  For example, when visiting a class, the
     * fields, methods, constructors, etc. of the class are also
     * visited.  The order in which the contained declarations are scanned is
     * not specified.
     *
     * <p>The <code>pre</code> and <code>post</code>
     * <code>DeclarationVisitor</code> parameters specify,
     * respectively, the processing the scanner will do before or
     * after visiting the contained declarations.  If only one of pre
     * and post processing is needed, use {@link
     * DeclarationVisitors#NO_OP DeclarationVisitors.NO_OP} for the
     * other parameter.
     *
     * @param pre visitor representing processing to do before
     * visiting contained declarations.
     *
     * @param post visitor representing processing to do after
     * visiting contained declarations.
     */
    public static DeclarationVisitor getDeclarationScanner(DeclarationVisitor pre,
							   DeclarationVisitor post) {
	return new DeclarationScanner(pre, post);
    }

    /**
     * Return a <code>DeclarationVisitor</code> that will scan the
     * declaration structure, visiting declarations contained in
     * another declaration in source code order.  For example, when
     * visiting a class, the fields, methods, constructors, etc. of
     * the class are also visited.  The order in which the contained
     * declarations are visited is as close to source code order as
     * possible; declaration mirrors created from class files instead
     * of source code will not have source position information.
     *
     * <p>The <code>pre</code> and <code>post</code>
     * <code>DeclarationVisitor</code> parameters specify,
     * respectively, the processing the scanner will do before or
     * after visiting the contained declarations.  If only one of pre
     * and post processing is needed, use {@link
     * DeclarationVisitors#NO_OP DeclarationVisitors.NO_OP} for the other parameter.
     *
     * @param pre visitor representing processing to do before
     * visiting contained declarations.
     *
     * @param post visitor representing processing to do after
     * visiting contained declarations.
     */
    public static DeclarationVisitor getSourceOrderDeclarationScanner(DeclarationVisitor pre,
								      DeclarationVisitor post) {
	return new SourceOrderDeclScanner(pre, post);
    }
}
