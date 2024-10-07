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

    @Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitEnumConstantDeclaration(this);
    }

    @Override
	public EnumDeclaration getDeclaringType()
    {
        return (EnumDeclaration)super.getDeclaringType();
    }

    @Override
	public MirrorKind kind(){ return MirrorKind.ENUM_CONSTANT; }
}
