/*
 * @(#)SourceOrderDeclScanner.java	1.5 04/09/16
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

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A visitor for declarations that scans declarations contained within
 * the given declaration in source code order.  For example, when
 * visiting a class, the methods, fields, constructors, and nested
 * types of the class are also visited.
 *
 * To control the processing done on a declaration, users of this
 * class pass in their own visitors for pre and post processing.  The
 * preprocessing visitor is called before the contained declarations
 * are scanned; the postprocessing visitor is called after the
 * contained declarations are scanned.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @version 1.5 04/09/16
 * @since 1.5
 */
class SourceOrderDeclScanner extends DeclarationScanner {
    static class SourceOrderComparator implements java.util.Comparator<Declaration> {
	SourceOrderComparator(){}


	static boolean equals(Declaration d1, Declaration d2) {
	    return d1 == d2 || (d1 != null && d1.equals(d2));
	}

	private static class DeclPartialOrder extends com.sun.mirror.util.SimpleDeclarationVisitor {
	    private int value = 1000;
	    private static int staticAdjust(Declaration d) {
		return d.getModifiers().contains(Modifier.STATIC)?0:1;
	    }

	    DeclPartialOrder() {}

	    public int getValue() { return value; }

	    @Override
	    public void visitTypeParameterDeclaration(TypeParameterDeclaration d) {value = 0;}

	    @Override
	    public void visitEnumConstantDeclaration(EnumConstantDeclaration d) {value = 1;}

	    @Override
	    public void visitClassDeclaration(ClassDeclaration d) {value = 2 + staticAdjust(d);}

	    @Override
	    public void visitInterfaceDeclaration(InterfaceDeclaration d) {value = 4;}

	    @Override
	    public void visitEnumDeclaration(EnumDeclaration d) {value = 6;}

	    @Override
	    public void visitAnnotationTypeDeclaration(AnnotationTypeDeclaration d) {value = 8;}

	    @Override
	    public void visitFieldDeclaration(FieldDeclaration d) {value = 10 + staticAdjust(d);}

	    @Override
	    public void visitConstructorDeclaration(ConstructorDeclaration d) {value = 12;}

	    @Override
	    public void visitMethodDeclaration(MethodDeclaration d) {value = 14 + staticAdjust(d);}
	}

	private int compareEqualPosition(Declaration d1, Declaration d2) {
	    assert d1.getPosition() == d2.getPosition();

	    DeclPartialOrder dpo1 = new DeclPartialOrder();
	    DeclPartialOrder dpo2 = new DeclPartialOrder();

	    d1.accept(dpo1);
	    d2.accept(dpo2);

	    int difference = dpo1.getValue() - dpo2.getValue();
	    if (difference != 0)
		return difference;
	    else {
		int result = d1.getSimpleName().compareTo(d2.getSimpleName());
		if (result != 0)
		    return result;
		return ( Long.signum((long)System.identityHashCode(d1) -
					  (long)System.identityHashCode(d2)));
	    }
	}

	@Override
	public int compare(Declaration d1, Declaration d2) {
	    if (equals(d1, d2))
		return 0;

	    SourcePosition p1 = d1.getPosition();
	    SourcePosition p2 = d2.getPosition();

	    if (p1 == null && p2 != null)
		return 1;
	    else if (p1 != null && p2 == null)
		return -1;
	    else if(p1 == null && p2 == null)
		return compareEqualPosition(d1, d2);
	    else {
		assert p1 != null && p2 != null;
		int fileComp = p1.file().compareTo(p2.file()) ;
		if (fileComp == 0) {
		    long diff = (long)p1.line() - (long)p2.line();
		    if (diff == 0) {
			diff = Long.signum((long)p1.column() - (long)p2.column());
			if (diff != 0)
			    return (int)diff;
			else {
			    // declarations may be two
			    // compiler-generated members with the
			    // same source position
			    return compareEqualPosition(d1, d2);
			}
		    } else
			return (diff<0)? -1:1;
		} else
		    return fileComp;
	    }
	}
    }

    final static java.util.Comparator<Declaration> comparator = new SourceOrderComparator();

    SourceOrderDeclScanner(DeclarationVisitor pre, DeclarationVisitor post) {
	super(pre, post);
    }

    /**
     * Visits a type declaration.
     *
     * @param d the declaration to visit
     */
    @Override
	public void visitTypeDeclaration(TypeDeclaration d) {
	d.accept(pre);

	SortedSet<Declaration> decls = new
	    TreeSet<>(SourceOrderDeclScanner.comparator) ;

	for(TypeParameterDeclaration tpDecl: d.getFormalTypeParameters()) {
	    decls.add(tpDecl);
	}

	for(FieldDeclaration fieldDecl: d.getFields()) {
	    decls.add(fieldDecl);
	}

	for(MethodDeclaration methodDecl: d.getMethods()) {
	    decls.add(methodDecl);
	}

	for(TypeDeclaration typeDecl: d.getNestedTypes()) {
	    decls.add(typeDecl);
	}

	for(Declaration decl: decls )
	    decl.accept(this);

	d.accept(post);
    }

    @Override
	public void visitClassDeclaration(ClassDeclaration d) {
	d.accept(pre);

	SortedSet<Declaration> decls = new
	    TreeSet<>(SourceOrderDeclScanner.comparator) ;

	for(TypeParameterDeclaration tpDecl: d.getFormalTypeParameters()) {
	    decls.add(tpDecl);
	}

	for(FieldDeclaration fieldDecl: d.getFields()) {
	    decls.add(fieldDecl);
	}

	for(MethodDeclaration methodDecl: d.getMethods()) {
	    decls.add(methodDecl);
	}

	for(TypeDeclaration typeDecl: d.getNestedTypes()) {
	    decls.add(typeDecl);
	}

	for(ConstructorDeclaration ctorDecl: d.getConstructors()) {
	    decls.add(ctorDecl);
	}

	for(Declaration decl: decls )
	    decl.accept(this);

	d.accept(post);
    }

    @Override
	public void visitExecutableDeclaration(ExecutableDeclaration d) {
	d.accept(pre);

	SortedSet<Declaration> decls = new
	    TreeSet<>(SourceOrderDeclScanner.comparator) ;

	for(TypeParameterDeclaration tpDecl: d.getFormalTypeParameters())
	    decls.add(tpDecl);

	for(ParameterDeclaration pDecl: d.getParameters())
	    decls.add(pDecl);

	for(Declaration decl: decls )
	    decl.accept(this);

	d.accept(post);
    }

}
