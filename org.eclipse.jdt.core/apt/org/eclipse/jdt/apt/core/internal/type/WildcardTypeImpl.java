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

import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.WildcardType;
import com.sun.mirror.util.TypeVisitor;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorType;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class WildcardTypeImpl implements WildcardType, EclipseMirrorType
{
    private final ITypeBinding _binding;
	private final BaseProcessorEnv _env;

    public WildcardTypeImpl(ITypeBinding binding, BaseProcessorEnv env)
    {
        _binding = binding;
		_env = env;
        assert _binding != null && _binding.isWildcardType();
        assert env != null : "missing environment"; //$NON-NLS-1$
    }

    @Override
	public void accept(TypeVisitor visitor)
    {
        visitor.visitWildcardType(this);
    }

    @Override
	public Collection<ReferenceType> getLowerBounds()
    {
        final ITypeBinding bound = _binding.getBound();
        // no bound or has an upper bound.
        if( bound == null || _binding.isUpperbound() )
            return Collections.emptyList();
        ReferenceType mirror = Factory.createReferenceType(bound, _env);
        if( mirror == null )
            mirror = Factory.createErrorClassType(bound);
        return Collections.singletonList(mirror);
    }

    @Override
	public Collection<ReferenceType> getUpperBounds()
    {
        final ITypeBinding bound = _binding.getBound();
        // no bound or has a lower bound.
        if( bound == null || !_binding.isUpperbound() )
            return Collections.emptyList();
        ReferenceType mirror = Factory.createReferenceType(bound, _env);
        if( mirror == null )
            mirror = Factory.createErrorClassType(bound);
        return Collections.singletonList(mirror);
    }

    @Override
	public String toString(){ return _binding.toString(); }
    @Override
	public int hashCode(){ return _binding.hashCode(); }
    @Override
	public boolean equals(Object obj)
    {
        if(obj instanceof WildcardTypeImpl )
            return ((WildcardTypeImpl)obj)._binding.isEqualTo(_binding);
        return false;
    }

    @Override
	public MirrorKind kind(){ return MirrorKind.TYPE_WILDCARD; }

    @Override
	public ITypeBinding getTypeBinding(){ return _binding; }

	@Override
	public BaseProcessorEnv getEnvironment(){ return _env; }

	@Override
	public boolean isAssignmentCompatible(EclipseMirrorType left) {
		return false;
	}

	@Override
	public boolean isSubTypeCompatible(EclipseMirrorType type) {
		return false;
	}

}
