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
package org.eclipse.jdt.apt.core.internal.declaration;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;

public class ASTBasedMethodDeclarationImpl
	extends ASTBasedExecutableDeclarationImpl
	implements MethodDeclaration{

	public ASTBasedMethodDeclarationImpl(
			final org.eclipse.jdt.core.dom.BodyDeclaration astNode,
			final IFile file,
			final BaseProcessorEnv env)
	{
		super(astNode, file, env);
	}

	@Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitMethodDeclaration(this);
    }

    @Override
	public TypeMirror getReturnType()
    {
    	final org.eclipse.jdt.core.dom.MethodDeclaration methodAstNode = getMethodAstNode();
    	final Type retType = methodAstNode.getReturnType2();
    	// some funny error case where the return type is missing but it's not a constructor.
    	if( retType == null )
    		return Factory.createErrorClassType(EMPTY_STRING);
        final ITypeBinding typeBinding = retType.resolveBinding();
        // This is most likely the reason that we end up with an ast based implementation.
        if( typeBinding == null ){
        	return Factory.createErrorClassType(retType.toString());
        }
        else{
        	final TypeMirror type = Factory.createTypeMirror(typeBinding, _env);
            if(type == null )
                return Factory.createErrorClassType(retType.toString());
            return type;
        }
    }

    @Override
	public MirrorKind kind(){ return MirrorKind.METHOD; }

}
