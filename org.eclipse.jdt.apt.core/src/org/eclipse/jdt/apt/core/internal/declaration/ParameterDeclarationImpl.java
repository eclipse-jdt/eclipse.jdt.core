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
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.DeclarationVisitor;
import java.lang.annotation.Annotation;
import java.util.Collection;
import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IResolvedAnnotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/**
 * Represents a formal parameter
 */
public abstract class ParameterDeclarationImpl extends DeclarationImpl implements ParameterDeclaration
{
    static final String ARG = "arg"; //$NON-NLS-1$
	/** this executable that this parameter came from */
	protected final ExecutableDeclarationImpl _executable;
	/** thie parameter is the <code>_paramIndex</code>th in <code>_executable</code> */
	protected final int _paramIndex;
	
    /**
     * Parameter declaration 
     * @param the executable that declares this parameter
     * @param type the type of the parameter
     * @param index the index of this parameter in <code>executable</code>'s param list.
     */
    public ParameterDeclarationImpl(ExecutableDeclarationImpl executable, 
									ITypeBinding type, 
									int index,
									BaseProcessorEnv env)
    {	
		super(type, env);
		_executable = executable;
		_paramIndex = index;
        assert _executable != null : "missing executable"; //$NON-NLS-1$
        assert _paramIndex >= 0 : "invalid param index " + _paramIndex; //$NON-NLS-1$
    }

    public void accept(DeclarationVisitor visitor)
    {
        super.accept(visitor);
        visitor.visitParameterDeclaration(this);
    }   

    public String getDocComment()
    {
        return null;
    }
    
    public String getSimpleName()
    {
        final SingleVariableDeclaration decl = (SingleVariableDeclaration)getAstNode();
        if( decl == null ) return ARG + _paramIndex;
        final SimpleName name = decl.getName();
        return name == null ? ARG : name.toString();
    }

    public TypeMirror getType()
    {
        final TypeMirror mirrorType = Factory.createTypeMirror(getTypeBinding(), _env);
        if( mirrorType == null )
            return Factory.createErrorClassType(getTypeBinding());
        return mirrorType;
    }
	
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass)
    {
		final IMethodBinding methodBinding = _executable.getDeclarationBinding();
		final IResolvedAnnotation[] paramAnnos = methodBinding.getParameterAnnotations(_paramIndex); 
        return _getAnnotation(annotationClass, paramAnnos);
    }

    public Collection<AnnotationMirror> getAnnotationMirrors()
    {
		final IMethodBinding methodBinding = _executable.getDeclarationBinding();
		final IResolvedAnnotation[] paramAnnos = methodBinding.getParameterAnnotations(_paramIndex); 
        return _getAnnotationMirrors(paramAnnos);
    }

    private ITypeBinding getTypeBinding(){ return (ITypeBinding)_binding; }

    public MirrorKind kind(){ return MirrorKind.FORMAL_PARAMETER; }

    public int hashCode(){
    	final String methodKey = _executable.getDeclarationBinding().getKey();
    	int hashcode = 0;
    	if( methodKey != null )
    		hashcode = methodKey.hashCode();
    	return hashcode + _paramIndex; 
    }   

    public String toString(){		
        final StringBuilder builder = new StringBuilder();
        builder.append(getTypeBinding().getName());
        builder.append(' ');
        builder.append(getSimpleName());        
        return builder.toString();
    }
	
	public IBinding getDeclarationBinding(){ throw new UnsupportedOperationException("should never be called"); } //$NON-NLS-1$   
}
