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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.util.SourcePosition;

/**
 * Represents a formal parameter that came from source
 */
public class SourceParameterDeclarationImpl extends ParameterDeclarationImpl implements ParameterDeclaration{

    // Ideally, we would store the IVariableBinding, but getting to it could be expensive
    // since it requires the ast node (SingleVariableDeclaration).
    // This could have a performance impact if the parameter did not come
    // from the compilation unit that is in the processor environment. In such a case,
    // we will have to go parse and resolve the file that contains the declaration.
    // Currently, the decision is to turn this into a on-demand process. (theodora)
   
    /**
     * Parameter declaration from source files
     * @param the executable that declares this parameter
     * @param type the type of the parameter
     * @param index the index of this parameter in <code>executable</code>'s param list.
     */
    public SourceParameterDeclarationImpl(ExecutableDeclarationImpl executable, 
										  ITypeBinding type, 
										  int index,
										  ProcessorEnvImpl env)
    {	
		super(executable, type, index, env);	
    }
	
	public Collection<Modifier> getModifiers()
	{
		final SingleVariableDeclaration paramDecl = (SingleVariableDeclaration)getAstNode();
		if( paramDecl == null ) return Collections.emptyList();
		final List<IExtendedModifier> extMods = paramDecl.modifiers();
        if( extMods == null || extMods.isEmpty() ) return Collections.emptyList();      
        for( IExtendedModifier extMod : extMods ){
            if( extMod.isModifier() ){				
				final org.eclipse.jdt.core.dom.Modifier mod = 
					(org.eclipse.jdt.core.dom.Modifier)extMod;
				if( org.eclipse.jdt.core.dom.Modifier.isFinal(mod.getFlags()) )
					return Collections.singletonList(Modifier.FINAL);                
            }
        }
		return Collections.emptyList();
	}
 
    public SourcePosition getPosition()
    {
		final ASTNode node = getAstNode();
		if( node == null ) return null;
        final CompilationUnit unit = _executable.getCompilationUnit();
        final int offset = node.getStartPosition();
		//TODO: waiting on new API Bugzilla #97766
        return new SourcePositionImpl(node.getStartPosition(),
                                      node.getLength(),
                                      unit.lineNumber(offset),
                                      0,//unit.columnNumber(offset),
                                      this);
    }
    
	public boolean equals(Object obj){
        if( obj instanceof SourceParameterDeclarationImpl ){
            final SourceParameterDeclarationImpl otherParam = (SourceParameterDeclarationImpl)obj;
            return otherParam._paramIndex == _paramIndex  &&
                   otherParam._executable.getDeclarationBinding().isEqualTo(_executable.getDeclarationBinding()) ;
        }
        return false;
    }
  	
	boolean isFromSource(){ return true; }

    SingleVariableDeclaration getAstNode()
    {
        final MethodDeclaration methodDecl = (MethodDeclaration)_executable.getAstNode();
		if( methodDecl == null ) return null;
        return (SingleVariableDeclaration)methodDecl.parameters().get(_paramIndex);
    }

    CompilationUnit getCompilationUnit()
    {
        return _executable.getCompilationUnit();
    }

    public IResource getResource(){
        return _executable.getResource();
    }
}


