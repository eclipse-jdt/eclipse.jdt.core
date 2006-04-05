/*
 * @(#)DeclarationScanner.java	1.5 04/04/20
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
 * A visitor for declarations that scans declarations contained within
 * the given declaration.  For example, when visiting a class, the
 * methods, fields, constructors, and nested types of the class are
 * also visited.
 *
 * <p> To control the processing done on a declaration, users of this
 * class pass in their own visitors for pre and post processing.  The
 * preprocessing visitor is called before the contained declarations
 * are scanned; the postprocessing visitor is called after the
 * contained declarations are scanned.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.5 04/04/20
 * @since 1.5
 */

class DeclarationScanner implements DeclarationVisitor {
    protected DeclarationVisitor pre;
    protected DeclarationVisitor post;

    DeclarationScanner(DeclarationVisitor pre, DeclarationVisitor post) {
	this.pre = pre;
	this.post = post;
    }

    /**
     * Visits a declaration.
     *
     * @param d the declaration to visit
     */
    public void visitDeclaration(Declaration d) {
	d.accept(pre);
	d.accept(post);
    }

    /**
     * Visits a package declaration.
     *
     * @param d the declaration to visit
     */
    public void visitPackageDeclaration(PackageDeclaration d) {
	d.accept(pre);

	for(ClassDeclaration classDecl: d.getClasses()) {
	    classDecl.accept(this);
	}

	for(InterfaceDeclaration interfaceDecl: d.getInterfaces()) {
	    interfaceDecl.accept(this);
	}

	d.accept(post);
    }

    /**
     * Visits a member or constructor declaration.
     *
     * @param d the declaration to visit
     */
    public void visitMemberDeclaration(MemberDeclaration d) {
	visitDeclaration(d);
    }

    /**
     * Visits a type declaration.
     *
     * @param d the declaration to visit
     */
    public void visitTypeDeclaration(TypeDeclaration d) {
	d.accept(pre);

	for(TypeParameterDeclaration tpDecl: d.getFormalTypeParameters()) {
	    tpDecl.accept(this);
	}
	
	for(FieldDeclaration fieldDecl: d.getFields()) {
	    fieldDecl.accept(this);
	}
	
	for(MethodDeclaration methodDecl: d.getMethods()) {
	    methodDecl.accept(this);
	}
	
	for(TypeDeclaration typeDecl: d.getNestedTypes()) {
	    typeDecl.accept(this);
	}

	d.accept(post);
    }

    /**
     * Visits a class declaration.
     *
     * @param d the declaration to visit
     */
    public void visitClassDeclaration(ClassDeclaration d) {
	d.accept(pre);

	for(TypeParameterDeclaration tpDecl: d.getFormalTypeParameters()) {
	    tpDecl.accept(this);
	}
	
	for(FieldDeclaration fieldDecl: d.getFields()) {
	    fieldDecl.accept(this);
	}
	
	for(MethodDeclaration methodDecl: d.getMethods()) {
	    methodDecl.accept(this);
	}
	
	for(TypeDeclaration typeDecl: d.getNestedTypes()) {
	    typeDecl.accept(this);
	}

	for(ConstructorDeclaration ctorDecl: d.getConstructors()) {
	    ctorDecl.accept(this);
	}

	d.accept(post);
    }

    /**
     * Visits an enum declaration.
     *
     * @param d the declaration to visit
     */
    public void visitEnumDeclaration(EnumDeclaration d) {
	visitClassDeclaration(d);
    }

    /**
     * Visits an interface declaration.
     *
     * @param d the declaration to visit
     */
    public void visitInterfaceDeclaration(InterfaceDeclaration d) {
	visitTypeDeclaration(d);
    }

    /**
     * Visits an annotation type declaration.
     *
     * @param d the declaration to visit
     */
    public void visitAnnotationTypeDeclaration(AnnotationTypeDeclaration d) {
	visitInterfaceDeclaration(d);
    }

    /**
     * Visits a field declaration.
     *
     * @param d the declaration to visit
     */
    public void visitFieldDeclaration(FieldDeclaration d) {
	visitMemberDeclaration(d);
    }

    /**
     * Visits an enum constant declaration.
     *
     * @param d the declaration to visit
     */
    public void visitEnumConstantDeclaration(EnumConstantDeclaration d) {
	visitFieldDeclaration(d);
    }

    /**
     * Visits a method or constructor declaration.
     *
     * @param d the declaration to visit
     */
    public void visitExecutableDeclaration(ExecutableDeclaration d) {
	d.accept(pre);

	for(TypeParameterDeclaration tpDecl: d.getFormalTypeParameters()) {
	    tpDecl.accept(this);
	}

	for(ParameterDeclaration pDecl: d.getParameters()) {
	    pDecl.accept(this);
	}

	d.accept(post);
    }

    /**
     * Visits a constructor declaration.
     *
     * @param d the declaration to visit
     */
    public void visitConstructorDeclaration(ConstructorDeclaration d) {
	visitExecutableDeclaration(d);
    }

    /**
     * Visits a method declaration.
     *
     * @param d the declaration to visit
     */
    public void visitMethodDeclaration(MethodDeclaration d) {
	visitExecutableDeclaration(d);
    }

    /**
     * Visits an annotation type element declaration.
     *
     * @param d the declaration to visit
     */
    public void visitAnnotationTypeElementDeclaration(
	    AnnotationTypeElementDeclaration d) {
	visitMethodDeclaration(d);
    }

    /**
     * Visits a parameter declaration.
     *
     * @param d the declaration to visit
     */
    public void visitParameterDeclaration(ParameterDeclaration d) {
	visitDeclaration(d);
    }

    /**
     * Visits a type parameter declaration.
     *
     * @param d the declaration to visit
     */
    public void visitTypeParameterDeclaration(TypeParameterDeclaration d) {
	visitDeclaration(d);
    }
}
