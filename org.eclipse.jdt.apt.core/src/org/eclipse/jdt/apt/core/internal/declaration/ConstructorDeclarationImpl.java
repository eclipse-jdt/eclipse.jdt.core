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

    public void accept(DeclarationVisitor visitor)
    {
        visitor.visitConstructorDeclaration(this);
    }

    public MirrorKind kind(){ return MirrorKind.CONSTRUCTOR; }

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

