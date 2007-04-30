/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.declaration;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;
import com.sun.mirror.util.TypeVisitor;

public class TypeParameterDeclarationImpl extends DeclarationImpl implements 
	TypeParameterDeclaration, TypeVariable, EclipseMirrorType
{
    public TypeParameterDeclarationImpl(final ITypeBinding binding,
                                        final BaseProcessorEnv env)
    {
		super(binding, env);
		assert binding.isTypeVariable();
    }

    public void accept(DeclarationVisitor visitor)
    {
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
            final ReferenceType type = Factory.createReferenceType(bound, _env);
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
		return Factory.createDeclaration(getOwnerBinding(), _env);    
    }

	private IBinding getOwnerBinding() {
		final ITypeBinding binding = getDeclarationBinding();
		// declared on a class
		IBinding owner = binding.getDeclaringClass();
		if( owner == null )
			// declared on the method
			owner = binding.getDeclaringMethod();
		return owner;
	}
    
    public SourcePosition getPosition()
    {
        if( isFromSource() )
        {
			final ASTNode node = getAstNode();  
			if( node == null ) return null;
            final CompilationUnit unit = getCompilationUnit();
            final int offset = node.getStartPosition();    		
            return new SourcePositionImpl(offset, 
            		                      node.getLength(), 
            							  unit.getLineNumber(offset), 
            							  unit.getColumnNumber(offset),
            							  this);
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
	public ITypeBinding getTypeBinding() { return (ITypeBinding)_binding;}

	public boolean isFromSource(){ return getDeclarationBinding().isFromSource(); }

	public boolean isAssignmentCompatible(EclipseMirrorType left) {
		return isSubTypeCompatible(left);
	}

	public boolean isSubTypeCompatible(EclipseMirrorType type) {
		if (type.kind() == MirrorKind.TYPE_PARAMETER_VARIABLE) {
			TypeParameterDeclarationImpl other = (TypeParameterDeclarationImpl) type;
			return getOwnerBinding() == other.getOwnerBinding() &&
				getSimpleName().equals(other.getSimpleName());
		}
		
		for (ReferenceType bound : getBounds()) {
			if (((EclipseMirrorType)bound).isSubTypeCompatible(type))
				return true;
		}
		
		return false;
	}   
}
