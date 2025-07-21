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

import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * This field declaration implementation is based on the variable declaration
 * fragment ast node in the parse tree.
 * The most common scenario where such implementation is required is when
 * the type of the field cannot be resolved. In this case, the jdt will not
 * create the field binding. Information such as the declaring type as well
 * as the name of the field will still be captured and make available to clients.
 */
public class ASTBasedFieldDeclarationImpl
	extends ASTBasedMemberDeclarationImpl
	implements FieldDeclaration {

	public ASTBasedFieldDeclarationImpl(
			final VariableDeclarationFragment astNode,
			final IFile file,
			final BaseProcessorEnv env)
    {
		super(astNode, file, env);
		assert astNode.getParent() != null &&
			   astNode.getParent().getNodeType() == ASTNode.FIELD_DECLARATION :
			   "parent isn't a field declaration"; //$NON-NLS-1$
    }

	@Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitFieldDeclaration(this);
    }

    @Override
	public String getConstantExpression()
    {
        final Object constant = getConstantValue();
        if( constant == null ) return null;
        return constant.toString();
    }

    @Override
	public Object getConstantValue()
    {
    	final VariableDeclarationFragment fragment = getAstNode();
    	final Expression initializer = fragment.getInitializer();
        if( initializer == null ) return null;
        return initializer.resolveConstantExpressionValue();
    }

    @Override
	public String getSimpleName() {
		final VariableDeclarationFragment fragment = getAstNode();
		final SimpleName nameNode = fragment.getName();
		return nameNode == null ? EMPTY_STRING : nameNode.getIdentifier();
	}

    @Override
	public TypeMirror getType()
    {
        final org.eclipse.jdt.core.dom.FieldDeclaration fieldASTNode = getFieldDeclarationAstNode();
        final Type type = fieldASTNode.getType();
        if( type == null )
        	return null;
        final ITypeBinding typeBinding = type.resolveBinding();
        // This is probably why we end up with an ast based implementation.
        if( typeBinding == null )
        	return Factory.createErrorClassType(type.toString());
        else{
	        TypeMirror typeMirror = Factory.createTypeMirror( typeBinding, _env );
	        if( typeMirror == null )
	        	typeMirror = Factory.createErrorClassType(typeBinding);
	        return typeMirror;
        }
    }

    @Override
	public String toString()
    {
    	/*
    	final org.eclipse.jdt.core.dom.FieldDeclaration fieldASTNode = getFieldDeclarationAstNode();
    	StringBuilder buffer = new StringBuilder();
    	final Type type = fieldASTNode.getType();
        if( type != null ){
        	buffer.append(type);
        	buffer.append(' ');
        }
        buffer.append( getSimpleName() );

        return buffer.toString();
        */
    	return getSimpleName();
    }

    @Override
	public MirrorKind kind(){ return MirrorKind.FIELD; }

    @Override
	VariableDeclarationFragment getAstNode()
    {
    	return (VariableDeclarationFragment)_astNode;
    }

    org.eclipse.jdt.core.dom.FieldDeclaration getFieldDeclarationAstNode()
    {
    	return (org.eclipse.jdt.core.dom.FieldDeclaration)_astNode.getParent();
    }
}
