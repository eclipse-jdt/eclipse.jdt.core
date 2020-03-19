/*
 * @(#)DeclarationVisitor.java	1.3 04/04/20
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

import com.sun.mirror.declaration.*;


/**
 * A visitor for declarations, in the style of the standard visitor
 * design pattern.  Classes implementing this interface are used to
 * operate on a declaration when the kind of declaration is unknown at
 * compile time.  When a visitor is passed to a declaration's {@link
 * Declaration#accept accept} method, the most specific
 * <code>visitXxx</code> method applicable to that declaration is
 * invoked.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.3 04/04/20
 * @since 1.5
 */

public interface DeclarationVisitor {

    /**
     * Visits a declaration.
     * @param d the declaration to visit
     */
    public void visitDeclaration(Declaration d);

    /**
     * Visits a package declaration.
     * @param d the declaration to visit
     */
    public void visitPackageDeclaration(PackageDeclaration d);

    /**
     * Visits a member or constructor declaration.
     * @param d the declaration to visit
     */
    public void visitMemberDeclaration(MemberDeclaration d);

    /**
     * Visits a type declaration.
     * @param d the declaration to visit
     */
    public void visitTypeDeclaration(TypeDeclaration d);

    /**
     * Visits a class declaration.
     * @param d the declaration to visit
     */
    public void visitClassDeclaration(ClassDeclaration d);

    /**
     * Visits an enum declaration.
     * @param d the declaration to visit
     */
    public void visitEnumDeclaration(EnumDeclaration d);

    /**
     * Visits an interface declaration.
     * @param d the declaration to visit
     */
    public void visitInterfaceDeclaration(InterfaceDeclaration d);

    /**
     * Visits an annotation type declaration.
     * @param d the declaration to visit
     */
    public void visitAnnotationTypeDeclaration(AnnotationTypeDeclaration d);

    /**
     * Visits a field declaration.
     * @param d the declaration to visit
     */
    public void visitFieldDeclaration(FieldDeclaration d);

    /**
     * Visits an enum constant declaration.
     * @param d the declaration to visit
     */
    public void visitEnumConstantDeclaration(EnumConstantDeclaration d);

    /**
     * Visits a method or constructor declaration.
     * @param d the declaration to visit
     */
    public void visitExecutableDeclaration(ExecutableDeclaration d);

    /**
     * Visits a constructor declaration.
     * @param d the declaration to visit
     */
    public void visitConstructorDeclaration(ConstructorDeclaration d);

    /**
     * Visits a method declaration.
     * @param d the declaration to visit
     */
    public void visitMethodDeclaration(MethodDeclaration d);

    /**
     * Visits an annotation type element declaration.
     * @param d the declaration to visit
     */
    public void visitAnnotationTypeElementDeclaration(
				     AnnotationTypeElementDeclaration d);

    /**
     * Visits a parameter declaration.
     * @param d the declaration to visit
     */
    public void visitParameterDeclaration(ParameterDeclaration d);

    /**
     * Visits a type parameter declaration.
     * @param d the declaration to visit
     */
    public void visitTypeParameterDeclaration(TypeParameterDeclaration d);
}
