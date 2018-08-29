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

import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.core.dom.IMethodBinding;

import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.util.DeclarationVisitor;

public class ConstructorDeclarationImpl extends ExecutableDeclarationImpl implements ConstructorDeclaration
{
    public ConstructorDeclarationImpl(final IMethodBinding binding, BaseProcessorEnv env)
    {
        super(binding, env);
        assert binding.isConstructor() : "binding does not represent a constructor"; //$NON-NLS-1$
    }

    @Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitConstructorDeclaration(this);
    }

    @Override
	public MirrorKind kind(){ return MirrorKind.CONSTRUCTOR; }

    @Override
	public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        final IMethodBinding methodBinding = getDeclarationBinding();
        buffer.append(methodBinding.getName());
        buffer.append('(');
        int i=0;
        for( ParameterDeclaration param : getParameters() ){
            if( i++ != 0 )
                buffer.append(", "); //$NON-NLS-1$
            buffer.append(param);
        }
        buffer.append(')');

        return buffer.toString();
    }
}

