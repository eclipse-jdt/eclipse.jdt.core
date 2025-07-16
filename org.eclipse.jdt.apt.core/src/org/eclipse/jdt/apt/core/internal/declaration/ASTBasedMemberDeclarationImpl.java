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

import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.util.DeclarationVisitor;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;

public abstract class ASTBasedMemberDeclarationImpl
	extends ASTBasedDeclarationImpl implements MemberDeclaration{

	public ASTBasedMemberDeclarationImpl(
			ASTNode astNode,
			IFile file,
			BaseProcessorEnv env)
	{
		super(astNode, file, env);
	}

	@Override
	public void accept(DeclarationVisitor visitor) {
        visitor.visitMemberDeclaration(this);
	}

    @Override
	public TypeDeclaration getDeclaringType()
    {
	    final AbstractTypeDeclaration parentType = getContainingTypeAstNode();
	    // most likely a mal-formed text.
	    if( parentType == null )
	    	return null;

	    final ITypeBinding parentTypeBinding = parentType.resolveBinding();
	    if( parentTypeBinding == null )
	    	throw new UnsupportedOperationException("Type declaration that doesn't have binding"); //$NON-NLS-1$
	    return Factory.createReferenceType(parentTypeBinding, _env);
    }

    @Override
	public String getDocComment()
    {
    	final ASTNode node = getAstNode();

		if( node instanceof BodyDeclaration )
			return getDocComment((BodyDeclaration)node);

		else if( node.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT ){
			final ASTNode parent = node.getParent();
			// a field declaration
			if( parent instanceof BodyDeclaration )
				return getDocComment((BodyDeclaration)parent);

		}
		return EMPTY_STRING;
    }

    /**
     * @return the closest ancestor to the ast node in this instance that
     * is a type declaration node or <code>null</code> if none is found.
     */
    protected AbstractTypeDeclaration getContainingTypeAstNode()
    {
    	ASTNode cur = _astNode;
    	while(cur != null){
    		switch(cur.getNodeType()){
    		case ASTNode.ANNOTATION_TYPE_DECLARATION:
    		case ASTNode.ENUM_DECLARATION:
    		case ASTNode.TYPE_DECLARATION:
    			return (AbstractTypeDeclaration)cur;
    		}
    		cur = cur.getParent();
    	}
    	return null;
    }
}
