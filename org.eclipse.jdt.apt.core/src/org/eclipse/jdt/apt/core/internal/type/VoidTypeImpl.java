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

    @Override
	public void accept(TypeVisitor visitor)
    {
        visitor.visitVoidType(this);
    }

    @Override
	public String toString(){ return "void"; } //$NON-NLS-1$

	@Override
	public ITypeBinding getTypeBinding(){return _binding;}

    @Override
	public MirrorKind kind(){ return MirrorKind.TYPE_VOID; }

	@Override
	public BaseProcessorEnv getEnvironment(){ return null; }

	@Override
	public boolean isAssignmentCompatible(EclipseMirrorType left) {
		return false;
	}

	@Override
	public boolean isSubTypeCompatible(EclipseMirrorType type) {
		return false;
	}
}
