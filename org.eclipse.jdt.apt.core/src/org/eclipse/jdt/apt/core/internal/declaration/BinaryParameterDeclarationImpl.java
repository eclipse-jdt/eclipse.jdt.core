/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.declaration;

import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.util.SourcePosition;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Represents a formal parameter that came from binary.
 */
public class BinaryParameterDeclarationImpl extends ParameterDeclarationImpl implements ParameterDeclaration
{   
    /**
     * Parameter declaration from binary
     */
    public BinaryParameterDeclarationImpl(ExecutableDeclarationImpl executable, 
										  ITypeBinding typeBinding,
                                          int index,
                                          ProcessorEnvImpl env)
    {
        super(executable, typeBinding, index, env);      
    }
 
    public Collection<Modifier> getModifiers()
    {
		// TODO
		// we don't store this information. so simply return nothing for now.
        return Collections.emptyList();
    }

    public SourcePosition getPosition()
    {
        return null;
    }        
	
	public boolean equals(Object obj){
        if( obj instanceof BinaryParameterDeclarationImpl ){
            final BinaryParameterDeclarationImpl otherParam = (BinaryParameterDeclarationImpl)obj;
            return otherParam._paramIndex == _paramIndex  &&
                   otherParam._executable.getDeclarationBinding().isEqualTo(_executable.getDeclarationBinding()) ;
        }
        return false;
    }
	
	boolean isFromSource(){ return false; }

    ASTNode getAstNode(){ return null; }

    public IResource getResource(){ return null; }
} 
