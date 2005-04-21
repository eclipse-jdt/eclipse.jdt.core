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

package org.eclipse.jdt.apt.core.internal.util;

import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.util.Declarations;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.NonEclipseImplementationException;
import org.eclipse.jdt.apt.core.internal.declaration.DeclarationImpl;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;

public class DeclarationsUtil implements Declarations
{
    public boolean hides(MemberDeclaration sub, MemberDeclaration sup)
    {
        throw new UnsupportedOperationException("NYI");
    }

    public boolean overrides(MethodDeclaration sub, MethodDeclaration sup)
    {
        final IMethodBinding subBinding = (IMethodBinding)getBinding(sub);
        final IMethodBinding supBinding = (IMethodBinding)getBinding(sup);
        if(subBinding == null || supBinding == null) return false;
        return subBinding.overrides(supBinding);
    }

    private IBinding getBinding(MemberDeclaration memberDecl)
        throws NonEclipseImplementationException
    {
        if( memberDecl == null ) return null;
        if( memberDecl instanceof EclipseMirrorImpl ){
            return ((DeclarationImpl)memberDecl).getDeclarationBinding();
        }
        throw new NonEclipseImplementationException("only applicable to eclipse type system objects." +
                                                    " Found " + memberDecl.getClass().getName());
    }
}
