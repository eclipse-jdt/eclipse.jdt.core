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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.sun.mirror.declaration.ExecutableDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.util.DeclarationVisitor;

public abstract class ExecutableDeclarationImpl extends MemberDeclarationImpl implements ExecutableDeclaration
{
    public ExecutableDeclarationImpl(final IMethodBinding binding, final BaseProcessorEnv env)
    {     
        super(binding, env);
    }

    public void accept(DeclarationVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitExecutableDeclaration(this);
    }

    public TypeDeclaration getDeclaringType()
    {
        final IMethodBinding methodBinding = getDeclarationBinding();        
        return Factory.createReferenceType(methodBinding.getDeclaringClass(), _env);
    }

    public Collection<TypeParameterDeclaration> getFormalTypeParameters()
    {
        final IMethodBinding methodBinding = getDeclarationBinding();
		final ITypeBinding[] typeParams = methodBinding.getTypeParameters();        
        if( typeParams == null || typeParams.length == 0 )
            return Collections.emptyList();
        final List<TypeParameterDeclaration> result = new ArrayList<TypeParameterDeclaration>();
        for( ITypeBinding typeVar : typeParams ){
            final TypeParameterDeclaration typeParamDecl = (TypeParameterDeclaration)Factory.createDeclaration(typeVar, _env);
            if( typeParamDecl != null )
                result.add(typeParamDecl);
        }
        return result;
    }
    public Collection<ParameterDeclaration> getParameters()
    {
        final IMethodBinding methodBinding = getDeclarationBinding();
        final ITypeBinding[] paramTypes = methodBinding.getParameterTypes();
        if( paramTypes == null || paramTypes.length == 0 )
            return Collections.emptyList();        
        final List<ParameterDeclaration> result = new ArrayList<ParameterDeclaration>(paramTypes.length);        
        
        for( int i=0; i<paramTypes.length; i++ ){
            final ITypeBinding type = paramTypes[i];
            final ParameterDeclaration param = Factory.createParameterDeclaration(this, i, type, _env);
            result.add(param);
        }

        return result;
    }

    public Collection<ReferenceType> getThrownTypes()
    {
        final IMethodBinding methodBinding = getDeclarationBinding();
        final ITypeBinding[] exceptions = methodBinding.getExceptionTypes();
        final List<ReferenceType> results = new ArrayList<ReferenceType>(4);
        for( ITypeBinding exception : exceptions ){
            final TypeDeclaration mirrorDecl = Factory.createReferenceType(exception, _env);
            if( mirrorDecl != null)
                results.add((ReferenceType)mirrorDecl);
        }
        return results;
    }

    public boolean isVarArgs()
    {
        return getDeclarationBinding().isVarargs();
    }

    public String getSimpleName()
    {
		return getDeclarationBinding().getName();
    }

    public IMethodBinding getDeclarationBinding()
    {
        return (IMethodBinding)_binding;
    }

    public boolean isFromSource()
    {
        final ITypeBinding type = getDeclarationBinding().getDeclaringClass();
        return ( type != null && type.isFromSource() );
    }
}