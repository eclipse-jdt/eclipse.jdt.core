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

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.env.AnnotationInvocationHandler;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IResolvedAnnotation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.util.DeclarationVisitor;

public abstract class DeclarationImpl implements Declaration, EclipseMirrorImpl
{
	/** the type binding corresponding to this declaration */
    final IBinding _binding;
    final ProcessorEnvImpl _env;

    DeclarationImpl(final IBinding binding, final ProcessorEnvImpl env)
    {
        assert binding != null : "binding cannot be null"; //$NON-NLS-1$
        assert env != null : "missing environment"; //$NON-NLS-1$
        _binding = binding;
        _env = env;
    }

    public void accept(DeclarationVisitor visitor)
    {
        visitor.visitDeclaration(this);     
    }

    public boolean equals(Object obj)
    {
        if(obj instanceof DeclarationImpl)
            return _binding.isEqualTo( ((DeclarationImpl)obj)._binding );
            
        return false;
    }

    public int hashCode(){ return _binding.hashCode(); }

    <A extends Annotation> A _getAnnotation(Class<A> annotationClass,
                                            IResolvedAnnotation[] annoInstances)
    {
        final String annoTypeName = annotationClass.getName();
		if( annoTypeName == null ) return null;
		final int len = annoInstances == null ? 0 : annoInstances.length;
        if( len == 0 ) return null;
        for( IResolvedAnnotation annoInstance :  annoInstances){
            final ITypeBinding binding = annoInstance.getAnnotationType();
            if(binding.isAnnotation() ){
                final String curTypeName = binding.getQualifiedName();
                if( annoTypeName.equals(curTypeName) ){
                    final AnnotationMirrorImpl annoMirror =
                        (AnnotationMirrorImpl)Factory.createAnnotationMirror(annoInstance, this, _env);
                    final AnnotationInvocationHandler handler = new AnnotationInvocationHandler(annoMirror);
                    return (A)Proxy.newProxyInstance(annotationClass.getClassLoader(),
                                                     new Class[]{ annotationClass }, handler );
                }
            }
        }
        return null; 
    }
	
	<A extends Annotation> A _getAnnotation(Class<A> annotationClass,
            List<org.eclipse.jdt.core.dom.Annotation> annoInstances)
	{
		final String annoTypeName = annotationClass.getName();
		if( annoInstances == null || annoInstances.size() == 0 ) return null;
		for( org.eclipse.jdt.core.dom.Annotation annoInstance :  annoInstances){
			final ITypeBinding binding = annoInstance.resolveTypeBinding();
			if(binding.isAnnotation() ){
				final String curTypeName = binding.getQualifiedName();
				if( annoTypeName.equals(curTypeName) ){
				final AnnotationMirrorImpl annoMirror =
				(AnnotationMirrorImpl)Factory.createAnnotationMirror(annoInstance.resolveAnnotation(), this, _env);
				final AnnotationInvocationHandler handler = new AnnotationInvocationHandler(annoMirror);
				return (A)Proxy.newProxyInstance(annotationClass.getClassLoader(),
				                     new Class[]{ annotationClass }, handler );
				}
			}
		}
		return null;
	}

    Collection<AnnotationMirror> _getAnnotationMirrors(IResolvedAnnotation[] annoInstances)
    {
		final int len = annoInstances == null ? 0 : annoInstances.length;
        if( len == 0 ) return Collections.emptyList();
        final List<AnnotationMirror> result = new ArrayList<AnnotationMirror>(len);
        for(IResolvedAnnotation annoInstance : annoInstances){
            final AnnotationMirrorImpl annoMirror =
                        (AnnotationMirrorImpl)Factory.createAnnotationMirror(annoInstance, this, _env);
            result.add(annoMirror);
        }
        return result;
    }  
	
	Collection<AnnotationMirror> _getAnnotationMirrors(List<org.eclipse.jdt.core.dom.Annotation> annoInstances)
	{
		if( annoInstances == null || annoInstances.size() == 0 ) return Collections.emptyList();
		final List<AnnotationMirror> result = new ArrayList<AnnotationMirror>(annoInstances.size());
		for( org.eclipse.jdt.core.dom.Annotation annoInstance : annoInstances){
			final AnnotationMirrorImpl annoMirror =
				(AnnotationMirrorImpl)Factory.createAnnotationMirror(annoInstance.resolveAnnotation(), this, _env);
			result.add(annoMirror);
		}
		return result;
	}  
	



    
    /**
     * @return the binding that corresponds to the original declaration. 
     * For parameterized type or raw type, return the generic type declaration binding.
     * For parameterized method, return the method declaration binding that has the 
     * type parameters not the one with the parameters substituted with type arguments.
     * In other cases, simply return the same binding. 
     */
    public abstract IBinding getDeclarationBinding();
    
    public Collection<Modifier> getModifiers()
    {
        final int modBits = getDeclarationBinding().getModifiers();
        final List<Modifier> mods = new ArrayList<Modifier>(4);
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


    /**
     * @return true iff this declaration came from a source file.
     *         Return false otherwise.
     */
    abstract boolean isFromSource();
        
    /**
     * @return the ast node that corresponding to this declaration.
     *         Return null if this declaration came from binary.
     * @see #isFromSource();
     */
    ASTNode getAstNode(){
        if( !isFromSource() ) return null;
        return _env.getASTNodeForBinding(getDeclarationBinding());      
    }

    /**
     * @return the compilation unit that the ast node of this declaration came from
     *         Return null if this declaration came from binary.
     * @see #isFromSource()
     */
    CompilationUnit getCompilationUnit(){
        if( !isFromSource() ) return null;
        return _env.getCompilationUnitForBinding(getDeclarationBinding());
    }
	
	/**
	 * @return the resource of this declaration if the declaration is from source.
	 */
	public IFile getResource(){
        if( isFromSource() ){
            final IBinding binding = getDeclarationBinding();
			return _env.getDeclaringFileForBinding(binding);            
        }
        return null;
    }
	
	public ProcessorEnvImpl getEnvironment(){ return _env; }
} 
