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
    
    public void accept(TypeVisitor visitor)
    {
        visitor.visitWildcardType(this);
    }

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

    public String toString(){ return _binding.toString(); }
    public int hashCode(){ return _binding.hashCode(); }
    public boolean equals(Object obj)
    {
        if(obj instanceof WildcardTypeImpl )
            return ((WildcardTypeImpl)obj)._binding.isEqualTo(_binding);
        return false;
    }

    public MirrorKind kind(){ return MirrorKind.TYPE_WILDCARD; }

    public ITypeBinding getTypeBinding(){ return _binding; }
	
	public BaseProcessorEnv getEnvironment(){ return _env; }

	public boolean isAssignmentCompatible(EclipseMirrorType left) {
		return false;
	}

	public boolean isSubTypeCompatible(EclipseMirrorType type) {
		return false;
	}

}
