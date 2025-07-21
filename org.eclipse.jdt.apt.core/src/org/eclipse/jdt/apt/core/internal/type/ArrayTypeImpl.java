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

import com.sun.mirror.type.ArrayType;
import com.sun.mirror.util.TypeVisitor;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorType;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class ArrayTypeImpl implements ArrayType, EclipseMirrorType
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

    @Override
	public void accept(TypeVisitor visitor)
    {
        visitor.visitArrayType(this);
    }

    @Override
	public EclipseMirrorType getComponentType()
    {
		final ITypeBinding elementType = _arrayBinding.getElementType();
        final int dimension = _arrayBinding.getDimensions();
        // guarding around error cases.
        if( dimension == 0 ) return null;
        final ITypeBinding result;
        if( dimension == 1 ) // the element type is the component type.
            result = elementType;
        else{
			final String componentKey = BindingKey.createArrayTypeBindingKey(elementType.getKey(), dimension - 1);
			result = _env.getTypeBindingFromKey(componentKey);
            if( result == null )
				throw new IllegalStateException("unknown component type for " + _arrayBinding); //$NON-NLS-1$
        }

        final EclipseMirrorType mirror = Factory.createTypeMirror(result, _env);
        if( mirror == null )
            return (EclipseMirrorType)Factory.createErrorClassType(result);
        return mirror;
    }

    @Override
	public String toString(){
    	final ITypeBinding elementType = _arrayBinding.getElementType();
    	final StringBuilder buffer = new StringBuilder();
    	String name = elementType.getQualifiedName();
    	buffer.append(name);
		for( int i=0, dim = _arrayBinding.getDimensions(); i<dim; i++ )
			buffer.append("[]"); //$NON-NLS-1$

		return buffer.toString();
    }

    @Override
	public boolean equals(Object obj)
    {
        if( obj instanceof ArrayTypeImpl )
            return _arrayBinding == ((ArrayTypeImpl)obj)._arrayBinding; //$IDENTITY-COMPARISON$
        return false;
    }

    @Override
	public ITypeBinding getTypeBinding(){ return _arrayBinding; }

    @Override
	public int hashCode(){ return _arrayBinding.hashCode(); }

    @Override
	public MirrorKind kind(){ return MirrorKind.TYPE_ARRAY; }

	@Override
	public BaseProcessorEnv getEnvironment(){ return _env; }

	@Override
	public boolean isAssignmentCompatible(EclipseMirrorType left) {
		return isSubTypeCompatible(left);
	}

	@Override
	public boolean isSubTypeCompatible(EclipseMirrorType type) {
		if (type.kind() == MirrorKind.TYPE_CLASS)
			return "java.lang.Object".equals(type.getTypeBinding().getQualifiedName()); //$NON-NLS-1$
		if (type.kind() == MirrorKind.TYPE_INTERFACE)
			return "java.lang.Cloneable".equals(type.getTypeBinding().getQualifiedName()) || //$NON-NLS-1$
				"java.io.Serializable".equals(type.getTypeBinding().getQualifiedName()); //$NON-NLS-1$
		if (type.kind() == MirrorKind.TYPE_ARRAY) {
			EclipseMirrorType element1 = getComponentType();
			EclipseMirrorType element2 = ((ArrayTypeImpl)type).getComponentType();
			return element1.isSubTypeCompatible(element2);
		}
		return false;
	}

}
