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

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/**
 * Represents a formal parameter that came from binary.
 */
public class BinaryParameterDeclarationImpl extends DeclarationImpl implements ParameterDeclaration
{
	static final String ARG = "arg"; //$NON-NLS-1$
	private final ITypeBinding _type;
	private final ExecutableDeclarationImpl _executable;
	private final int _paramIndex;
    /**
     * Parameter declaration from binary
     */
    public BinaryParameterDeclarationImpl(ExecutableDeclarationImpl executable,
										  ITypeBinding typeBinding,
                                          int index,
                                          BaseProcessorEnv env)
    {
        super(typeBinding, env);
        assert( typeBinding != null ) : "missing type binding"; //$NON-NLS-1$
        assert( index >= 0 ) : "invalid index " + index; //$NON-NLS-1$
        assert executable != null : "missing executable"; //$NON-NLS-1$
        _type = typeBinding;
        _paramIndex = index;
        _executable = executable;
    }

    @Override
	public void accept(DeclarationVisitor visitor)
    {
        visitor.visitParameterDeclaration(this);
    }

    @Override
	public Collection<Modifier> getModifiers()
    {
		// TODO
		// we don't store this information. so simply return nothing for now.
        return Collections.emptyList();
    }

    @Override
	public String getDocComment()
    {
        return null;
    }

    @Override
	public String getSimpleName()
    {
        final SingleVariableDeclaration decl = (SingleVariableDeclaration)getAstNode();
        if( decl == null ) return ARG + _paramIndex;
        final SimpleName name = decl.getName();
        return name == null ? ARG : name.toString();
    }

    @Override
	public SourcePosition getPosition()
    {
        return null;
    }

    @Override
	public TypeMirror getType()
    {
        final TypeMirror mirrorType = Factory.createTypeMirror(getTypeBinding(), _env);
        if( mirrorType == null )
            return Factory.createErrorClassType(getTypeBinding());
        return mirrorType;
    }

    @Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass)
    {
		final IMethodBinding methodBinding = _executable.getDeclarationBinding();
		final IAnnotationBinding[] paramAnnos = methodBinding.getParameterAnnotations(_paramIndex);
        return _getAnnotation(annotationClass, paramAnnos);
    }

    @Override
	public Collection<AnnotationMirror> getAnnotationMirrors()
    {
		final IMethodBinding methodBinding = _executable.getDeclarationBinding();
		final IAnnotationBinding[] paramAnnos = methodBinding.getParameterAnnotations(_paramIndex);
        return _getAnnotationMirrors(paramAnnos);
    }

	@Override
	public boolean isBindingBased(){ return true; }

	@Override
	public boolean isFromSource(){ return false; }

    @Override
	ASTNode getAstNode(){ return null; }

    @Override
	public IFile getResource(){ return null; }

    private ITypeBinding getTypeBinding(){ return _type; }

    @Override
	public MirrorKind kind(){ return MirrorKind.FORMAL_PARAMETER; }

    @Override
	public IBinding getDeclarationBinding(){
    	throw new UnsupportedOperationException("should never be invoked on a BinaryParameterDeclaration"); //$NON-NLS-1$
    }

    @Override
	public boolean equals(Object obj){
        if( obj instanceof BinaryParameterDeclarationImpl ){
            final BinaryParameterDeclarationImpl otherParam = (BinaryParameterDeclarationImpl)obj;
            return otherParam._paramIndex == _paramIndex  &&
                   otherParam._executable.getDeclarationBinding().isEqualTo(_executable.getDeclarationBinding()) ;
        }
        return false;
    }

    @Override
	public int hashCode(){
    	final String methodKey = _executable.getDeclarationBinding().getKey();
    	int hashcode = 0;
    	if( methodKey != null )
    		hashcode = methodKey.hashCode();
    	return hashcode + _paramIndex;
    }

    @Override
	public String toString(){
        final StringBuilder builder = new StringBuilder();
        builder.append(getTypeBinding().getName());
        builder.append(' ');
        builder.append(getSimpleName());
        return builder.toString();
    }
}
