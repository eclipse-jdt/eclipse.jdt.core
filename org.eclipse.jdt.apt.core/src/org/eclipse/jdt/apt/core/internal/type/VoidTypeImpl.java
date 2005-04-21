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

package org.eclipse.jdt.apt.core.internal.type;

import com.sun.mirror.type.VoidType;
import com.sun.mirror.util.TypeVisitor;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class VoidTypeImpl implements VoidType, EclipseMirrorImpl
{
    public static final VoidType TYPE_VOID = new VoidTypeImpl();
    public static final ITypeBinding VOID_TYPE_BINDING;

    static{
        final org.eclipse.jdt.core.dom.PrimitiveType astVoid =
            PrimitiveTypeImpl.STATIC_AST.newPrimitiveType(org.eclipse.jdt.core.dom.PrimitiveType.VOID);
        VOID_TYPE_BINDING = astVoid.resolveBinding();        
    }

    private VoidTypeImpl(){}
    
    public void accept(TypeVisitor visitor)
    {
        visitor.visitTypeMirror(this);
        visitor.visitVoidType(this);
    }

    public String toString(){ return "void"; }

    public MirrorKind kind(){ return MirrorKind.TYPE_VOID; }
	
	public ProcessorEnvImpl getEnvironment(){ return null; }
}
