/*******************************************************************************
 * Copyright (c) 2005, 2015 BEA Systems, Inc.
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

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.TypeVisitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class ClassDeclarationImpl extends TypeDeclarationImpl implements ClassDeclaration, ClassType
{
    public ClassDeclarationImpl(final ITypeBinding binding, final BaseProcessorEnv env)
    {
        super(binding, env);
        // Enum types return false for isClass().
        assert !binding.isInterface();
    }

    @Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitClassDeclaration(this);
    }

    @SuppressWarnings("rawtypes")
	private void getASTConstructor(
    		final AbstractTypeDeclaration typeDecl,
    		final List<ConstructorDeclaration> results){

    	final List bodyDecls = typeDecl.bodyDeclarations();
    	IFile file = null;
    	for( int i=0, len=bodyDecls.size(); i<len; i++ ){
    		final BodyDeclaration bodyDecl = (BodyDeclaration)bodyDecls.get(i);
    		if( bodyDecl.getNodeType() == ASTNode.METHOD_DECLARATION ){
    			final org.eclipse.jdt.core.dom.MethodDeclaration methodDecl =
    					(org.eclipse.jdt.core.dom.MethodDeclaration)bodyDecl;

    			if( methodDecl.isConstructor() ){
    				final IMethodBinding methodBinding = methodDecl.resolveBinding();
    				// built an ast based representation.
    				if( methodBinding == null ){
    					if( file == null )
        					file = getResource();
        				ConstructorDeclaration mirrorDecl =
        					(ConstructorDeclaration)Factory.createDeclaration(methodDecl, file, _env);
        				if( mirrorDecl != null )
        					results.add(mirrorDecl);
    				}
    			}
    		}
    	}
    }

    @Override
	public Collection<ConstructorDeclaration> getConstructors()
    {
    	final List<ConstructorDeclaration> results = new ArrayList<>();
    	if( isFromSource() ){
    		// need to consult the ast since methods with broken signature
    		// do not appear in bindings.
    		final ITypeBinding typeBinding = getDeclarationBinding();
    		final ASTNode node =
    			_env.getASTNodeForBinding(typeBinding);
    		if( node != null ){
    			switch( node.getNodeType() )
    			{
    			case ASTNode.TYPE_DECLARATION:
    			case ASTNode.ANNOTATION_TYPE_DECLARATION:
    			case ASTNode.ENUM_DECLARATION:
    				AbstractTypeDeclaration typeDecl =
    					(AbstractTypeDeclaration)node;
    				// built the ast based methods first.
    				getASTConstructor(typeDecl, results);
    				break;
    			default:
    				// the ast node for a type binding should be a AbstractTypeDeclaration.
    				throw new IllegalStateException("expecting a AbstractTypeDeclaration but got "  //$NON-NLS-1$
    						+ node.getClass().getName() );
    			}
    		}
    	}
        // build methods for binding type or
    	// build the binding based method for source type.

        final IMethodBinding[] methods = getDeclarationBinding().getDeclaredMethods();
        for( IMethodBinding method : methods ){
            if( method.isSynthetic() ) continue;
            if( method.isConstructor() ){
                Declaration mirrorDecl = Factory.createDeclaration(method, _env);
                if( mirrorDecl != null)
                    results.add((ConstructorDeclaration)mirrorDecl);
            }
        }
        return results;

    }

	@Override
	@SuppressWarnings("unchecked")
	public Collection<MethodDeclaration> getMethods()
    {
        return (Collection<MethodDeclaration>)_getMethods();
    }

    // Start of implementation of ClassType API
    @Override
	public void accept(TypeVisitor visitor)
    {
        visitor.visitClassType(this);
    }

    @Override
	public ClassType getSuperclass()
    {
        final ITypeBinding superClass = getDeclarationBinding().getSuperclass();
		if ( superClass == null )
			return null;
		else if( superClass.isClass() && !superClass.isRecovered() )
            return (ClassType)Factory.createReferenceType(superClass, _env);
        else // catch error case where user extends some interface instead of a class.
            return Factory.createErrorClassType(superClass);
    }

    @Override
	public ClassDeclaration getDeclaration()
    {
        return (ClassDeclaration)super.getDeclaration();
    }
    // End of implementation of ClassType API

    @Override
	public MirrorKind kind(){ return MirrorKind.TYPE_CLASS; }
}
