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
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;

/**
 * Represents a formal parameter that came from source
 */
public class SourceParameterDeclarationImpl
	extends ASTBasedDeclarationImpl implements ParameterDeclaration{

    /**
     * Parameter declaration from source files
     * @param astNode the ast node that defines this parameter
     * @param file the file where the ast node originates
     * @param env
     */
    public SourceParameterDeclarationImpl(SingleVariableDeclaration astNode,
    									  IFile file,
										  BaseProcessorEnv env)
    {
    	super( astNode, file, env);
    }

    @Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitParameterDeclaration(this);
    }

    @Override
	public TypeMirror getType()
    {
    	final SingleVariableDeclaration astNode = getAstNode();
    	final Type  type = astNode.getType();
    	if( type == null )
    		return Factory.createErrorClassType(EMPTY_STRING);
    	final IVariableBinding varBinding = astNode.resolveBinding();
    	if( varBinding == null ){
    		String typeName = type.toString();
			 if( astNode.isVarargs() )
				 return Factory.createErrorArrayType(typeName, 1);
			 else
				 return Factory.createErrorClassType(typeName);
    	}
    	else{
    		 final ITypeBinding typeBinding = varBinding.getType();
    		 if( typeBinding == null ){
    			 String typeName = type.toString();
    			 if( astNode.isVarargs() )
    				 return Factory.createErrorArrayType(typeName, 1);
    			 else
    				 return Factory.createErrorClassType(typeName);
             }
    		 else{
	        	final TypeMirror mirrorType = Factory.createTypeMirror(typeBinding, _env);
	            if(mirrorType == null )
	                return Factory.createErrorClassType(type.toString());
	            return mirrorType;
    	     }
    	}
    }

    @Override
	public String getSimpleName()
    {
    	final Name nameNode = getAstNode().getName();
    	return nameNode == null ? EMPTY_STRING : nameNode.toString();
    }

    @Override
	public String getDocComment()
    {
    	return EMPTY_STRING;
    }

    @Override
	SingleVariableDeclaration getAstNode()
    {
    	return (SingleVariableDeclaration)_astNode;
    }

    @Override
	public MirrorKind kind(){ return MirrorKind.FORMAL_PARAMETER; }

    @Override
	public String toString(){
    	return _astNode.toString();
    }

	@Override
	public boolean equals(Object obj){
        if( obj instanceof SourceParameterDeclarationImpl ){
            final SourceParameterDeclarationImpl otherParam = (SourceParameterDeclarationImpl)obj;
            return _astNode == otherParam._astNode;
        }
        return false;
    }

	@Override
	public int hashCode(){
		return _astNode.hashCode();
    }
}


