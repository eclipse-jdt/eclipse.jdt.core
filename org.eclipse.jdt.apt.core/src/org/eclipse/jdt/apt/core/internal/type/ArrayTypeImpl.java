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

import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.TypeVisitor;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class ArrayTypeImpl implements ArrayType, EclipseMirrorImpl
{
    private final ITypeBinding _arrayBinding;
	private final BaseProcessorEnv _env;
    public ArrayTypeImpl(final ITypeBinding binding, BaseProcessorEnv env)
    {
        _arrayBinding = binding;
		_env = env;
        assert _arrayBinding != null && _arrayBinding.isArray();
        assert env != null : "missing environment"; //$NON-NLS-1$
    }

    public void accept(TypeVisitor visitor)
    {
        visitor.visitTypeMirror(this);
        visitor.visitArrayType(this);
    }

    public TypeMirror getComponentType()
    {
		final ITypeBinding elementType = _arrayBinding.getElementType();
        final int dimension = _arrayBinding.getDimensions();
        // guarding around error cases.
        if( dimension == 0 ) return null;
        final ITypeBinding componentType;
        if( dimension == 1 ) // the element type is the component type.
            componentType = elementType;
        else{
            final ITypeBinding leaf = elementType.getElementType();
			final String componentKey = BindingKey.createArrayTypeBindingKey(leaf.getKey(), dimension - 1);
			componentType = _env.getTypeBinding(componentKey);
            if( componentType == null )
				throw new IllegalStateException("unknown component type for " + _arrayBinding); //$NON-NLS-1$
        }

        final TypeMirror mirror = Factory.createTypeMirror(componentType, _env);
        if( mirror == null )
            return Factory.createErrorClassType(componentType);
        return mirror;
    }

    public String toString(){ return _arrayBinding.toString(); }

    public boolean equals(Object obj)
    {
        if( obj instanceof ArrayTypeImpl )
            return _arrayBinding == ((ArrayTypeImpl)obj)._arrayBinding;
        return false;
    }

    public ITypeBinding getArrayBinding(){ return _arrayBinding; }

    public int hashCode(){ return _arrayBinding.hashCode(); }

    public MirrorKind kind(){ return MirrorKind.TYPE_ARRAY; }
	
	public BaseProcessorEnv getEnvironment(){ return _env; }
}
