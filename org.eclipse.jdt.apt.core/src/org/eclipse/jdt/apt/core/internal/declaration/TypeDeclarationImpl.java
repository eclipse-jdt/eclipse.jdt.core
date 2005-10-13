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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import com.sun.mirror.declaration.*;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.TypeVisitor;

public abstract class TypeDeclarationImpl extends MemberDeclarationImpl implements TypeDeclaration, DeclaredType, ReferenceType
{
	// jdt core compiler add a field to a type with the following name when there is a hierachy problem with the type.	
	private static final String HAS_INCONSISTENT_TYPE_HIERACHY = "has inconsistent hierarchy"; //$NON-NLS-1$
    public TypeDeclarationImpl(final ITypeBinding binding,
                               final BaseProcessorEnv env)
    {
        super(binding, env);
    }

    public String getQualifiedName()
    {
        ITypeBinding type = getTypeBinding();
        return type.getQualifiedName();      
    }

    public String getSimpleName()
    {
    	ITypeBinding type = getTypeBinding();
    	return type.getName();        
    }

    public PackageDeclaration getPackage()
    {
        ITypeBinding binding = getDeclarationBinding();
		return new PackageDeclarationImpl(binding.getPackage(), this, _env, false);        
    }

    public void accept(DeclarationVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitTypeDeclaration(this);
    }

    public ITypeBinding getTypeBinding(){ return (ITypeBinding)_binding; }

    public Collection<FieldDeclaration> getFields()
    {
        final IVariableBinding[] fields = getDeclarationBinding().getDeclaredFields();
        final List<FieldDeclaration> results = new ArrayList<FieldDeclaration>(fields.length);
        for( IVariableBinding field : fields ){
        	// note that the name HAS_INCONSISTENT_TYPE_HIERACHY is not a legal java identifier
        	// so there is no chance that we are filtering out actual declared fields.
        	if( field.isSynthetic() || HAS_INCONSISTENT_TYPE_HIERACHY.equals(field.getName())) 
        		continue;
            Declaration mirrorDecl = Factory.createDeclaration(field, _env);
            if( mirrorDecl != null)
                results.add( (FieldDeclaration)mirrorDecl);
        }
        return results;
    }

    public Collection<TypeDeclaration> getNestedTypes()
    {
        final ITypeBinding[] memberTypes = getDeclarationBinding().getDeclaredTypes();
        final List<TypeDeclaration> results = new ArrayList<TypeDeclaration>(memberTypes.length);
        for( ITypeBinding type : memberTypes ){
            Declaration mirrorDecl = Factory.createReferenceType(type, _env);
            if( mirrorDecl != null )
                results.add((TypeDeclaration)mirrorDecl);
        }
        return results;
    }

    public Collection<TypeParameterDeclaration> getFormalTypeParameters()
    {
        final ITypeBinding[] typeParams = getDeclarationBinding().getTypeParameters();
        final List<TypeParameterDeclaration> results = new ArrayList<TypeParameterDeclaration>(typeParams.length);
        for( ITypeBinding typeParam : typeParams ){
            Declaration mirrorDecl = Factory.createDeclaration(typeParam, _env);
            if( mirrorDecl != null )
                results.add( (TypeParameterDeclaration)mirrorDecl );
        }
        return results;
    }    

    public TypeDeclaration getDeclaringType()
    {
        final ITypeBinding decl = getDeclarationBinding();
        if( decl.isMember() )
        	return Factory.createReferenceType(decl.getDeclaringClass(), _env);
        return null;        
    }

    // Start of implementation of DeclaredType API
    public Collection<TypeMirror> getActualTypeArguments()
    {
        final ITypeBinding type = getTypeBinding();
        final ITypeBinding[] typeArgs = type.getTypeArguments();
        if( typeArgs == null || typeArgs.length == 0 )
    		return Collections.emptyList();

        final Collection<TypeMirror> result = new ArrayList<TypeMirror>(typeArgs.length);
        for( ITypeBinding arg : typeArgs ){
            final TypeMirror mirror = Factory.createTypeMirror(arg, _env);
            if(arg == null)
                result.add(Factory.createErrorClassType(arg));
            else
                result.add(mirror);
        }

        return result;
    }

    public DeclaredType getContainingType()
    {
        final ITypeBinding outer = getTypeBinding().getDeclaringClass();
        return Factory.createReferenceType(outer, _env);
    }

    public TypeDeclaration getDeclaration()
    {
        final ITypeBinding declBinding = getDeclarationBinding();
        if( declBinding == _binding ) return this;
        else return Factory.createReferenceType(declBinding, _env);
    }
    
    public Collection<InterfaceType> getSuperinterfaces()
    {
        final ITypeBinding[] superInterfaceBindings = getDeclarationBinding().getInterfaces();
        if( superInterfaceBindings == null || superInterfaceBindings.length == 0 )
            return Collections.emptyList();
        final List<InterfaceType> results = new ArrayList<InterfaceType>(superInterfaceBindings.length);
        for( ITypeBinding binding : superInterfaceBindings ){
            if( binding.isInterface() ){
                final TypeDeclarationImpl mirrorDecl = Factory.createReferenceType(binding, _env);
                if( mirrorDecl.kind() == MirrorKind.TYPE_INTERFACE ){
                    results.add((InterfaceType)mirrorDecl);
                }
            }
            else results.add(Factory.createErrorInterfaceType(binding));
        }
        return results;
    }

    public void accept(TypeVisitor visitor)
    {
        visitor.visitTypeMirror(this);
        visitor.visitDeclaredType(this);
        visitor.visitReferenceType(this);
    }

    // End of implementation of DeclaredType API

    public ITypeBinding getDeclarationBinding()
    {	
        final ITypeBinding type = getTypeBinding();
        return type.getTypeDeclaration();
    }

    protected List<? extends MethodDeclaration> _getMethods()
    {
        final IMethodBinding[] methods = getDeclarationBinding().getDeclaredMethods();
        final List<MethodDeclaration> results = new ArrayList<MethodDeclaration>(methods.length);
        for( IMethodBinding method : methods ){
            if( method.isConstructor() || method.isSynthetic() ) continue;
            Declaration mirrorDecl = Factory.createDeclaration(method, _env);
            if( mirrorDecl != null)
                results.add((MethodDeclaration)mirrorDecl);
        }
        return results;
    }
    
    public String toString()
    {		
    	return getQualifiedName();
    }

    public boolean isFromSource(){ return getDeclarationBinding().isFromSource(); }
}
