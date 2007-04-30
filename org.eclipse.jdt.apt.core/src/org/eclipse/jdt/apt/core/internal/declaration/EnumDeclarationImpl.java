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


import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.type.EnumType;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.TypeVisitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public class EnumDeclarationImpl extends ClassDeclarationImpl implements EnumDeclaration, EnumType
{
    public EnumDeclarationImpl(final ITypeBinding binding, final BaseProcessorEnv env)
    {
        super(binding, env);
        assert binding.isEnum() : "binding is not an enum."; //$NON-NLS-1$
    }

    public void accept(DeclarationVisitor visitor)
    {
        visitor.visitEnumDeclaration(this);
    }

    public Collection<EnumConstantDeclaration> getEnumConstants()
    {
        final ITypeBinding enumBinding = getDeclarationBinding();
        final IVariableBinding[] fields = enumBinding.getDeclaredFields();
        final List<EnumConstantDeclaration> results = new ArrayList<EnumConstantDeclaration>(4);
        for( IVariableBinding field : fields ){
            if( field.isEnumConstant() ){
                final Declaration mirrorDecl = Factory.createDeclaration(field, _env);
                if( mirrorDecl  != null )
                    results.add((EnumConstantDeclaration)mirrorDecl);
            }
        }
        return results;
    }

    // start of implementation of EnumType API
    
    // end of implementation of EnumType API
    public void accept(TypeVisitor visitor)
    {
        visitor.visitEnumType(this);
    }

    public EnumDeclaration getDeclaration()
    {
        return (EnumDeclaration)super.getDeclaration();
    }

    public MirrorKind kind(){ return MirrorKind.TYPE_ENUM; }
}
