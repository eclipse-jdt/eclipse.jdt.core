/*******************************************************************************
 * Copyright (c) 2005, 2015 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

    @Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitTypeParameterDeclaration(this);
    }

    @Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass)
    {
        return null;
    }

    @Override
	public Collection<AnnotationMirror> getAnnotationMirrors()
    {
        return Collections.emptyList();
    }

    @Override
	public Collection<ReferenceType> getBounds()
    {
        final ITypeBinding[] bounds = getDeclarationBinding().getTypeBounds();
        if( bounds == null || bounds.length == 0 )
            return Collections.emptyList();

        final Collection<ReferenceType> result = new ArrayList<>(4);
        for( ITypeBinding bound : bounds ){
            final ReferenceType type = Factory.createReferenceType(bound, _env);
             if( type != null )
                result.add(type);
        }

        return result;
    }

    @Override
	public String getDocComment()
    {
        return null;
    }

    @Override
	public Collection<Modifier> getModifiers()
    {
        return Collections.emptyList();
    }

    @Override
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

    @Override
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

    @Override
	public String getSimpleName()
    {
        final ITypeBinding typeVar = getDeclarationBinding();
        return typeVar.getName();
    }

    // Start of implementation of TypeVariable API
    @Override
	public void accept(TypeVisitor visitor)
    {
        visitor.visitTypeVariable(this);
    }

    @Override
	public TypeParameterDeclaration getDeclaration()
    {
        return this;
    }
    // End of implementation of TypeVariable API

    @Override
	public String toString()
    {
        return getSimpleName();
    }

    @Override
	public MirrorKind kind(){ return MirrorKind.TYPE_PARAMETER_VARIABLE; }

	@Override
	public ITypeBinding getDeclarationBinding(){ return (ITypeBinding) _binding; }
	@Override
	public ITypeBinding getTypeBinding() { return (ITypeBinding)_binding;}

	@Override
	public boolean isFromSource(){ return getDeclarationBinding().isFromSource(); }

	@Override
	public boolean isAssignmentCompatible(EclipseMirrorType left) {
		return isSubTypeCompatible(left);
	}

	@Override
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
