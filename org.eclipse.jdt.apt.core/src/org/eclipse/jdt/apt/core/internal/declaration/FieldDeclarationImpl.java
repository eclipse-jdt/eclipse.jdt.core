/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.declaration;

import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;

public class FieldDeclarationImpl extends MemberDeclarationImpl implements FieldDeclaration
{
    public FieldDeclarationImpl(final IVariableBinding binding, final BaseProcessorEnv env)
    {
        super(binding, env);
        assert binding.isField() : "binding doesn't represent a field"; //$NON-NLS-1$
    }
    
    public void accept(DeclarationVisitor visitor)
    {
        visitor.visitFieldDeclaration(this);
    }

    public String getConstantExpression()
    {
        final IVariableBinding field = getDeclarationBinding();
        final Object constant = field.getConstantValue();
        if( constant == null ) return null;
        return constant.toString();   
    }

    public Object getConstantValue()
    {
        final IVariableBinding field = getDeclarationBinding();
        return field.getConstantValue();
    }

    public TypeDeclaration getDeclaringType()
    {
        final IVariableBinding field = getDeclarationBinding();
        final ITypeBinding outer = field.getDeclaringClass();
        return Factory.createReferenceType(outer, _env);
    }

    public String getSimpleName()
    {
		final IVariableBinding field = getDeclarationBinding();
        final String name = field.getName();
        return name == null ? "" : name; //$NON-NLS-1$
    }

    public TypeMirror getType()
    {
        final IVariableBinding field = getDeclarationBinding();
        final TypeMirror typeMirror = Factory.createTypeMirror( field.getType(), _env );
        if( typeMirror == null )
            return Factory.createErrorClassType(field.getType());
        return typeMirror;
    }

    public IVariableBinding getDeclarationBinding()
    {
        return (IVariableBinding)_binding;
    }
    
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

    public MirrorKind kind(){ return MirrorKind.FIELD; }

    public boolean isFromSource()
    {
        final ITypeBinding type = getDeclarationBinding().getDeclaringClass();
        return ( type != null && type.isFromSource() );
    }
}
