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
	
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitExecutableDeclaration(this);
    }
	
    public Collection<TypeParameterDeclaration> getFormalTypeParameters()
    {
    	return ExecutableUtil.getFormalTypeParameters(this, _env);
    }
    
    public Collection<ParameterDeclaration> getParameters()
    {
    	return ExecutableUtil.getParameters(this, _env);
    }

    public Collection<ReferenceType> getThrownTypes()
    {
    	return ExecutableUtil.getThrownTypes(this, _env);
    }

    public boolean isVarArgs()
    {
        return getMethodAstNode().isVarargs();
    }

    public String getSimpleName()
    {
    	final org.eclipse.jdt.core.dom.MethodDeclaration methodAstNode = getMethodAstNode(); 
    	final SimpleName nameNode = methodAstNode.getName();
    	return nameNode == null ? EMPTY_STRING : nameNode.getIdentifier();
    }
    
    org.eclipse.jdt.core.dom.MethodDeclaration getMethodAstNode(){ 
		return (org.eclipse.jdt.core.dom.MethodDeclaration)_astNode; 
	}
    
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        final org.eclipse.jdt.core.dom.MethodDeclaration methodAstNode = getMethodAstNode();
        @SuppressWarnings("unchecked")
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
        @SuppressWarnings("unchecked")
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
