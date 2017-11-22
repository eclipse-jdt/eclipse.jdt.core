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

import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.TypeVisitor;
import java.util.Collection;

import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class InterfaceDeclarationImpl extends TypeDeclarationImpl implements InterfaceDeclaration, InterfaceType
{
    public InterfaceDeclarationImpl(final ITypeBinding binding, final BaseProcessorEnv env)
    {
        super(binding, env);
        assert binding.isInterface() : "binding does not represents an interface."; //$NON-NLS-1$
    }

    @Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitInterfaceDeclaration(this);
    }

    @Override
	public Collection<? extends MethodDeclaration> getMethods()
    {
        return _getMethods();
    }

    // start of implementation of InterfaceType API
    @Override
	public void accept(TypeVisitor visitor)
    {
        visitor.visitInterfaceType(this);
    }

    @Override
	public InterfaceDeclaration getDeclaration()
    {
        return (InterfaceDeclaration)super.getDeclaration();
    }
    // end of implementation of InterfaceType API

    @Override
	public MirrorKind kind(){ return MirrorKind.TYPE_INTERFACE; }
}
