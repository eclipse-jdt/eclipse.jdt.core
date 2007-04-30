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

import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.util.DeclarationVisitor;

import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.core.dom.IVariableBinding;

public class EnumConstantDeclarationImpl extends FieldDeclarationImpl implements EnumConstantDeclaration
{
    public EnumConstantDeclarationImpl(IVariableBinding binding, BaseProcessorEnv env)
    {
        super(binding, env);
        assert binding.isEnumConstant() : "binding does not represent an enum constant"; //$NON-NLS-1$
    }

    public void accept(DeclarationVisitor visitor)
    {
        visitor.visitEnumConstantDeclaration(this);
    }

    public EnumDeclaration getDeclaringType()
    {
        return (EnumDeclaration)super.getDeclaringType();
    }

    public MirrorKind kind(){ return MirrorKind.ENUM_CONSTANT; }
}
