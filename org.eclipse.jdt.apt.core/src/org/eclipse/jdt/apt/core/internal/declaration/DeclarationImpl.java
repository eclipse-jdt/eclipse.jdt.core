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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;

import com.sun.mirror.declaration.Modifier;

public abstract class DeclarationImpl extends EclipseDeclarationImpl {

	/** the type binding corresponding to this declaration */
	protected final IBinding _binding;
	DeclarationImpl(final IBinding binding, final BaseProcessorEnv env )
	{
		super(env);
		assert binding != null : "binding cannot be null"; //$NON-NLS-1$
		_binding = binding;
	}

	@Override
	public boolean equals(Object obj)
    {
        if(obj instanceof DeclarationImpl)
            return _binding.isEqualTo( ((DeclarationImpl)obj)._binding );

        return false;
    }

	@Override
	public int hashCode(){
    	final String key = getDeclarationBinding().getKey();
    	return key == null ? 0 : key.hashCode();
    }

	 /**
     * @return the binding that corresponds to the original declaration.
     * For parameterized type or raw type, return the generic type declaration binding.
     * For parameterized method, return the method declaration binding that has the
     * type parameters not the one with the parameters substituted with type arguments.
     * In other cases, simply return the same binding.
     */
    public abstract IBinding getDeclarationBinding();

    @Override
	public Collection<Modifier> getModifiers()
    {
        final int modBits = getDeclarationBinding().getModifiers();
        final List<Modifier> mods = new ArrayList<>(4);
        if( org.eclipse.jdt.core.dom.Modifier.isAbstract(modBits) )
        	mods.add(Modifier.ABSTRACT);
        if( org.eclipse.jdt.core.dom.Modifier.isFinal(modBits) )
        	mods.add(Modifier.FINAL);
        if( org.eclipse.jdt.core.dom.Modifier.isNative(modBits) )
        	mods.add(Modifier.NATIVE);
        if( org.eclipse.jdt.core.dom.Modifier.isPrivate(modBits) )
        	mods.add(Modifier.PRIVATE);
        if( org.eclipse.jdt.core.dom.Modifier.isProtected(modBits) )
        	mods.add(Modifier.PROTECTED);
        if( org.eclipse.jdt.core.dom.Modifier.isPublic(modBits) )
        	mods.add(Modifier.PUBLIC);
        if( org.eclipse.jdt.core.dom.Modifier.isStatic(modBits) )
        	mods.add(Modifier.STATIC);
        if( org.eclipse.jdt.core.dom.Modifier.isStrictfp(modBits) )
        	mods.add(Modifier.STRICTFP);
        if( org.eclipse.jdt.core.dom.Modifier.isSynchronized(modBits) )
        	mods.add(Modifier.SYNCHRONIZED);
        if( org.eclipse.jdt.core.dom.Modifier.isTransient(modBits) )
        	mods.add(Modifier.TRANSIENT);
        if( org.eclipse.jdt.core.dom.Modifier.isVolatile(modBits) )
        	mods.add(Modifier.VOLATILE);
        return mods;
    }

    @Override
	public boolean isBindingBased(){ return true; }

    @Override
	ASTNode getAstNode(){
        if( !isFromSource() ) return null;
        return _env.getASTNodeForBinding(getDeclarationBinding());
    }

    @Override
	CompilationUnit getCompilationUnit(){
        if( !isFromSource() ) return null;
        return _env.getCompilationUnitForBinding(getDeclarationBinding());
    }

	@Override
	public IFile getResource(){
        if( isFromSource() ){
            final IBinding binding = getDeclarationBinding();
			return _env.getDeclaringFileForBinding(binding);
        }
        return null;
    }

}
