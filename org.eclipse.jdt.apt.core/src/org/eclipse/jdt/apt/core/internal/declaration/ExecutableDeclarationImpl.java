/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
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

import com.sun.mirror.declaration.ExecutableDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.util.DeclarationVisitor;
import java.util.Collection;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public abstract class ExecutableDeclarationImpl
	extends MemberDeclarationImpl implements ExecutableDeclaration
{
    public ExecutableDeclarationImpl(final IMethodBinding binding, final BaseProcessorEnv env)
    {
        super(binding, env);
    }

    @Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitExecutableDeclaration(this);
    }

    @Override
	public TypeDeclaration getDeclaringType()
    {
        final IMethodBinding methodBinding = getDeclarationBinding();
        return Factory.createReferenceType(methodBinding.getDeclaringClass(), _env);
    }

    @Override
	public Collection<TypeParameterDeclaration> getFormalTypeParameters()
    {
    	return ExecutableUtil.getFormalTypeParameters(this, _env);
    }
    @Override
	public Collection<ParameterDeclaration> getParameters()
    {
    	return ExecutableUtil.getParameters(this, _env);
    }

    @Override
	public Collection<ReferenceType> getThrownTypes()
    {
    	return ExecutableUtil.getThrownTypes(this, _env);
    }

    @Override
	public boolean isVarArgs()
    {
        return getDeclarationBinding().isVarargs();
    }

    @Override
	public String getSimpleName()
    {
		return getDeclarationBinding().getName();
    }

    @Override
	public IMethodBinding getDeclarationBinding()
    {
        return (IMethodBinding)_binding;
    }

    @Override
	public boolean isFromSource()
    {
        final ITypeBinding type = getDeclarationBinding().getDeclaringClass();
        return ( type != null && type.isFromSource() );
    }
}
