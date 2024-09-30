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

import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.util.TypeVisitor;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorType;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class PrimitiveTypeImpl implements PrimitiveType, EclipseMirrorType
{
    private final ITypeBinding _binding;

    public PrimitiveTypeImpl(ITypeBinding binding)
    {
		assert binding != null;
        _binding = binding;
    }
    @Override
	public void accept(TypeVisitor visitor)
    {
        visitor.visitPrimitiveType(this);
    }

    @Override
	public PrimitiveType.Kind getKind()
    {
		final String name = getTypeBinding().getName();
		if( "int".equals(name) ) //$NON-NLS-1$
			return PrimitiveType.Kind.INT;
		else if( "byte".equals(name) ) //$NON-NLS-1$
			return PrimitiveType.Kind.BYTE;
		else if( "short".equals(name) ) //$NON-NLS-1$
			return PrimitiveType.Kind.SHORT;
		else if( "char".equals(name) ) //$NON-NLS-1$
			return PrimitiveType.Kind.CHAR;
		else if( "long".equals(name) ) //$NON-NLS-1$
			return PrimitiveType.Kind.LONG;
		else if( "float".equals(name) ) //$NON-NLS-1$
			return PrimitiveType.Kind.FLOAT;
		else if( "double".equals(name) ) //$NON-NLS-1$
			return PrimitiveType.Kind.DOUBLE;
		else if( "boolean".equals(name)) //$NON-NLS-1$
			return PrimitiveType.Kind.BOOLEAN;
		else
			throw new IllegalStateException("unrecognized primitive type " + _binding); //$NON-NLS-1$
    }

    @Override
	public String toString(){ return _binding.getName(); }

    @Override
	public ITypeBinding getTypeBinding(){ return _binding; }

    @Override
	public MirrorKind kind(){ return MirrorKind.TYPE_PRIMITIVE; }

	@Override
	public boolean equals(final Object obj)
	{
		try{
			return this._binding.isEqualTo( ((PrimitiveTypeImpl)obj)._binding );
		}
		catch(ClassCastException e){
			return false;
		}
	}

	@Override
	public BaseProcessorEnv getEnvironment(){ return null; }

	@Override
	public boolean isAssignmentCompatible(EclipseMirrorType left) {
		return getTypeBinding().isAssignmentCompatible(left.getTypeBinding());
	}
	@Override
	public boolean isSubTypeCompatible(EclipseMirrorType type) {
		return getTypeBinding().isSubTypeCompatible(type.getTypeBinding());
	}
}
