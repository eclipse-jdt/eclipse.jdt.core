/*
 * @(#)SimpleDeclarationVisitor.java	1.3 04/04/30
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
 * A simple visitor for declarations.
 *
 * <p> The implementations of the methods of this class do nothing but
 * delegate up the declaration hierarchy.  A subclass should override the
 * methods that correspond to the kinds of declarations on which it
 * will operate.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.3 04/04/30
 * @since 1.5
 */

public class SimpleDeclarationVisitor implements DeclarationVisitor {

    /**
     * Creates a new <code>SimpleDeclarationVisitor</code>.
     */
    public SimpleDeclarationVisitor(){}

    /**
     * Visits a declaration.
     * The implementation does nothing.
     * @param d the declaration to visit
     */
    @Override
	public void visitDeclaration(Declaration d) {
    }

    /**
     * Visits a package declaration.
     * The implementation simply invokes
     * {@link #visitDeclaration visitDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitPackageDeclaration(PackageDeclaration d) {
	visitDeclaration(d);
    }

    /**
     * Visits a member or constructor declaration.
     * The implementation simply invokes
     * {@link #visitDeclaration visitDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitMemberDeclaration(MemberDeclaration d) {
	visitDeclaration(d);
    }

    /**
     * Visits a type declaration.
     * The implementation simply invokes
     * {@link #visitMemberDeclaration visitMemberDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitTypeDeclaration(TypeDeclaration d) {
	visitMemberDeclaration(d);
    }

    /**
     * Visits a class declaration.
     * The implementation simply invokes
     * {@link #visitTypeDeclaration visitTypeDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitClassDeclaration(ClassDeclaration d) {
	visitTypeDeclaration(d);
    }

    /**
     * Visits an enum declaration.
     * The implementation simply invokes
     * {@link #visitClassDeclaration visitClassDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitEnumDeclaration(EnumDeclaration d) {
	visitClassDeclaration(d);
    }

    /**
     * Visits an interface declaration.
     * The implementation simply invokes
     * {@link #visitTypeDeclaration visitTypeDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitInterfaceDeclaration(InterfaceDeclaration d) {
	visitTypeDeclaration(d);
    }

    /**
     * Visits an annotation type declaration.
     * The implementation simply invokes
     * {@link #visitInterfaceDeclaration visitInterfaceDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitAnnotationTypeDeclaration(AnnotationTypeDeclaration d) {
	visitInterfaceDeclaration(d);
    }

    /**
     * Visits a field declaration.
     * The implementation simply invokes
     * {@link #visitMemberDeclaration visitMemberDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitFieldDeclaration(FieldDeclaration d) {
	visitMemberDeclaration(d);
    }

    /**
     * Visits an enum constant declaration.
     * The implementation simply invokes
     * {@link #visitFieldDeclaration visitFieldDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitEnumConstantDeclaration(EnumConstantDeclaration d) {
	visitFieldDeclaration(d);
    }

    /**
     * Visits a method or constructor declaration.
     * The implementation simply invokes
     * {@link #visitMemberDeclaration visitMemberDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitExecutableDeclaration(ExecutableDeclaration d) {
	visitMemberDeclaration(d);
    }

    /**
     * Visits a constructor declaration.
     * The implementation simply invokes
     * {@link #visitExecutableDeclaration visitExecutableDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitConstructorDeclaration(ConstructorDeclaration d) {
	visitExecutableDeclaration(d);
    }

    /**
     * Visits a method declaration.
     * The implementation simply invokes
     * {@link #visitExecutableDeclaration visitExecutableDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitMethodDeclaration(MethodDeclaration d) {
	visitExecutableDeclaration(d);
    }

    /**
     * Visits an annotation type element declaration.
     * The implementation simply invokes
     * {@link #visitMethodDeclaration visitMethodDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitAnnotationTypeElementDeclaration(
	    AnnotationTypeElementDeclaration d) {
	visitMethodDeclaration(d);
    }

    /**
     * Visits a parameter declaration.
     * The implementation simply invokes
     * {@link #visitDeclaration visitDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitParameterDeclaration(ParameterDeclaration d) {
	visitDeclaration(d);
    }

    /**
     * Visits a type parameter declaration.
     * The implementation simply invokes
     * {@link #visitDeclaration visitDeclaration}.
     * @param d the declaration to visit
     */
    @Override
	public void visitTypeParameterDeclaration(TypeParameterDeclaration d) {
	visitDeclaration(d);
    }
}
