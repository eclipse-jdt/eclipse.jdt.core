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

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Represents a formal parameter that came from binary.
 */
public class BinaryParameterDeclarationImpl extends DeclarationImpl implements ParameterDeclaration
{
    private final IMethodBinding _method;
    private final int _rank;
    
    /**
     * Parameter declaration from binary
     */
    public BinaryParameterDeclarationImpl(ITypeBinding typeBinding,
                                          IMethodBinding method, int rank,
                                          ProcessorEnvImpl env)
    {
        super(typeBinding, env);
        _method = method;
        _rank = rank;
    }

    public void accept(DeclarationVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitParameterDeclaration(this);
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass)
    {
        // TODO: (theodora) handle the binary case.
        return null;
    }

    public Collection<AnnotationMirror> getAnnotationMirrors()
    {
        // TODO: (theodora) handle the binary case.
        return null;
    }

    public String getDocComment()
    {
        return null;
    }

    public Collection<Modifier> getModifiers()
    {
        return Collections.emptyList();
    }

    public SourcePosition getPosition()
    {
        return null;
    }

    public String getSimpleName()
    {
        return ParameterDeclarationImpl.ARG + _rank;
    }

    public TypeMirror getType()
    {
        final TypeMirror mirrorType = Factory.createTypeMirror(getTypeBinding(), _env);
        if( mirrorType == null )
            return Factory.createErrorClassType(getTypeBinding());
        return mirrorType;
    }

    private ITypeBinding getTypeBinding(){ return (ITypeBinding)_binding; }

    public MirrorKind kind(){ return MirrorKind.FORMAL_PARAMETER; }

    public int hashCode()
    {
        return _method.hashCode() + _rank;
    }

    public boolean equals(Object obj){
        if( obj instanceof BinaryParameterDeclarationImpl ){
            final BinaryParameterDeclarationImpl other =
                (BinaryParameterDeclarationImpl)obj;
            return other._method.isEqualTo(_method) && other._rank == _rank;
        }
        return false;
    }

    public String toString(){
        final StringBuilder builder = new StringBuilder();
        builder.append(getTypeBinding().getName());
        builder.append(' ');
        builder.append(ParameterDeclarationImpl.ARG);
        builder.append(_rank);
        return builder.toString();
    }
	
	public IBinding getDeclarationBinding(){ throw new UnsupportedOperationException("should never be called"); }
	
	boolean isFromSource(){ return false; }

    ASTNode getAstNode(){ return null; }

    public IResource getResource(){ return null; }
} 
