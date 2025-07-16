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
 *     tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.declaration;

import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

public class FieldDeclarationImpl extends MemberDeclarationImpl implements FieldDeclaration
{
    public FieldDeclarationImpl(final IVariableBinding binding, final BaseProcessorEnv env)
    {
        super(binding, env);
        assert binding.isField() : "binding doesn't represent a field"; //$NON-NLS-1$
    }

    @Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitFieldDeclaration(this);
    }

    @Override
	public String getConstantExpression()
    {
        final IVariableBinding field = getDeclarationBinding();
        final Object constant = field.getConstantValue();
        if( constant == null ) return null;
        return constant.toString();
    }

    @Override
	public Object getConstantValue()
    {
        final IVariableBinding field = getDeclarationBinding();
        return field.getConstantValue();
    }

    @Override
	public TypeDeclaration getDeclaringType()
    {
        final IVariableBinding field = getDeclarationBinding();
        final ITypeBinding outer = field.getDeclaringClass();
        return Factory.createReferenceType(outer, _env);
    }

    @Override
	public String getSimpleName()
    {
		final IVariableBinding field = getDeclarationBinding();
        final String name = field.getName();
        return name == null ? "" : name; //$NON-NLS-1$
    }

    @Override
	public TypeMirror getType()
    {
        final IVariableBinding field = getDeclarationBinding();
        final TypeMirror typeMirror = Factory.createTypeMirror( field.getType(), _env );
        if( typeMirror == null )
            return Factory.createErrorClassType(field.getType());
        return typeMirror;
    }

    @Override
	public IVariableBinding getDeclarationBinding()
    {
        return (IVariableBinding)_binding;
    }

    @Override
	public String toString()
    {
    /*
    	final StringBuilder buffer = new StringBuilder();
    	final IVariableBinding field = getDeclarationBinding();
    	if( field.getType() != null ){
    		buffer.append( field.getType() );
    		buffer.append(' ');
    	}
    	buffer.append(field.getName());
    	return buffer.toString();
    */
    	return getSimpleName();
    }

    @Override
	public MirrorKind kind(){ return MirrorKind.FIELD; }

    @Override
	public boolean isFromSource()
    {
        final ITypeBinding type = getDeclarationBinding().getDeclaringClass();
        return ( type != null && type.isFromSource() );
    }
}
