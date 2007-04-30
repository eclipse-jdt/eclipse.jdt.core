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

package org.eclipse.jdt.apt.core.internal.type;

import com.sun.mirror.type.VoidType;
import com.sun.mirror.util.TypeVisitor;

import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorType;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class VoidTypeImpl implements VoidType, EclipseMirrorType
{   
	private final ITypeBinding _binding;

    public VoidTypeImpl(final ITypeBinding  binding){
		assert binding != null : "missing binding"; //$NON-NLS-1$
		_binding = binding;
	}
    
    public void accept(TypeVisitor visitor)
    {
        visitor.visitVoidType(this);
    }

    public String toString(){ return "void"; } //$NON-NLS-1$
	
	public ITypeBinding getTypeBinding(){return _binding;}

    public MirrorKind kind(){ return MirrorKind.TYPE_VOID; }
	
	public BaseProcessorEnv getEnvironment(){ return null; }

	public boolean isAssignmentCompatible(EclipseMirrorType left) {
		return false;
	}

	public boolean isSubTypeCompatible(EclipseMirrorType type) {
		return false;
	}
}
