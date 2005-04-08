/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.declaration;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;
import com.sun.mirror.util.TypeVisitor;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeParameter;

public class TypeParameterDeclarationImpl extends DeclarationImpl implements TypeParameterDeclaration, TypeVariable
{
    public TypeParameterDeclarationImpl(final ITypeBinding binding,
                                        final ProcessorEnvImpl env)
    {
		super(binding, env);
		assert binding.isTypeVariable();
    }

    public void accept(DeclarationVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitTypeParameterDeclaration(this);
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass)
    {
        return null;
    }

    public Collection<AnnotationMirror> getAnnotationMirrors()
    {
        return Collections.emptyList();
    }

    public Collection<ReferenceType> getBounds()
    {
        final ITypeBinding[] bounds = getDeclarationBinding().getTypeBounds();
        if( bounds == null || bounds.length == 0 )
            return Collections.emptyList();

        final Collection<ReferenceType> result = new ArrayList<ReferenceType>(4);
        for( ITypeBinding bound : bounds ){
            final ReferenceType type = (ReferenceType)Factory.createReferenceType(bound, _env);
             if( type != null )
                result.add(type);
        }

        return result;
    }

    public String getDocComment()
    {
        return null;
    }

    public Collection<Modifier> getModifiers()
    {
        return Collections.emptyList();
    }

    public Declaration getOwner()
    {
        // TODO: (theodora) uncomment the following code when we get the next version of jdt.core
	/*
		final ITypeBinding binding = getDeclarationBinding();
		// declared on a class
		IBinding owner = binding.getDeclaringClass();
		if( owner == null )
			// declared on the method
			owner = binding.getDeclaringMethod();
		// actually don't know for some reason.
		if( owner == null ) return null;
		return Factory.createDeclaration(owner, _env);
		
	*/	
        throw new UnsupportedOperationException("Need version 1.44 of jdt.core.dom.ITypeBinding.java. tyeung@bea.com");
    }

    public SourcePosition getPosition()
    {
        if( isFromSource() )
        {
			final ASTNode node = getAstNode();  
			if( node == null ) return null;
            final CompilationUnit unit = getCompilationUnit();
            final int offset = node.getStartPosition();
            return new SourcePositionImpl(offset, node.getLength(), unit.lineNumber(offset), this);
        }
        else
            return null;
    }

    public String getSimpleName()
    {
        final ITypeBinding typeVar = getDeclarationBinding();
        return typeVar.getName();
    }

    // Start of implementation of TypeVariable API
    public void accept(TypeVisitor visitor)
    {
        visitor.visitTypeMirror(this);
        visitor.visitReferenceType(this);
        visitor.visitTypeVariable(this);
    }

    public TypeParameterDeclaration getDeclaration()
    {
        return this;
    }
    // End of implementation of TypeVariable API

    public String toString()
    {
        return getSimpleName();
    }

    public MirrorKind kind(){ return MirrorKind.TYPE_PARAMETER_VARIABLE; }
	
	public ITypeBinding getDeclarationBinding(){ return (ITypeBinding) _binding; }

    boolean isFromSource(){ return getDeclarationBinding().isFromSource(); }   
}
