/*******************************************************************************
 * Copyright (c) 2005, 2013 BEA Systems, Inc.
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
package org.eclipse.jdt.apt.core.internal.declaration;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;

import com.sun.mirror.declaration.ExecutableDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.util.DeclarationVisitor;

public abstract class ASTBasedExecutableDeclarationImpl
	extends ASTBasedMemberDeclarationImpl
	implements ExecutableDeclaration{

	public ASTBasedExecutableDeclarationImpl(
			final org.eclipse.jdt.core.dom.BodyDeclaration astNode,
			final IFile file,
			final BaseProcessorEnv env)
	{
		super(astNode, file, env);
	}

	@Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitExecutableDeclaration(this);
    }

    @Override
	public Collection<TypeParameterDeclaration> getFormalTypeParameters()
    {
    	return ExecutableUtil.getFormalTypeParameters(this, _env);
    }

    @Override
	public Collection<ParameterDeclaration> getParameters()
    {
    	return ExecutableUtil.getParameters(this, _env);
    }

    @Override
	public Collection<ReferenceType> getThrownTypes()
    {
    	return ExecutableUtil.getThrownTypes(this, _env);
    }

    @Override
	public boolean isVarArgs()
    {
        return getMethodAstNode().isVarargs();
    }

    @Override
	public String getSimpleName()
    {
    	final org.eclipse.jdt.core.dom.MethodDeclaration methodAstNode = getMethodAstNode();
    	final SimpleName nameNode = methodAstNode.getName();
    	return nameNode == null ? EMPTY_STRING : nameNode.getIdentifier();
    }

    org.eclipse.jdt.core.dom.MethodDeclaration getMethodAstNode(){
		return (org.eclipse.jdt.core.dom.MethodDeclaration)_astNode;
	}

    @Override
	public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        final org.eclipse.jdt.core.dom.MethodDeclaration methodAstNode = getMethodAstNode();
    	final List<TypeParameter> typeParams = methodAstNode.typeParameters();
        if( typeParams != null && typeParams.size() > 0 ){
        	 buffer.append('<');
             for(int i=0, size=typeParams.size(); i<size; i++ ){
                 if( i != 0 )
                     buffer.append(", "); //$NON-NLS-1$
                 buffer.append(typeParams.get(i).toString());
             }
             buffer.append('>');
        }

        if( methodAstNode.getReturnType2() != null )
            buffer.append(methodAstNode.getReturnType2());
        buffer.append(' ');
        buffer.append(methodAstNode.getName());
        buffer.append('(');
        int i=0;
    	final List<SingleVariableDeclaration> params = methodAstNode.parameters();
        for( SingleVariableDeclaration param : params ){
            if( i++ != 0 )
                buffer.append(", "); //$NON-NLS-1$
            buffer.append(param.getName());
        }
        buffer.append(')');

        return buffer.toString();
    }

}
